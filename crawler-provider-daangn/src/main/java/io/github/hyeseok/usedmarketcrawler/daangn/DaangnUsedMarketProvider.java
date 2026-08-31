package io.github.hyeseok.usedmarketcrawler.daangn;

import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketProviderResult;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import io.github.hyeseok.usedmarketcrawler.daangn.client.DaangnClient;
import io.github.hyeseok.usedmarketcrawler.daangn.mapper.DaangnItemMapper;
import io.github.hyeseok.usedmarketcrawler.daangn.model.DaangnItem;
import io.github.hyeseok.usedmarketcrawler.daangn.parser.DaangnParser;

import java.util.List;

public class DaangnUsedMarketProvider
    implements UsedMarketProvider {

    private final DaangnClient client;

    private final DaangnParser parser;

    private final DaangnItemMapper mapper;

    public DaangnUsedMarketProvider(
        DaangnClient client,
        DaangnParser parser,
        DaangnItemMapper mapper
    ) {

        if (
            client == null
        ) {
            throw new IllegalArgumentException(
                "client must not be null"
            );
        }

        if (
            parser == null
        ) {
            throw new IllegalArgumentException(
                "parser must not be null"
            );
        }

        if (
            mapper == null
        ) {
            throw new IllegalArgumentException(
                "mapper must not be null"
            );
        }

        this.client =
            client;

        this.parser =
            parser;

        this.mapper =
            mapper;
    }

    @Override
    public MarketType market() {

        return MarketType.DAANGN;
    }

    @Override
    public UsedMarketProviderResult search(
        UsedMarketSearchRequest request
    ) {

        if (
            request == null
        ) {

            throw new IllegalArgumentException(
                "request must not be null"
            );
        }

        String regionSlug =
            resolveRegionSlug(
                request.location()
            );

        String payload =
            client.search(
                request.keyword(),
                regionSlug
            );

        List<DaangnItem> parsedItems =
            parser.parseSearchResults(
                payload
            );

        List<UsedItem> items =
            parsedItems.stream()
                .map(
                    mapper::map
                )
                .toList();

        return UsedMarketProviderResult.success(
            market(),
            items
        );
    }

    private String resolveRegionSlug(
        String location
    ) {

        if (
            location == null ||
            location.isBlank()
        ) {
            return null;
        }

        String normalized =
            location.trim();

        /*
         * 현재 단계에서는 이미 해석된
         * 당근 region slug만 직접 사용한다.
         *
         * 예:
         * 강남구-386
         * 역삼동-6035
         *
         * 다음 단계에서
         * "역삼동" → region API → slug
         * 자동 변환을 추가한다.
         */
        if (
            normalized.matches(
                "^.+-\\d+$"
            )
        ) {

            return normalized;
        }

        return null;
    }
}