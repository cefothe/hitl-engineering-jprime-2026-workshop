package com.example.payhub.ingress;

import com.zaxxer.hikari.HikariDataSource;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

/// End-to-end proof of the FAILURE half of the status contract (prd/002 AC3):
/// a real {@link SQLException} on the durable-write path must surface as an HTTP
/// 503, and nothing may be ACKed before the commit succeeds.
///
/// Failure injection is deterministic, not timing-based: the real Helidon Nima
/// server is wired to a REAL PostgreSQL (Testcontainers) with the schema applied,
/// then the target table is DROPPED out from under the running handler. The next
/// INSERT therefore throws a genuine SQLException ("relation does not exist"),
/// exercising exactly the {@code catch (SQLException)} → 503 branch in
/// {@link WebhookHandler} — no mocks, no sleeps, no flakiness.
///
/// The test fails if the handler ever ACKed-before-commit or swallowed the write
/// error into a 2xx: the response must be exactly 503, and after re-creating the
/// table no row for the event id may exist (the failed INSERT committed nothing).
///
/// This IT deliberately destroys the schema, so it lives in its own class with a
/// private stack rather than sharing {@link WebhookEndpointIT}'s healthy server.
@Testcontainers
class WriteFailure503IT {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by @Testcontainers
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private static IngressConfig config;
    private static HikariDataSource pool;
    private static WebServer server;
    private static HttpClient httpClient;
    private static String baseUri;

    @BeforeAll
    static void startStack() throws SQLException {
        config = new IngressConfig(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                0, // ephemeral port
                IngressConfig.DEFAULT_EVENT_ID_HEADER,
                IngressConfig.DEFAULT_MAX_BODY_BYTES,
                2);

        pool = IngressApplication.openPool(config);
        // Apply the schema so the server starts against a valid table; the test
        // body then drops it to inject the write failure.
        new SchemaInitializer(pool).apply();

        WebhookHandler handler = new WebhookHandler(
                new WebhookEventRepository(pool),
                new EventIdExtractor(config.eventIdHeader()),
                config.maxBodyBytes(),
                Clock.systemUTC());

        server = IngressApplication.startServer(config, handler);
        baseUri = "http://localhost:" + server.port() + IngressApplication.WEBHOOK_PATH;
        httpClient = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopStack() {
        if (server != null) {
            server.stop();
        }
        if (pool != null) {
            pool.close();
        }
    }

    @Test
    void writeFailureSurfacesAs503AndCommitsNothing()
            throws IOException, InterruptedException, SQLException {
        // Inject a deterministic, real write failure: remove the target table so
        // the handler's INSERT throws SQLException on the very next request.
        dropWebhookEventTable();

        byte[] body = "{\"type\":\"payment.succeeded\"}".getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUri))
                .header("PayHub-Event-Id", "evt_write_fail_503")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // The write failed at/before commit; the handler must return 503, never a 2xx.
        assertThat(response.statusCode()).isEqualTo(503);

        // Re-create the table and prove the failed INSERT committed nothing: an
        // ACK-before-commit (or a swallowed error) would have left a row behind.
        recreateWebhookEventTable();
        assertThat(rowCount("evt_write_fail_503")).isZero();
    }

    private static void dropWebhookEventTable() throws SQLException {
        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE webhook_event");
        }
    }

    private static void recreateWebhookEventTable() throws SQLException {
        new SchemaInitializer(pool).apply();
    }

    private static int rowCount(String eventId) throws SQLException {
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
