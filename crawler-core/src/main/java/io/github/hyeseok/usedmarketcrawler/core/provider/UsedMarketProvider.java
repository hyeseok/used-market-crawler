package io.github.hyeseok.usedmarketcrawler.core.provider;

import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketProviderResult;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;

public interface UsedMarketProvider {

    MarketType market();

    UsedMarketProviderResult search(
        UsedMarketSearchRequest request
    );
}