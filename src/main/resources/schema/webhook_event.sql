-- ============================================================================
-- PayHub webhook durable-ingest schema.
--
-- This .sql file is the SINGLE SOURCE OF TRUTH for the table shape and the
-- cross-framework contract between the hot path (plain JDBC, this story) and
-- the warm path (Spring Data, later stories). See ADR 0002 and architect
-- tension T1: the schema is owned by neither Spring nor the ingress.
--
-- For story prd/002 the hot path applies this idempotently at startup
-- (CREATE TABLE IF NOT EXISTS). Migration ownership MOVES to the warm-path
-- module (Flyway) in a later story — this app-applied bootstrap is the
-- interim, deliberately reversible choice.
-- ============================================================================

CREATE TABLE IF NOT EXISTS webhook_event (
    -- Surrogate primary key. The hot path never reasons about ordering by id;
    -- it is here only as a stable internal handle.
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- PayHub's event identifier, extracted from the request (header or minimal
    -- body scan — see IngressApplication / EventIdExtractor). UNIQUE so the
    -- warm-path dedup logic (prd/004) has the constraint it needs; the dedup
    -- behaviour itself is OUT OF SCOPE for prd/002.
    event_id      TEXT        NOT NULL,

    -- The verbatim request body bytes. Stored raw so a later story can HMAC
    -- over exactly what PayHub sent (prd/003). This is PII — never logged.
    payload       BYTEA       NOT NULL,

    -- When the hot path received the request (UTC, set by the application).
    received_at   TIMESTAMPTZ NOT NULL,

    -- Claim/processed marker for the warm path's "SELECT ... FOR UPDATE SKIP
    -- LOCKED" drain. Defined now as part of the contract; the hot path only
    -- ever writes the default (false) and never reads it.
    processed     BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_webhook_event_event_id UNIQUE (event_id)
);

-- Supports the warm path's claim scan over unprocessed rows in arrival order.
CREATE INDEX IF NOT EXISTS ix_webhook_event_unprocessed
    ON webhook_event (received_at)
    WHERE processed = FALSE;
