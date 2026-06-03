package com.example.payhub.ingress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import javax.sql.DataSource;

/// Plain-JDBC durable-write repository for inbound webhook events.
///
/// No ORM, no Spring {@code Repository}, no JPA {@code @Entity} — the hot-path
/// framework ban (CLAUDE.md, ADR 0002, ADR 0019) forbids them. This class owns
/// the transactional discipline that makes Goal 1 ("never lost", ADR 0011)
/// concrete: {@code setAutoCommit(false)} → INSERT → {@code commit()}. The ACK
/// is returned by the handler ONLY after this method returns normally.
final class WebhookEventRepository {

    private static final String INSERT_SQL = """
            INSERT INTO webhook_event (event_id, payload, received_at)
            VALUES (?, ?, ?)
            """;

    private final DataSource dataSource;

    WebhookEventRepository(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    /// Durably stores one event and commits before returning.
    ///
    /// On success the row is committed and survives a hard process restart
    /// (prd/002 AC4). On ANY {@link SQLException} at or before commit the
    /// transaction is rolled back and the exception propagates, so the handler
    /// returns a retryable non-2xx (prd/002 AC2/AC3).
    ///
    /// PII guard: the {@code payload} bytes are never logged here, and no bound
    /// parameter is ever placed into an exception message or log line.
    ///
    /// @param eventId    the PayHub event id (carries a UNIQUE constraint; dedup
    ///                    behaviour itself is prd/004, out of scope here)
    /// @param payload    the verbatim raw request body
    /// @param receivedAt the UTC instant the request was received
    /// @throws SQLException if the durable write cannot be committed
    void store(String eventId, byte[] payload, Instant receivedAt) throws SQLException {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(receivedAt, "receivedAt");

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                ps.setString(1, eventId);
                ps.setBytes(2, payload);
                ps.setTimestamp(3, Timestamp.from(receivedAt));
                ps.executeUpdate();
            }

            // Durability boundary: the event is "not lost" the instant this
            // commit succeeds (ADR 0011). The caller must not ACK before this.
            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            // Re-throw WITHOUT echoing payload or bound params (no-PII rule).
            throw e;
        } finally {
            closeQuietly(conn);
        }
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Best-effort rollback; the original failure is what matters and is
            // already being propagated. Nothing PII-bearing to log here.
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            // Restoring autoCommit before returning the connection to the pool
            // keeps pooled connections in a clean default state.
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
            // ignore — connection is being returned/closed regardless
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
            // ignore — pool will evict a bad connection
        }
    }
}
