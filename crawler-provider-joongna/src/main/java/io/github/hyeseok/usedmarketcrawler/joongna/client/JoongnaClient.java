package io.github.hyeseok.usedmarketcrawler.joongna.client;

import io.github.hyeseok.usedmarketcrawler.joongna.config.JoongnaCrawlerConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class JoongnaClient {

    private final JoongnaCrawlerConfig config;

    private final HttpClient httpClient;

    public JoongnaClient(
        JoongnaCrawlerConfig config
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
        String keyword
    ) {
        URI uri =
            buildSearchUri(
                keyword
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
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
                )
                .header(
                    "Accept-Language",
                    "ko-KR,ko;q=0.9"
                )
                .GET()
                .build();

        try {
            HttpResponse<String>
                response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers
                        .ofString()
                );

            int statusCode =
                response.statusCode();

            if (
                statusCode < 200
                    || statusCode >= 300
            ) {
                throw new IllegalStateException(
                    "Joongna search request failed. status="
                        + statusCode
                );
            }

            String body =
                response.body();

            if (
                body == null
                    || body.isBlank()
            ) {
                throw new IllegalStateException(
                    "Joongna search response body is empty."
                );
            }

            return body;
        } catch (
            IOException exception
        ) {
            throw new IllegalStateException(
                "Failed to request Joongna.",
                exception
            );
        } catch (
            InterruptedException exception
        ) {
            Thread.currentThread()
                .interrupt();

            throw new IllegalStateException(
                "Joongna request interrupted.",
                exception
            );
        }
    }

    public URI buildSearchUri(
        String keyword
    ) {
        if (
            keyword == null
                || keyword.isBlank()
        ) {
            throw new IllegalArgumentException(
                "keyword must not be blank"
            );
        }

        String encodedKeyword =
            URLEncoder.encode(
                    keyword.trim(),
                    StandardCharsets.UTF_8
                )
                .replace(
                    "+",
                    "%20"
                );

        return URI.create(
            config.searchUrl()
                + encodedKeyword
        );
    }
}