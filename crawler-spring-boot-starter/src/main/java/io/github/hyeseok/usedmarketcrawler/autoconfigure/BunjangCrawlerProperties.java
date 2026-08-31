package io.github.hyeseok.usedmarketcrawler.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(
    prefix = "used-market-crawler.bunjang"
)
public class BunjangCrawlerProperties {

    private boolean enabled =
        true;

    private String baseUrl =
        "https://api.bunjang.co.kr";

    private String webBaseUrl =
        "https://m.bunjang.co.kr";

    private Duration connectTimeout =
        Duration.ofSeconds(
            5
        );

    private Duration requestTimeout =
        Duration.ofSeconds(
            10
        );

    private String userAgent =
        "used-market-crawler/0.1";

    private int imageResolution =
        300;

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

    public String getWebBaseUrl() {
        return webBaseUrl;
    }

    public void setWebBaseUrl(
        String webBaseUrl
    ) {
        this.webBaseUrl =
            webBaseUrl;
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

    public int getImageResolution() {
        return imageResolution;
    }

    public void setImageResolution(
        int imageResolution
    ) {
        this.imageResolution =
            imageResolution;
    }
}