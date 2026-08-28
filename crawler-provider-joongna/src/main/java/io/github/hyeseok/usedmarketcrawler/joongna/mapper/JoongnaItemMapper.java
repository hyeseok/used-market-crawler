package io.github.hyeseok.usedmarketcrawler.joongna.mapper;

import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItemStatus;
import io.github.hyeseok.usedmarketcrawler.joongna.model.JoongnaItem;

import java.time.Instant;
import java.time.OffsetDateTime;

public class JoongnaItemMapper {

    public UsedItem toUsedItem(
        JoongnaItem item
    ) {
        if (
            item == null
        ) {
            throw new IllegalArgumentException(
                "item must not be null"
            );
        }

        return new UsedItem(
            item.id(),
            MarketType.JOONGNA,
            item.title(),
            item.price(),
            item.location(),
            item.imageUrl(),
            item.itemUrl(),
            item.description(),
            mapStatus(
                item.status()
            ),
            parsePublishedAt(
                item.publishedAt()
            )
        );
    }

    private UsedItemStatus mapStatus(
        String status
    ) {
        if (
            status == null
                || status.isBlank()
        ) {
            return UsedItemStatus.UNKNOWN;
        }

        return switch (
            status.trim()
                .toUpperCase()
        ) {
            case
                "AVAILABLE",
                "SALE",
                "SELLING",
                "ON_SALE"
                ->
                UsedItemStatus.AVAILABLE;

            case
                "RESERVED"
                ->
                UsedItemStatus.RESERVED;

            case
                "SOLD",
                "SOLD_OUT",
                "COMPLETED"
                ->
                UsedItemStatus.SOLD;

            default ->
                UsedItemStatus.UNKNOWN;
        };
    }

    private Instant parsePublishedAt(
        String value
    ) {
        if (
            value == null
                || value.isBlank()
        ) {
            return null;
        }

        try {
            return Instant.parse(
                value
            );
        } catch (
            Exception ignored
        ) {
        }

        try {
            return OffsetDateTime
                .parse(
                    value
                )
                .toInstant();
        } catch (
            Exception ignored
        ) {
        }

        return null;
    }
}