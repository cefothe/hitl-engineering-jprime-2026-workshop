package com.example.payhub.ingress;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.helidon.http.Status;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import java.sql.SQLException;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Hot-path entry point. Everything is hand-wired here in {@code main} — there
/// is deliberately NO Spring, NO DI container, NO {@code @Bean}/{@code @Component}
/// lifecycle. The framework ban (CLAUDE.md, ADR 0019) is the whole point of the
/// hot path; this class is the proof that the path needs none of it.
///
/// Wiring order:
///   1. Read config from the environment (fail fast on missing secrets).
///   2. Open a HikariCP pool (ADR 0021) — a plain library, not a framework.
///   3. Apply the .sql schema idempotently (ADR 0002).
///   4. Build the plain-JDBC repository and the webhook handler.
///   5. Start Helidon Nima (ADR 0020) with the single POST route.
///   6. Register a shutdown hook to stop the server and close the pool.
public final class IngressApplication {

    private static final Logger LOG = LoggerFactory.getLogger(IngressApplication.class);

    /// Route the hot path listens on. PayHub is configured to POST here.
    static final String WEBHOOK_PATH = "/webhooks/payhub";

    private IngressApplication() {
    }

    public static void main(String[] args) {
        IngressConfig config = IngressConfig.fromEnvironment();

        HikariDataSource dataSource = openPool(config);
        try {
            new SchemaInitializer(dataSource).apply();
        } catch (SQLException e) {
            // Cannot guarantee durability without the table — refuse to start.
            LOG.error("Schema initialization failed (sqlState={}, errorCode={}); aborting startup",
                    e.getSQLState(), e.getErrorCode());
            dataSource.close();
            throw new IllegalStateException("Schema initialization failed", e);
        }

        WebhookEventRepository repository = new WebhookEventRepository(dataSource);
        WebhookHandler handler = new WebhookHandler(
                repository,
                new EventIdExtractor(config.eventIdHeader()),
                config.maxBodyBytes(),
                Clock.systemUTC());

        WebServer server = startServer(config, handler);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down hot path");
            server.stop();
            dataSource.close();
        }, "ingress-shutdown"));

        LOG.info("PayHub hot path listening on port {} at {}",
                server.port(), WEBHOOK_PATH);
    }

    /// Hand-wires the HikariCP pool from config. No Spring auto-config, no
    /// {@code @Bean} — a plain library object whose lifecycle we own.
    static HikariDataSource openPool(IngressConfig config) {
        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("payhub-hot-path");
        hikari.setJdbcUrl(config.jdbcUrl());
        hikari.setUsername(config.dbUser());
        hikari.setPassword(config.dbPassword());
        hikari.setMaximumPoolSize(config.poolMaxSize());
        // The hot path's only DB work is a short single-row commit; default
        // autoCommit stays true at the pool level — the repository flips it to
        // false per transaction and restores it before returning the connection.
        hikari.setAutoCommit(true);
        return new HikariDataSource(hikari);
    }

    /// Builds and starts the Nima server with exactly one POST route plus a
    /// 405 catch-all so non-POST methods on the path are rejected explicitly.
    static WebServer startServer(IngressConfig config, WebhookHandler handler) {
        return WebServer.builder()
                .port(config.httpPort())
                // Reject oversized bodies at the listener; the handler also
                // checks the cap to produce the explicit 413 contract.
                .maxPayloadSize(config.maxBodyBytes())
                .routing((HttpRouting.Builder routing) -> routing
                        .post(WEBHOOK_PATH, handler)
                        // Any other method on the route is not allowed.
                        .any(WEBHOOK_PATH, (req, res) ->
                                res.status(Status.METHOD_NOT_ALLOWED_405_CODE).send()))
                .build()
                .start();
    }
}
