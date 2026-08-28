package io.github.hyeseok.usedmarketcrawler.core.model;

import java.util.Set;

public record UsedMarketSearchRequest(

    String keyword,

    Set<MarketType> markets,

    Long minPrice,

    Long maxPrice,

    String location,

    Integer limit

) {

    private static final int DEFAULT_LIMIT =
        50;

    private static final int MAX_LIMIT =
        200;

    public UsedMarketSearchRequest {

        if (
            keyword == null ||
            keyword.isBlank()
        ) {
            throw new IllegalArgumentException(
                "keyword must not be blank"
            );
        }

        keyword =
            keyword.trim();

        if (
            markets == null
        ) {
            markets =
                Set.of();
        } else {
            markets =
                Set.copyOf(
                    markets
                );
        }

        if (
            minPrice != null &&
            minPrice < 0
        ) {
            throw new IllegalArgumentException(
                "minPrice must be greater than or equal to 0"
            );
        }

        if (
            maxPrice != null &&
            maxPrice < 0
        ) {
            throw new IllegalArgumentException(
                "maxPrice must be greater than or equal to 0"
            );
        }

        if (
            minPrice != null &&
            maxPrice != null &&
            minPrice > maxPrice
        ) {
            throw new IllegalArgumentException(
                "minPrice must be less than or equal to maxPrice"
            );
        }

        if (
            location != null
        ) {
            location =
                location.trim();

            if (
                location.isBlank()
            ) {
                location =
                    null;
            }
        }

        if (
            limit == null
        ) {
            limit =
                DEFAULT_LIMIT;
        }

        if (
            limit <= 0
        ) {
            throw new IllegalArgumentException(
                "limit must be greater than 0"
            );
        }

        if (
            limit > MAX_LIMIT
        ) {
            limit =
                MAX_LIMIT;
        }
    }

    public static UsedMarketSearchRequest of(
        String keyword
    ) {

        return new UsedMarketSearchRequest(
            keyword,
            Set.of(),
            null,
            null,
            null,
            DEFAULT_LIMIT
        );
    }
}