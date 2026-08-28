package io.github.hyeseok.usedmarketcrawler.demo.controller;

import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
    "/api/v1/crawler"
)
public class CrawlerDemoController {

    private final UsedMarketCrawler crawler;

    public CrawlerDemoController(
        UsedMarketCrawler crawler
    ) {

        this.crawler =
            crawler;
    }

    @GetMapping(
        "/search"
    )
    public UsedMarketSearchResult search(
        @RequestParam
        String keyword,

        @RequestParam(
            required = false
        )
        String location,

        @RequestParam(
            required = false
        )
        Long minPrice,

        @RequestParam(
            required = false
        )
        Long maxPrice,

        @RequestParam(
            required = false
        )
        String markets,

        @RequestParam(
            defaultValue = "20"
        )
        Integer limit
    ) {

        Set<MarketType> marketTypes =
            parseMarkets(
                markets
            );

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                marketTypes,
                minPrice,
                maxPrice,
                location,
                limit
            );

        return crawler.search(
            request
        );
    }

    private Set<MarketType> parseMarkets(
        String markets
    ) {

        /*
         * markets 파라미터가 없으면
         * 전체 Provider를 조회한다.
         *
         * DefaultUsedMarketCrawler에서
         * markets가 비어 있으면
         * 등록된 모든 Provider를 실행한다.
         */
        if (
            markets == null
                || markets.isBlank()
        ) {
            return Set.of();
        }

        try {
            return Arrays.stream(
                    markets.split(
                        ","
                    )
                )
                .map(
                    String::trim
                )
                .filter(
                    value ->
                        !value.isBlank()
                )
                .map(
                    String::toUpperCase
                )
                .map(
                    MarketType::valueOf
                )
                .collect(
                    Collectors.toUnmodifiableSet()
                );
        } catch (
            IllegalArgumentException exception
        ) {
            throw new IllegalArgumentException(
                "Unsupported market. "
                    + "Available markets: "
                    + Arrays.toString(
                        MarketType.values()
                    ),
                exception
            );
        }
    }
}