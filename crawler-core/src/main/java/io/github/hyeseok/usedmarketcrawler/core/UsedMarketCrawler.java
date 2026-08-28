package io.github.hyeseok.usedmarketcrawler.core;

import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;

public interface UsedMarketCrawler {

    UsedMarketSearchResult search(
        UsedMarketSearchRequest request
    );
}