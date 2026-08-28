package io.github.hyeseok.usedmarketcrawler.autoconfigure;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "used-market-crawler.daangn"
)
public class DaangnCrawlerProperties {

    private boolean enabled =
        true;

    private String baseUrl =
        "https://www.daangn.com";

    private Duration connectTimeout =
        Duration.ofSeconds(
            5
        );

    private Duration requestTimeout =
        Duration.ofSeconds(
            10
        );

    private String userAgent =
        "used-market-crawler/0.1 "
            + "(+https://github.com/hyeseok/used-market-crawler)";

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(
        boolean enabled
    ) {

        this.enabled =
            enabled;
    }

    public String getBaseUrl() {

        return baseUrl;
    }

    public void setBaseUrl(
        String baseUrl
    ) {

        this.baseUrl =
            baseUrl;
    }

    public Duration getConnectTimeout() {

        return connectTimeout;
    }

    public void setConnectTimeout(
        Duration connectTimeout
    ) {

        this.connectTimeout =
            connectTimeout;
    }

    public Duration getRequestTimeout() {

        return requestTimeout;
    }

    public void setRequestTimeout(
        Duration requestTimeout
    ) {

        this.requestTimeout =
            requestTimeout;
    }

    public String getUserAgent() {

        return userAgent;
    }

    public void setUserAgent(
        String userAgent
    ) {

        this.userAgent =
            userAgent;
    }
}