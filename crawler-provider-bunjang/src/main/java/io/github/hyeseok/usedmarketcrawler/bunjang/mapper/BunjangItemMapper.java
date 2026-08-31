package io.github.hyeseok.usedmarketcrawler.bunjang.mapper;

import io.github.hyeseok.usedmarketcrawler.bunjang.config.BunjangCrawlerConfig;
import io.github.hyeseok.usedmarketcrawler.bunjang.model.BunjangItem;
import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItemStatus;

public class BunjangItemMapper {

    private final BunjangCrawlerConfig config;

    public BunjangItemMapper(
        BunjangCrawlerConfig config
    ) {
        this.config =
            config;
    }

    public UsedItem map(
        BunjangItem source
    ) {

        return new UsedItem(
            String.valueOf(
                source.pid()
            ),
            MarketType.BUNJANG,
            source.name(),
            source.price(),

            /*
             * 번개장터 검색 API에서
             * 현재 지역 정보는 내려오지 않는다.
             */
            null,

            normalizeImageUrl(
                source.productImage()
            ),

            config.productUrl(
                source.pid()
            ),

            /*
             * 검색 API에는 description 없음.
             */
            null,

            mapStatus(
                source.status()
            ),

            /*
             * updatedAt은 상품 등록일이 아니라
             * 수정/갱신 시각일 가능성이 있으므로
             * publishedAt으로 사용하지 않는다.
             */
            null
        );
    }

    private UsedItemStatus mapStatus(
        String value
    ) {

        if (
            value == null
        ) {
            return UsedItemStatus.UNKNOWN;
        }

        return switch (
            value
        ) {

            case "SELLING" ->
                UsedItemStatus.AVAILABLE;

            case "RESERVED" ->
                UsedItemStatus.RESERVED;

            case "SOLD_OUT" ->
                UsedItemStatus.SOLD;

            default ->
                UsedItemStatus.UNKNOWN;
        };
    }

    private String normalizeImageUrl(
        String value
    ) {

        if (
            value == null
                || value.isBlank()
        ) {
            return null;
        }

        if (
            !value.contains(
                "{res}"
            )
        ) {
            return value;
        }

        return value.replace(
            "{res}",
            String.valueOf(
                config.imageResolution()
            )
        );
    }
}