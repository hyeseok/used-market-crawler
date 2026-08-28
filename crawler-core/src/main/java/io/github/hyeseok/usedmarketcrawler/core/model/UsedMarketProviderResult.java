package io.github.hyeseok.usedmarketcrawler.core.model;

import java.util.List;

public record UsedMarketProviderResult(

    MarketType market,

    List<UsedItem> items,

    boolean success,

    String errorMessage

) {

    public UsedMarketProviderResult {

        if (
            market == null
        ) {
            throw new IllegalArgumentException(
                "market must not be null"
            );
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
            success
        ) {
            errorMessage =
                null;
        }
    }

    public static UsedMarketProviderResult success(
        MarketType market,
        List<UsedItem> items
    ) {

        return new UsedMarketProviderResult(
            market,
            items,
            true,
            null
        );
    }

    public static UsedMarketProviderResult failure(
        MarketType market,
        String errorMessage
    ) {

        String normalizedMessage =
            errorMessage;

        if (
            normalizedMessage == null ||
            normalizedMessage.isBlank()
        ) {
            normalizedMessage =
                "Unknown provider error";
        }

        return new UsedMarketProviderResult(
            market,
            List.of(),
            false,
            normalizedMessage
        );
    }
}