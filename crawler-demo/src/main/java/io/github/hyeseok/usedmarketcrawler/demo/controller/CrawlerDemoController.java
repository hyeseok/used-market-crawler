package io.github.hyeseok.usedmarketcrawler.demo.controller;

import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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
            defaultValue = "20"
        )
        Integer limit
    ) {

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                Set.of(
                    MarketType.DAANGN
                ),
                minPrice,
                maxPrice,
                location,
                limit
            );

        return crawler.search(
            request
        );
    }
}