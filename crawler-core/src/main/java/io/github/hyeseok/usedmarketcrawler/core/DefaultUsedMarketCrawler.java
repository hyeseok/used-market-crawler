package io.github.hyeseok.usedmarketcrawler.core;

import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketProviderResult;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DefaultUsedMarketCrawler implements UsedMarketCrawler {

    private final List<UsedMarketProvider> providers;

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
            providers.stream()
                .filter(
                    Objects::nonNull
                )
                .toList();
    }

    @Override
    public UsedMarketSearchResult search(
        UsedMarketSearchRequest request
    ) {
        validateRequest(
            request
        );

        List<UsedMarketProviderResult>
            providerResults =
            executeProviders(
                request
            );

        List<UsedItem>
            items =
            providerResults.stream()
                .filter(
                    UsedMarketProviderResult::success
                )
                .flatMap(
                    result ->
                        result.items()
                            .stream()
                )
                .filter(
                    Objects::nonNull
                )
                .filter(
                    item ->
                        matchesPrice(
                            item,
                            request
                        )
                )
                .sorted(
                    publishedAtComparator()
                )
                .limit(
                    request.limit()
                )
                .toList();

        return new UsedMarketSearchResult(
            request.keyword(),
            items,
            providerResults
        );
    }

    private List<UsedMarketProviderResult>
        executeProviders(
            UsedMarketSearchRequest request
        ) {

        List<UsedMarketProviderResult>
            results =
            new ArrayList<>();

        for (
            UsedMarketProvider provider :
            providers
        ) {
            if (
                !shouldSearchProvider(
                    provider,
                    request
                )
            ) {
                continue;
            }

            try {
                UsedMarketProviderResult
                    result =
                    provider.search(
                        request
                    );

                if (
                    result == null
                ) {
                    results.add(
                        UsedMarketProviderResult.failure(
                            provider.market(),
                            "Provider returned null result."
                        )
                    );

                    continue;
                }

                results.add(
                    result
                );
            } catch (
                Exception exception
            ) {
                results.add(
                    UsedMarketProviderResult.failure(
                        provider.market(),
                        buildProviderErrorMessage(
                            exception
                        )
                    )
                );
            }
        }

        return List.copyOf(
            results
        );
    }

    private boolean shouldSearchProvider(
        UsedMarketProvider provider,
        UsedMarketSearchRequest request
    ) {
        if (
            provider == null
        ) {
            return false;
        }

        if (
            request.markets() == null
                || request.markets()
                    .isEmpty()
        ) {
            return true;
        }

        return request.markets()
            .contains(
                provider.market()
            );
    }

    private boolean matchesPrice(
        UsedItem item,
        UsedMarketSearchRequest request
    ) {
        Long price =
            item.price();

        if (
            price == null
        ) {
            return request.minPrice()
                    == null
                && request.maxPrice()
                    == null;
        }

        Long minPrice =
            request.minPrice();

        if (
            minPrice != null
                && price < minPrice
        ) {
            return false;
        }

        Long maxPrice =
            request.maxPrice();

        if (
            maxPrice != null
                && price > maxPrice
        ) {
            return false;
        }

        return true;
    }

    private Comparator<UsedItem>
        publishedAtComparator() {

        return Comparator.comparing(
            UsedItem::publishedAt,
            Comparator.nullsLast(
                Comparator.reverseOrder()
            )
        );
    }

    private void validateRequest(
        UsedMarketSearchRequest request
    ) {
        if (
            request == null
        ) {
            throw new IllegalArgumentException(
                "request must not be null"
            );
        }

        if (
            request.keyword() == null
                || request.keyword()
                    .isBlank()
        ) {
            throw new IllegalArgumentException(
                "keyword must not be blank"
            );
        }

        if (
            request.limit() <= 0
        ) {
            throw new IllegalArgumentException(
                "limit must be greater than 0"
            );
        }

        if (
            request.minPrice()
                != null
                && request.minPrice() < 0
        ) {
            throw new IllegalArgumentException(
                "minPrice must be greater than or equal to 0"
            );
        }

        if (
            request.maxPrice()
                != null
                && request.maxPrice() < 0
        ) {
            throw new IllegalArgumentException(
                "maxPrice must be greater than or equal to 0"
            );
        }

        if (
            request.minPrice()
                != null
                && request.maxPrice()
                    != null
                && request.minPrice()
                    > request.maxPrice()
        ) {
            throw new IllegalArgumentException(
                "minPrice must be less than or equal to maxPrice"
            );
        }
    }

    private String buildProviderErrorMessage(
        Exception exception
    ) {
        if (
            exception == null
        ) {
            return "Unknown provider error.";
        }

        String message =
            exception.getMessage();

        if (
            message == null
                || message.isBlank()
        ) {
            return exception
                .getClass()
                .getSimpleName();
        }

        return message;
    }
}