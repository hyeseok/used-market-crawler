package io.github.hyeseok.usedmarketcrawler.daangn.client;

import io.github.hyeseok.usedmarketcrawler.daangn.config.DaangnCrawlerConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DaangnClient {

    private static final String SEARCH_DATA_ROUTE =
        "routes/kr.buy-sell._index";

    private final DaangnCrawlerConfig config;

    private final HttpClient httpClient;

    public DaangnClient(
        DaangnCrawlerConfig config
    ) {

        if (
            config == null
        ) {

            throw new IllegalArgumentException(
                "config must not be null"
            );
        }

        this.config =
            config;

        this.httpClient =
            HttpClient.newBuilder()
                .connectTimeout(
                    config.connectTimeout()
                )
                .followRedirects(
                    HttpClient.Redirect.NORMAL
                )
                .build();
    }

    public String search(
        String keyword,
        String regionSlug
    ) {

        URI uri =
            buildSearchUri(
                keyword,
                regionSlug
            );

        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(
                    uri
                )
                .timeout(
                    config.requestTimeout()
                )
                .header(
                    "User-Agent",
                    config.userAgent()
                )
                .header(
                    "Accept",
                    "application/json, text/plain, */*"
                )
                .header(
                    "Accept-Language",
                    "ko-KR,ko;q=0.9,en;q=0.8"
                )
                .header(
                    "Referer",
                    config.searchUrl()
                )
                .GET()
                .build();

        try {

            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                    )
                );

            validateResponse(
                response,
                uri
            );

            return response.body();

        } catch (
            InterruptedException e
        ) {

            Thread.currentThread()
                .interrupt();

            throw new IllegalStateException(
                "Daangn request was interrupted.",
                e
            );

        } catch (
            IOException e
        ) {

            throw new IllegalStateException(
                "Failed to request Daangn. url="
                    + uri,
                e
            );
        }
    }

    public URI buildSearchUri(
        String keyword,
        String regionSlug
    ) {

        String normalizedKeyword =
            normalizeKeyword(
                keyword
            );

        String normalizedRegionSlug =
            normalizeRegionSlug(
                regionSlug
            );

        StringBuilder url =
            new StringBuilder(
                config.searchUrl()
            );

        url.append(
            "?search="
        );

        url.append(
            encode(
                normalizedKeyword
            )
        );

        url.append(
            "&only_on_sale=true"
        );

        if (
            normalizedRegionSlug != null
        ) {

            url.append(
                "&in="
            );

            url.append(
                encode(
                    normalizedRegionSlug
                )
            );
        }

        url.append(
            "&_data="
        );

        url.append(
            encode(
                SEARCH_DATA_ROUTE
            )
        );

        return URI.create(
            url.toString()
        );
    }

    private String normalizeKeyword(
        String keyword
    ) {

        if (
            keyword == null ||
            keyword.isBlank()
        ) {

            throw new IllegalArgumentException(
                "keyword must not be blank"
            );
        }

        return keyword.trim();
    }

    private String normalizeRegionSlug(
        String regionSlug
    ) {

        if (
            regionSlug == null
        ) {

            return null;
        }

        String normalized =
            regionSlug.trim();

        if (
            normalized.isBlank()
        ) {

            return null;
        }

        return normalized;
    }

    private void validateResponse(
        HttpResponse<String> response,
        URI uri
    ) {

        int statusCode =
            response.statusCode();

        if (
            statusCode < 200 ||
            statusCode >= 300
        ) {

            throw new IllegalStateException(
                "Daangn request failed. "
                    + "status="
                    + statusCode
                    + ", url="
                    + uri
            );
        }

        String body =
            response.body();

        if (
            body == null ||
            body.isBlank()
        ) {

            throw new IllegalStateException(
                "Daangn returned an empty response. "
                    + "url="
                    + uri
            );
        }
    }

    private String encode(
        String value
    ) {

        return URLEncoder.encode(
            value,
            StandardCharsets.UTF_8
        );
    }
}