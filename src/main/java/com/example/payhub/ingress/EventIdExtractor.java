package com.example.payhub.ingress;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

/// Extracts the PayHub event id from a request.
///
/// ASSUMPTION (pending prd/001 spike): the prd/001 spike could not run — there
/// is no PayHub sandbox in this repo — so PayHub's real envelope is unconfirmed.
/// The documented, reversible default is:
///
///   1. Prefer a configurable header (default {@code PayHub-Event-Id}). Webhook
///      providers commonly surface the event id in a header, and reading it
///      avoids parsing the body at all.
///   2. If the header is absent or blank, fall back to a minimal scan for a
///      top-level JSON {@code "id"} string field in the raw body.
///
/// This is intentionally NOT a full JSON parser: the hot path must not couple
/// itself to PayHub's full body schema before the spike confirms it, and a
/// regex scan keeps the hot path allocation-light. When the spike lands, this
/// is the single place to correct the extraction strategy.
final class EventIdExtractor {

    /// Matches the first top-level {@code "id": "<value>"} occurrence and
    /// captures the value. Tolerates whitespace around the colon. Deliberately
    /// conservative: only string ids, which is what PayHub-style ids are.
    private static final Pattern JSON_ID =
            Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");

    private final String headerName;

    EventIdExtractor(String headerName) {
        this.headerName = headerName;
    }

    /// @param headerValue the value of the configured event-id header, or
    ///                    {@code null} if the header was not present
    /// @param body        the raw request body bytes (verbatim)
    /// @return the extracted event id, or empty if neither source yields one
    Optional<String> extract(String headerValue, byte[] body) {
        if (headerValue != null && !headerValue.isBlank()) {
            return Optional.of(headerValue.trim());
        }
        return scanJsonId(body);
    }

    String headerName() {
        return headerName;
    }

    private static Optional<String> scanJsonId(byte[] body) {
        if (body == null || body.length == 0) {
            return Optional.empty();
        }
        // The body is PII; decode only enough to find the id and never log it.
        String text = new String(body, StandardCharsets.UTF_8);
        var matcher = JSON_ID.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            return value.isEmpty() ? Optional.empty() : Optional.of(value);
        }
        return Optional.empty();
    }
}
