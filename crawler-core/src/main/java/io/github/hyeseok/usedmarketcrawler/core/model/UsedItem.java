package io.github.hyeseok.usedmarketcrawler.core.model;

import java.time.Instant;

public record UsedItem(

    String externalId,

    MarketType market,

    String title,

    Long price,

    String location,

    String imageUrl,

    String itemUrl,

    String description,

    UsedItemStatus status,

    Instant publishedAt

) {

    public UsedItem {

        if (
            market == null
        ) {
            throw new IllegalArgumentException(
                "market must not be null"
            );
        }

        if (
            title == null ||
            title.isBlank()
        ) {
            throw new IllegalArgumentException(
                "title must not be blank"
            );
        }

        if (
            status == null
        ) {
            status =
                UsedItemStatus.UNKNOWN;
        }
    }
}