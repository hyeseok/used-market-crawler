package io.github.hyeseok.usedmarketcrawler.bunjang.client;

import io.github.hyeseok.usedmarketcrawler.bunjang.config.BunjangCrawlerConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BunjangClient {

    private final BunjangCrawlerConfig config;

    private final HttpClient httpClient;

    public BunjangClient(
        BunjangCrawlerConfig config
    ) {
        this.config =
            config;

        this.httpClient =
            HttpClient
                .newBuilder()
                .connectTimeout(
                    config.connectTimeout()
                )
                .followRedirects(
                    HttpClient.Redirect.NORMAL
                )
                .build();
    }

    public String searchSpec(
        String keyword
    ) {

        String url =
            config.keywordSpecUrl()
                + "?q="
                + encode(
                    keyword
                );

        return get(
            url
        );
    }

    public String search(
        String keyword,
        String policyKey,
        String cursor
    ) {

        StringBuilder url =
            new StringBuilder(
                config.searchUrl()
            );

        url.append(
            "?policyKey="
        );

        url.append(
            encode(
                policyKey
            )
        );

        url.append(
            "&q="
        );

        url.append(
            encode(
                keyword
            )
        );

        /*
         * 실제 번개장터 응답은 size 파라미터와 무관하게
         * 현재 60개 단위 batch를 반환하고 있다.
         *
         * 따라서 request.limit을 여기의 size에 직접 연결하지 않는다.
         */
        url.append(
            "&size=60"
        );

        if (
            cursor != null
                && !cursor.isBlank()
        ) {

            url.append(
                "&cursor="
            );

            url.append(
                encode(
                    cursor
                )
            );
        }

        return get(
            url.toString()
        );
    }

    private String get(
        String url
    ) {

        HttpRequest request =
            HttpRequest
                .newBuilder()
                .uri(
                    URI.create(
                        url
                    )
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
                    "application/json"
                )
                .header(
                    "Accept-Language",
                    "ko-KR,ko;q=0.9"
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

            int statusCode =
                response.statusCode();

            if (
                statusCode < 200
                    || statusCode >= 300
            ) {

                throw new IllegalStateException(
                    "Bunjang request failed. "
                        + "status="
                        + statusCode
                        + ", url="
                        + url
                );
            }

            String body =
                response.body();

            if (
                body == null
                    || body.isBlank()
            ) {

                throw new IllegalStateException(
                    "Bunjang returned empty response. "
                        + "url="
                        + url
                );
            }

            return body;

        } catch (
            InterruptedException e
        ) {

            Thread.currentThread()
                .interrupt();

            throw new IllegalStateException(
                "Bunjang request interrupted.",
                e
            );

        } catch (
            IOException e
        ) {

            throw new IllegalStateException(
                "Bunjang request failed.",
                e
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