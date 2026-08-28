package io.github.hyeseok.usedmarketcrawler.core;

import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketProviderResult;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DefaultUsedMarketCrawler
    implements UsedMarketCrawler {

    private final List<UsedMarketProvider>
        providers;

    public DefaultUsedMarketCrawler(
        List<UsedMarketProvider> providers
    ) {

        if (
            providers == null
        ) {
            this.providers =
                List.of();

            return;
        }

        this.providers =
            List.copyOf(
                providers
            );
    }

    @Override
    public UsedMarketSearchResult search(
        UsedMarketSearchRequest request
    ) {

        if (
            request == null
        ) {
            throw new IllegalArgumentException(
                "request must not be null"
            );
        }

        List<UsedMarketProviderResult>
            providerResults =
            new ArrayList<>();

        List<UsedItem> items =
            new ArrayList<>();

        Set<MarketType> requestedMarkets =
            request.markets();

        for (
            UsedMarketProvider provider :
            providers
        ) {

            if (
                provider == null
            ) {
                continue;
            }

            MarketType market =
                provider.market();

            if (
                market == null
            ) {
                continue;
            }

            if (
                !requestedMarkets.isEmpty() &&
                !requestedMarkets.contains(
                    market
                )
            ) {
                continue;
            }

            UsedMarketProviderResult result;

            try {

                result =
                    provider.search(
                        request
                    );

                if (
                    result == null
                ) {
                    result =
                        UsedMarketProviderResult.failure(
                            market,
                            "Provider returned null result"
                        );
                }

            } catch (
                Exception e
            ) {

                result =
                    UsedMarketProviderResult.failure(
                        market,
                        getErrorMessage(
                            e
                        )
                    );
            }

            providerResults.add(
                result
            );

            if (
                result.success()
            ) {
                items.addAll(
                    result.items()
                );
            }
        }

        List<UsedItem> filteredItems =
            items.stream()
                .filter(
                    item ->
                        matchesPriceFilter(
                            item,
                            request
                        )
                )
                .filter(
                    item ->
                        matchesLocationFilter(
                            item,
                            request
                        )
                )
                .sorted(
                    Comparator.comparing(
                        UsedItem::publishedAt,
                        Comparator.nullsLast(
                            Comparator.reverseOrder()
                        )
                    )
                )
                .limit(
                    request.limit()
                )
                .toList();

        return new UsedMarketSearchResult(
            request.keyword(),
            filteredItems,
            providerResults
        );
    }

    private boolean matchesPriceFilter(
        UsedItem item,
        UsedMarketSearchRequest request
    ) {

        Long price =
            item.price();

        if (
            price == null
        ) {
            return true;
        }

        Long minPrice =
            request.minPrice();

        if (
            minPrice != null &&
            price < minPrice
        ) {
            return false;
        }

        Long maxPrice =
            request.maxPrice();

        if (
            maxPrice != null &&
            price > maxPrice
        ) {
            return false;
        }

        return true;
    }

    private boolean matchesLocationFilter(
        UsedItem item,
        UsedMarketSearchRequest request
    ) {

        String requestedLocation =
            request.location();

        if (
            requestedLocation == null
        ) {
            return true;
        }

        String itemLocation =
            item.location();

        if (
            itemLocation == null ||
            itemLocation.isBlank()
        ) {
            return false;
        }

        return itemLocation
            .toLowerCase()
            .contains(
                requestedLocation
                    .toLowerCase()
            );
    }

    private String getErrorMessage(
        Exception exception
    ) {

        String message =
            exception.getMessage();

        if (
            message == null ||
            message.isBlank()
        ) {
            return exception
                .getClass()
                .getSimpleName();
        }

        return message;
    }
}