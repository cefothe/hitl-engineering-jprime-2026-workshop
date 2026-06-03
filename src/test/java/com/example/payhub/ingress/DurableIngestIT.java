package com.example.payhub.ingress;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/// Integration test for the hot-path durable-write contract against a REAL
/// PostgreSQL (Testcontainers). Covers prd/002 AC2, AC3, and the headline AC4:
/// a committed event survives a hard process restart.
///
/// The container outlives both pools and is the durable substrate; closing a
/// HikariDataSource discards ALL in-memory state (pooled connections, caches),
/// which is the faithful in-test analogue of killing and restarting the JVM —
/// only what was committed to Postgres can survive. We deliberately do NOT
/// truncate between the "before restart" and "after restart" phases.
@Testcontainers
class DurableIngestIT {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by @Testcontainers
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private static IngressConfig config;

    @BeforeAll
    static void buildConfig() {
        config = new IngressConfig(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                0,
                IngressConfig.DEFAULT_EVENT_ID_HEADER,
                IngressConfig.DEFAULT_MAX_BODY_BYTES,
                2);
    }

    @AfterAll
    static void cleanup() {
        // Container teardown is handled by @Testcontainers; nothing to do.
    }

    @Test
    void committedEventSurvivesHardRestart() throws SQLException {
        byte[] payload = "{\"id\":\"evt_restart\",\"type\":\"payment.succeeded\"}"
                .getBytes(StandardCharsets.UTF_8);
        Instant receivedAt = Instant.parse("2026-06-02T10:15:30Z");

        // --- Process lifetime #1: apply schema, commit one event, then "die". ---
        HikariDataSource poolOne = IngressApplication.openPool(config);
        new SchemaInitializer(poolOne).apply();
        new WebhookEventRepository(poolOne).store("evt_restart", payload, receivedAt);
        // Hard "process restart": tear down the entire pool and all its state.
        poolOne.close();

        // --- Process lifetime #2: brand-new pool to the SAME database. ---
        HikariDataSource poolTwo = IngressApplication.openPool(config);
        try {
            // Schema bootstrap is idempotent on restart.
            new SchemaInitializer(poolTwo).apply();

            byte[] stored = readPayload(poolTwo, "evt_restart");
            assertThat(stored).isEqualTo(payload);
            assertThat(countRows(poolTwo, "evt_restart")).isEqualTo(1);
        } finally {
            poolTwo.close();
        }
    }

    @Test
    void storePersistsRawBytesVerbatim() throws SQLException {
        // Bytes that are not valid as a printable string round-trip exactly,
        // proving we store the verbatim body (needed for prd/003 HMAC).
        byte[] payload = {0x00, 0x7B, (byte) 0xC3, (byte) 0xA9, 0x7D, (byte) 0xFF};
        Instant receivedAt = Instant.parse("2026-06-02T11:00:00Z");

        HikariDataSource pool = IngressApplication.openPool(config);
        try {
            new SchemaInitializer(pool).apply();
            new WebhookEventRepository(pool).store("evt_verbatim", payload, receivedAt);

            assertThat(readPayload(pool, "evt_verbatim")).isEqualTo(payload);
        } finally {
            pool.close();
        }
    }

    @Test
    void duplicateEventIdViolatesUniqueConstraintAndRollsBack() throws SQLException {
        byte[] payload = "{\"id\":\"evt_dup\"}".getBytes(StandardCharsets.UTF_8);
        Instant receivedAt = Instant.parse("2026-06-02T12:00:00Z");

        HikariDataSource pool = IngressApplication.openPool(config);
        try {
            new SchemaInitializer(pool).apply();
            WebhookEventRepository repository = new WebhookEventRepository(pool);

            repository.store("evt_dup", payload, receivedAt);

            // Second insert of the same event id breaches the UNIQUE constraint.
            // The repository must surface a SQLException (so the handler 503s)
            // and must NOT leave a partial/extra row.
            assertThatThrownBy(() -> repository.store("evt_dup", payload, receivedAt))
                    .isInstanceOf(SQLException.class);

            assertThat(countRows(pool, "evt_dup")).isEqualTo(1);
        } finally {
            pool.close();
        }
    }

    private static byte[] readPayload(HikariDataSource pool, String eventId) throws SQLException {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT payload FROM webhook_event WHERE event_id = ?")) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).as("row for %s present", eventId).isTrue();
                return rs.getBytes(1);
            }
        }
    }

    private static int countRows(HikariDataSource pool, String eventId) throws SQLException {
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM webhook_event WHERE event_id = ?")) {
            ps.setString(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
