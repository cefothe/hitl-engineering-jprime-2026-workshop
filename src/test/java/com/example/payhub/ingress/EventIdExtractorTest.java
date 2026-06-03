package com.example.payhub.ingress;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EventIdExtractorTest {

    private final EventIdExtractor extractor = new EventIdExtractor("PayHub-Event-Id");

    @Test
    void prefersHeaderWhenPresent() {
        byte[] body = "{\"id\":\"evt_from_body\"}".getBytes(StandardCharsets.UTF_8);

        Optional<String> result = extractor.extract("evt_from_header", body);

        assertThat(result).contains("evt_from_header");
    }

    @Test
    void trimsHeaderValue() {
        Optional<String> result = extractor.extract("  evt_123  ", new byte[0]);

        assertThat(result).contains("evt_123");
    }

    @Test
    void fallsBackToJsonIdWhenHeaderAbsent() {
        byte[] body = "{\"id\":\"evt_body_456\",\"type\":\"payment.succeeded\"}"
                .getBytes(StandardCharsets.UTF_8);

        Optional<String> result = extractor.extract(null, body);

        assertThat(result).contains("evt_body_456");
    }

    @Test
    void fallsBackToJsonIdWhenHeaderBlank() {
        byte[] body = "{\"id\":\"evt_body_789\"}".getBytes(StandardCharsets.UTF_8);

        Optional<String> result = extractor.extract("   ", body);

        assertThat(result).contains("evt_body_789");
    }

    @Test
    void tolerwatesWhitespaceAroundJsonColon() {
        byte[] body = "{ \"id\" :   \"evt_spaced\" }".getBytes(StandardCharsets.UTF_8);

        Optional<String> result = extractor.extract(null, body);

        assertThat(result).contains("evt_spaced");
    }

    @Test
    void returnsEmptyWhenNoHeaderAndNoJsonId() {
        byte[] body = "{\"type\":\"payment.succeeded\"}".getBytes(StandardCharsets.UTF_8);

        Optional<String> result = extractor.extract(null, body);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyForEmptyBodyAndNoHeader() {
        Optional<String> result = extractor.extract(null, new byte[0]);

        assertThat(result).isEmpty();
    }

    @Test
    void exposesConfiguredHeaderName() {
        assertThat(extractor.headerName()).isEqualTo("PayHub-Event-Id");
    }
}
