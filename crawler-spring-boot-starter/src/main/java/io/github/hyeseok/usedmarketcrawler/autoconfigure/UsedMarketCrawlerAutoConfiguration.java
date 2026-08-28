package io.github.hyeseok.usedmarketcrawler.autoconfigure;

import io.github.hyeseok.usedmarketcrawler.core.DefaultUsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import io.github.hyeseok.usedmarketcrawler.daangn.DaangnUsedMarketProvider;
import io.github.hyeseok.usedmarketcrawler.daangn.client.DaangnClient;
import io.github.hyeseok.usedmarketcrawler.daangn.config.DaangnCrawlerConfig;
import io.github.hyeseok.usedmarketcrawler.daangn.mapper.DaangnItemMapper;
import io.github.hyeseok.usedmarketcrawler.daangn.parser.DaangnParser;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(
    DaangnCrawlerProperties.class
)
public class UsedMarketCrawlerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.daangn",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public DaangnCrawlerConfig daangnCrawlerConfig(
        DaangnCrawlerProperties properties
    ) {

        return new DaangnCrawlerConfig(
            properties.getBaseUrl(),
            properties.getConnectTimeout(),
            properties.getRequestTimeout(),
            properties.getUserAgent()
        );
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.daangn",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public DaangnClient daangnClient(
        DaangnCrawlerConfig config
    ) {

        return new DaangnClient(
            config
        );
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.daangn",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public DaangnParser daangnParser() {

        return new DaangnParser();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.daangn",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public DaangnItemMapper daangnItemMapper() {

        return new DaangnItemMapper();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.daangn",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean(
        DaangnUsedMarketProvider.class
    )
    public DaangnUsedMarketProvider
        daangnUsedMarketProvider(
            DaangnClient client,
            DaangnParser parser,
            DaangnItemMapper mapper
        ) {

        return new DaangnUsedMarketProvider(
            client,
            parser,
            mapper
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public UsedMarketCrawler usedMarketCrawler(
        List<UsedMarketProvider> providers
    ) {

        return new DefaultUsedMarketCrawler(
            providers
        );
    }
}