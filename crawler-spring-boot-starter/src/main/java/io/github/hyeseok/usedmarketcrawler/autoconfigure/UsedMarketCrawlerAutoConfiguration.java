package io.github.hyeseok.usedmarketcrawler.autoconfigure;

import io.github.hyeseok.usedmarketcrawler.core.DefaultUsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import io.github.hyeseok.usedmarketcrawler.daangn.DaangnUsedMarketProvider;
import io.github.hyeseok.usedmarketcrawler.daangn.client.DaangnClient;
import io.github.hyeseok.usedmarketcrawler.daangn.config.DaangnCrawlerConfig;
import io.github.hyeseok.usedmarketcrawler.daangn.mapper.DaangnItemMapper;
import io.github.hyeseok.usedmarketcrawler.daangn.parser.DaangnParser;

import io.github.hyeseok.usedmarketcrawler.joongna.client.JoongnaClient;
import io.github.hyeseok.usedmarketcrawler.joongna.config.JoongnaCrawlerConfig;
import io.github.hyeseok.usedmarketcrawler.joongna.mapper.JoongnaItemMapper;
import io.github.hyeseok.usedmarketcrawler.joongna.parser.JoongnaParser;
import io.github.hyeseok.usedmarketcrawler.joongna.provider.JoongnaUsedMarketProvider;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties({
    DaangnCrawlerProperties.class,
    JoongnaCrawlerProperties.class
})
public class UsedMarketCrawlerAutoConfiguration {

    /*
     * =========================================================
     * Daangn
     * =========================================================
     */

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

    /*
     * =========================================================
     * Joongna
     * =========================================================
     */

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.joongna",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public JoongnaCrawlerConfig joongnaCrawlerConfig(
        JoongnaCrawlerProperties properties
    ) {

        return new JoongnaCrawlerConfig(
            properties.getBaseUrl(),
            properties.getConnectTimeout(),
            properties.getRequestTimeout(),
            properties.getUserAgent()
        );
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.joongna",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public JoongnaClient joongnaClient(
        JoongnaCrawlerConfig config
    ) {

        return new JoongnaClient(
            config
        );
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.joongna",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public JoongnaParser joongnaParser() {

        return new JoongnaParser();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.joongna",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean
    public JoongnaItemMapper joongnaItemMapper() {

        return new JoongnaItemMapper();
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "used-market-crawler.joongna",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    @ConditionalOnMissingBean(
        JoongnaUsedMarketProvider.class
    )
    public JoongnaUsedMarketProvider
        joongnaUsedMarketProvider(
            JoongnaClient client,
            JoongnaParser parser,
            JoongnaItemMapper mapper
        ) {

        return new JoongnaUsedMarketProvider(
            client,
            parser,
            mapper
        );
    }

    /*
     * =========================================================
     * Aggregate crawler
     * =========================================================
     */

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