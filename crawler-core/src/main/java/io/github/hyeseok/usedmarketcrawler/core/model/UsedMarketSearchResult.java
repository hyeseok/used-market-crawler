package io.github.hyeseok.usedmarketcrawler.core.model;

import java.util.List;

public record UsedMarketSearchResult(

    String keyword,

    List<UsedItem> items,

    List<UsedMarketProviderResult> providers

) {

    public UsedMarketSearchResult {

        if (
            keyword == null
        ) {
            keyword =
                "";
        }

        if (
            items == null
        ) {
            items =
                List.of();
        } else {
            items =
                List.copyOf(
                    items
                );
        }

        if (
            providers == null
        ) {
            providers =
                List.of();
        } else {
            providers =
                List.copyOf(
                    providers
                );
        }
    }

    public int totalCount() {

        return items.size();
    }

    public int successProviderCount() {

        return (int) providers.stream()
            .filter(
                UsedMarketProviderResult::success
            )
            .count();
    }

    public int failureProviderCount() {

        return (int) providers.stream()
            .filter(
                result ->
                    !result.success()
            )
            .count();
    }
}