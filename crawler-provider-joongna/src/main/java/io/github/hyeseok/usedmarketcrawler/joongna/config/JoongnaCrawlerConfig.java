package io.github.hyeseok.usedmarketcrawler.joongna.config;

import java.time.Duration;

public record JoongnaCrawlerConfig(
    String baseUrl,
    Duration connectTimeout,
    Duration requestTimeout,
    String userAgent
) {

    public JoongnaCrawlerConfig {
        if (
            baseUrl == null
                || baseUrl.isBlank()
        ) {
            throw new IllegalArgumentException(
                "baseUrl must not be blank"
            );
        }

        if (
            connectTimeout == null
        ) {
            throw new IllegalArgumentException(
                "connectTimeout must not be null"
            );
        }

        if (
            requestTimeout == null
        ) {
            throw new IllegalArgumentException(
                "requestTimeout must not be null"
            );
        }

        if (
            userAgent == null
                || userAgent.isBlank()
        ) {
            throw new IllegalArgumentException(
                "userAgent must not be blank"
            );
        }
    }

    public String searchUrl() {

        return baseUrl
            + "/search/";
    }
}