package io.github.hyeseok.usedmarketcrawler.daangn.config;

import java.time.Duration;

public record DaangnCrawlerConfig(

    String baseUrl,

    Duration connectTimeout,

    Duration requestTimeout,

    String userAgent

) {

    private static final String DEFAULT_BASE_URL =
        "https://www.daangn.com";

    private static final Duration DEFAULT_CONNECT_TIMEOUT =
        Duration.ofSeconds(
            5
        );

    private static final Duration DEFAULT_REQUEST_TIMEOUT =
        Duration.ofSeconds(
            10
        );

    private static final String DEFAULT_USER_AGENT =
        "used-market-crawler/0.1 "
            + "(+https://github.com/hyeseok/used-market-crawler)";

    public DaangnCrawlerConfig {

        if (
            baseUrl == null ||
            baseUrl.isBlank()
        ) {

            baseUrl =
                DEFAULT_BASE_URL;
        }

        baseUrl =
            stripTrailingSlash(
                baseUrl.trim()
            );

        if (
            connectTimeout == null ||
            connectTimeout.isNegative() ||
            connectTimeout.isZero()
        ) {

            connectTimeout =
                DEFAULT_CONNECT_TIMEOUT;
        }

        if (
            requestTimeout == null ||
            requestTimeout.isNegative() ||
            requestTimeout.isZero()
        ) {

            requestTimeout =
                DEFAULT_REQUEST_TIMEOUT;
        }

        if (
            userAgent == null ||
            userAgent.isBlank()
        ) {

            userAgent =
                DEFAULT_USER_AGENT;
        }
    }

    public static DaangnCrawlerConfig defaults() {

        return new DaangnCrawlerConfig(
            DEFAULT_BASE_URL,
            DEFAULT_CONNECT_TIMEOUT,
            DEFAULT_REQUEST_TIMEOUT,
            DEFAULT_USER_AGENT
        );
    }

    /**
     * 당근 중고거래 검색 endpoint.
     *
     * 현재 실제 응답이 확인된
     * /kr/buy-sell/all/ 을 사용한다.
     */
    public String searchUrl() {

        return baseUrl
            + "/kr/buy-sell/all/";
    }

    private static String stripTrailingSlash(
        String value
    ) {

        while (
            value.endsWith("/")
        ) {

            value =
                value.substring(
                    0,
                    value.length() - 1
                );
        }

        return value;
    }
}