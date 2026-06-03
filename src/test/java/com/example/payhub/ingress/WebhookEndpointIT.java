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
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

/// End-to-end HTTP test of the hot path: a real Helidon Nima server in front of
/// a real PostgreSQL (Testcontainers), driven by the JDK HttpClient. Validates
/// the full status contract (prd/002 AC1, AC2, AC3) and that the committed row
/// matches the bytes POSTed.
@Testcontainers
class WebhookEndpointIT {

    @Container
    @SuppressWarnings("resource") // lifecycle managed by @Testcontainers
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private static HikariDataSource pool;
    private static WebServer server;
    private static HttpClient httpClient;
    private static String baseUri;

    @BeforeAll
    static void startStack() throws SQLException {
        IngressConfig config = new IngressConfig(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                0, // ephemeral port
                IngressConfig.DEFAULT_EVENT_ID_HEADER,
                64, // tiny cap so we can exercise the 413 path
                2);

        pool = IngressApplication.openPool(config);
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
    void postWithEventIdHeaderReturns200AndCommits() throws IOException, InterruptedException, SQLException {
        byte[] body = "{\"type\":\"payment.succeeded\"}".getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUri))
                .header("PayHub-Event-Id", "evt_http_200")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(committedPayload("evt_http_200")).isEqualTo(body);
    }

    @Test
    void postWithEventIdInBodyReturns200() throws IOException, InterruptedException, SQLException {
        byte[] body = "{\"id\":\"evt_http_body\"}".getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUri))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(committedPayload("evt_http_body")).isEqualTo(body);
    }

    @Test
    void postWithNoExtractableEventIdReturns400() throws IOException, InterruptedException {
        byte[] body = "{\"type\":\"noid\"}".getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUri))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void getOnWebhookPathReturns405() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUri))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(405);
    }

    @Test
    void oversizedBodyIsRejected() throws IOException, InterruptedException, SQLException {
        // Cap is 64 bytes (see startStack); this body exceeds it.
        byte[] body = new byte[256];
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUri))
                .header("PayHub-Event-Id", "evt_too_big")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // The listener enforces maxPayloadSize; the oversized body is rejected
        // (413) before any durable write. Either way it must NOT be a 2xx.
        assertThat(response.statusCode()).isEqualTo(413);
        assertThat(rowCount("evt_too_big")).isZero();
    }

    private static byte[] committedPayload(String eventId) throws SQLException {
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
