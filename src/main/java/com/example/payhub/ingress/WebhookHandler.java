package com.example.payhub.ingress;

import io.helidon.http.HeaderName;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Hot-path handler for {@code POST /webhooks/payhub}.
///
/// Reads the request body as RAW BYTES preserved verbatim (so prd/003 can HMAC
/// over exactly what PayHub sent), extracts the event id, durably commits the
/// row, and ACKs only after the commit succeeds (ADR 0011).
///
/// Status contract (prd/002):
///   200 — durable commit succeeded (event is "not lost")
///   413 — body exceeded the configured cap (rejected before any write)
///   400 — no event id could be extracted (cannot satisfy the UNIQUE column)
///   503 — durable write failed; retryable, so PayHub re-delivers
///
/// PII rule (CLAUDE.md): the raw body is PII. It is NEVER logged, and no log
/// line — including error paths — carries the payload or bound parameters.
/// Only the event id and received-at timestamp are logged.
///
/// This class is plain Java: no Spring, no DI annotations. It is constructed
/// by hand in {@link IngressApplication} and registered as a Helidon
/// {@link Handler}.
final class WebhookHandler implements Handler {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookHandler.class);

    private final WebhookEventRepository repository;
    private final EventIdExtractor eventIdExtractor;
    private final HeaderName eventIdHeaderName;
    private final long maxBodyBytes;
    private final Clock clock;

    WebhookHandler(WebhookEventRepository repository,
                   EventIdExtractor eventIdExtractor,
                   long maxBodyBytes,
                   Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.eventIdExtractor = Objects.requireNonNull(eventIdExtractor, "eventIdExtractor");
        this.eventIdHeaderName = HeaderNames.create(eventIdExtractor.headerName());
        this.maxBodyBytes = maxBodyBytes;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void handle(ServerRequest request, ServerResponse response) {
        // Read the body verbatim. Helidon enforces maxPayloadSize at the
        // listener; this is a defence-in-depth check against the configured cap
        // and produces the explicit 413 contract.
        byte[] body = request.content().as(byte[].class);
        if (body.length > maxBodyBytes) {
            response.status(Status.REQUEST_ENTITY_TOO_LARGE_413_CODE).send();
            return;
        }

        String headerValue = request.headers().first(eventIdHeaderName).orElse(null);
        Optional<String> eventId = eventIdExtractor.extract(headerValue, body);
        if (eventId.isEmpty()) {
            // No id means we cannot honour the UNIQUE column. This is a
            // client/contract problem, not a transient failure — 400, not 503,
            // so PayHub does not retry a request that can never succeed as-is.
            LOG.warn("Rejecting webhook: no event id in header '{}' or body",
                    eventIdHeaderName.defaultCase());
            response.status(Status.BAD_REQUEST_400_CODE).send();
            return;
        }

        String id = eventId.get();
        Instant receivedAt = Instant.now(clock);

        try {
            repository.store(id, body, receivedAt);
        } catch (SQLException e) {
            // Write failed at/before commit: nothing is durable. Return a
            // retryable status so PayHub re-delivers within its window.
            // Log the id and the SQLState/error code ONLY — never the payload.
            LOG.error("Durable write failed for event id {} (sqlState={}, errorCode={}); returning 503",
                    id, e.getSQLState(), e.getErrorCode());
            response.status(Status.SERVICE_UNAVAILABLE_503_CODE).send();
            return;
        }

        // Commit succeeded: the event is durably stored and survives restart.
        LOG.info("Durably stored webhook event id {} received_at {}", id, receivedAt);
        response.status(Status.OK_200_CODE).send();
    }
}
