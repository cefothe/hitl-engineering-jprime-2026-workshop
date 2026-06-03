package com.example.payhub.ingress;

import java.util.Objects;

/// Hot-path configuration, sourced exclusively from environment variables.
///
/// No secrets are ever read from files or the repo (CLAUDE.md secrets rule);
/// the database password arrives via {@code PAYHUB_DB_PASSWORD}. See README for
/// the full list of variables.
///
/// This is a hand-built config object — there is deliberately no Spring
/// {@code @ConfigurationProperties} or any framework binding on the hot path.
record IngressConfig(
        String jdbcUrl,
        String dbUser,
        String dbPassword,
        int httpPort,
        String eventIdHeader,
        int maxBodyBytes,
        int poolMaxSize) {

    /// Default header PayHub is assumed to use for the event id. This is an
    /// ASSUMPTION pending the prd/001 spike (no PayHub sandbox in this repo to
    /// confirm the real envelope) and is overridable via PAYHUB_EVENT_ID_HEADER.
    static final String DEFAULT_EVENT_ID_HEADER = "PayHub-Event-Id";

    /// 1 MiB body cap. Bodies larger than this are rejected with 413 before any
    /// write is attempted, bounding memory per request under burst.
    static final int DEFAULT_MAX_BODY_BYTES = 1024 * 1024;

    static final int DEFAULT_HTTP_PORT = 8080;

    static final int DEFAULT_POOL_MAX_SIZE = 10;

    IngressConfig {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(dbUser, "dbUser");
        Objects.requireNonNull(dbPassword, "dbPassword");
        Objects.requireNonNull(eventIdHeader, "eventIdHeader");
        if (httpPort < 0 || httpPort > 65_535) {
            throw new IllegalArgumentException("httpPort out of range: " + httpPort);
        }
        if (maxBodyBytes <= 0) {
            throw new IllegalArgumentException("maxBodyBytes must be positive: " + maxBodyBytes);
        }
        if (poolMaxSize <= 0) {
            throw new IllegalArgumentException("poolMaxSize must be positive: " + poolMaxSize);
        }
    }

    /// Builds the configuration from the process environment.
    ///
    /// @throws IllegalStateException if a required variable is missing, so the
    ///         process fails fast at startup rather than at first request.
    static IngressConfig fromEnvironment() {
        return new IngressConfig(
                required("PAYHUB_DB_URL"),
                required("PAYHUB_DB_USER"),
                required("PAYHUB_DB_PASSWORD"),
                intEnv("PAYHUB_HTTP_PORT", DEFAULT_HTTP_PORT),
                envOrDefault("PAYHUB_EVENT_ID_HEADER", DEFAULT_EVENT_ID_HEADER),
                intEnv("PAYHUB_MAX_BODY_BYTES", DEFAULT_MAX_BODY_BYTES),
                intEnv("PAYHUB_DB_POOL_MAX_SIZE", DEFAULT_POOL_MAX_SIZE));
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required environment variable not set: " + name);
        }
        return value;
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static int intEnv(String name, int fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Environment variable " + name + " must be an integer, was: " + value, e);
        }
    }
}
