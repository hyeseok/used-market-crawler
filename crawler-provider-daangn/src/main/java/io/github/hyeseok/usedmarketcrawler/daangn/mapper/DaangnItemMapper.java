package io.github.hyeseok.usedmarketcrawler.daangn.mapper;

import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItemStatus;
import io.github.hyeseok.usedmarketcrawler.daangn.model.DaangnItem;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class DaangnItemMapper {

    public UsedItem map(
        DaangnItem source
    ) {

        if (
            source == null
        ) {

            throw new IllegalArgumentException(
                "source must not be null"
            );
        }

        return new UsedItem(
            source.id(),
            MarketType.DAANGN,
            source.title(),
            source.price(),
            source.location(),
            source.imageUrl(),
            source.itemUrl(),
            source.description(),
            mapStatus(
                source.status()
            ),
            parseInstant(
                source.publishedAt()
            )
        );
    }

    private UsedItemStatus mapStatus(
        String status
    ) {

        if (
            status == null ||
            status.isBlank()
        ) {

            /*
             * 검색 endpoint에서 status를
             * 제공하지 않는 경우도 있으므로 UNKNOWN.
             */
            return UsedItemStatus.UNKNOWN;
        }

        String normalized =
            status.trim()
                .toLowerCase();

        return switch (
            normalized
        ) {

            case "ongoing",
                 "available",
                 "for_sale",
                 "onsale",
                 "on_sale",
                 "selling" ->
                UsedItemStatus.AVAILABLE;

            case "reserved",
                 "reservation",
                 "reserved_for_sale" ->
                UsedItemStatus.RESERVED;

            case "closed",
                 "sold",
                 "soldout",
                 "sold_out",
                 "completed" ->
                UsedItemStatus.SOLD;

            default ->
                UsedItemStatus.UNKNOWN;
        };
    }

    private Instant parseInstant(
        String value
    ) {

        if (
            value == null ||
            value.isBlank()
        ) {
            return null;
        }

        try {

            return Instant.parse(
                value
            );

        } catch (
            DateTimeParseException ignored
        ) {
        }

        try {

            return OffsetDateTime.parse(
                value
            ).toInstant();

        } catch (
            DateTimeParseException ignored
        ) {
        }

        return null;
    }
}