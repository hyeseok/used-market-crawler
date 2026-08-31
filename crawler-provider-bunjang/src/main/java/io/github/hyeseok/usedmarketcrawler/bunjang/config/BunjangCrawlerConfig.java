package io.github.hyeseok.usedmarketcrawler.bunjang.config;

import java.time.Duration;
import java.util.Objects;

public record BunjangCrawlerConfig(
    String baseUrl,
    String webBaseUrl,
    Duration connectTimeout,
    Duration requestTimeout,
    String userAgent,
    int imageResolution
) {

    public BunjangCrawlerConfig {

        Objects.requireNonNull(
            baseUrl,
            "baseUrl must not be null"
        );

        Objects.requireNonNull(
            webBaseUrl,
            "webBaseUrl must not be null"
        );

        Objects.requireNonNull(
            connectTimeout,
            "connectTimeout must not be null"
        );

        Objects.requireNonNull(
            requestTimeout,
            "requestTimeout must not be null"
        );

        Objects.requireNonNull(
            userAgent,
            "userAgent must not be null"
        );

        if (
            baseUrl.isBlank()
        ) {
            throw new IllegalArgumentException(
                "baseUrl must not be blank"
            );
        }

        if (
            webBaseUrl.isBlank()
        ) {
            throw new IllegalArgumentException(
                "webBaseUrl must not be blank"
            );
        }

        if (
            userAgent.isBlank()
        ) {
            throw new IllegalArgumentException(
                "userAgent must not be blank"
            );
        }

        if (
            imageResolution <= 0
        ) {
            throw new IllegalArgumentException(
                "imageResolution must be greater than 0"
            );
        }
    }

    public String keywordSpecUrl() {
        return baseUrl
            + "/api/search/v8/pw/product/specs/keyword";
    }

    public String searchUrl() {
        return baseUrl
            + "/api/search/v8/web/search";
    }

    public String productUrl(
        long pid
    ) {
        return webBaseUrl
            + "/products/"
            + pid;
    }
}