package com.example.payhub.ingress;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/// Applies the durable-ingest schema at startup.
///
/// For prd/002 the hot path applies the {@code .sql} source of truth
/// idempotently ({@code CREATE TABLE IF NOT EXISTS}) so local/dev runs need no
/// separate migration step. Migration OWNERSHIP moves to the warm-path module
/// (Flyway) in a later story — this is the deliberate, reversible interim.
final class SchemaInitializer {

    /// Classpath location of the single-source-of-truth schema.
    static final String SCHEMA_RESOURCE = "/schema/webhook_event.sql";

    private final DataSource dataSource;

    SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /// Reads and executes the schema script. Idempotent: safe to run on every
    /// startup, including against a database that already has the table.
    ///
    /// @throws SQLException if the script fails to apply
    void apply() throws SQLException {
        String script = readScript();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // The driver executes the multi-statement script; each statement is
            // independently idempotent (IF NOT EXISTS).
            stmt.execute(script);
        }
    }

    private static String readScript() {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Schema resource not found on classpath: " + SCHEMA_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to read schema resource: " + SCHEMA_RESOURCE, e);
        }
    }
}
