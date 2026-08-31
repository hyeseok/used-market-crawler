package io.github.hyeseok.usedmarketcrawler.bunjang.provider;

import io.github.hyeseok.usedmarketcrawler.bunjang.client.BunjangClient;
import io.github.hyeseok.usedmarketcrawler.bunjang.mapper.BunjangItemMapper;
import io.github.hyeseok.usedmarketcrawler.bunjang.model.BunjangItem;
import io.github.hyeseok.usedmarketcrawler.bunjang.model.BunjangSearchPage;
import io.github.hyeseok.usedmarketcrawler.bunjang.parser.BunjangParser;
import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketProviderResult;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BunjangUsedMarketProvider
    implements UsedMarketProvider {

    private static final int MAX_PAGE_REQUESTS =
        20;

    private final BunjangClient client;

    private final BunjangParser parser;

    private final BunjangItemMapper mapper;

    public BunjangUsedMarketProvider(
        BunjangClient client,
        BunjangParser parser,
        BunjangItemMapper mapper
    ) {
        this.client =
            client;

        this.parser =
            parser;

        this.mapper =
            mapper;
    }

    @Override
    public MarketType market() {
        return MarketType.BUNJANG;
    }

    @Override
    public UsedMarketProviderResult search(
        UsedMarketSearchRequest request
    ) {

        try {

            return searchInternal(
                request
            );

        } catch (
            Exception e
        ) {

            return UsedMarketProviderResult.failure(
                MarketType.BUNJANG,
                safeMessage(
                    e
                )
            );
        }
    }

    private UsedMarketProviderResult searchInternal(
        UsedMarketSearchRequest request
    ) {

        int limit =
            request.limit();

        if (
            limit <= 0
        ) {

            return UsedMarketProviderResult.success(
                MarketType.BUNJANG,
                List.of()
            );
        }

        String keyword =
            request.keyword();

        String specJson =
            client.searchSpec(
                keyword
            );

        BunjangSearchPage firstPage =
            parser.parseSpec(
                specJson
            );

        String policyKey =
            firstPage.policyKey();

        List<UsedItem> results =
            new ArrayList<>();

        Set<Long> seenPids =
            new HashSet<>();

        appendItems(
            firstPage.items(),
            seenPids,
            results,
            limit
        );

        String nextCursor =
            firstPage.nextCursor();

        int pageRequests =
            1;

        while (
            results.size() < limit
                && nextCursor != null
                && !nextCursor.isBlank()
                && pageRequests < MAX_PAGE_REQUESTS
        ) {

            String currentCursor =
                nextCursor;

            String pageJson =
                client.search(
                    keyword,
                    policyKey,
                    currentCursor
                );

            BunjangSearchPage page =
                parser.parseSearch(
                    pageJson,
                    policyKey
                );

            appendItems(
                page.items(),
                seenPids,
                results,
                limit
            );

            nextCursor =
                page.nextCursor();

            pageRequests++;

            /*
             * API 이상으로 동일 cursor가 반복되면
             * 무한 루프를 방지한다.
             */
            if (
                currentCursor.equals(
                    nextCursor
                )
            ) {
                break;
            }
        }

        return UsedMarketProviderResult.success(
            MarketType.BUNJANG,
            results
        );
    }

    private void appendItems(
        List<BunjangItem> source,
        Set<Long> seenPids,
        List<UsedItem> target,
        int limit
    ) {

        for (
            BunjangItem item
                : source
        ) {

            if (
                target.size() >= limit
            ) {
                return;
            }

            Long pid =
                item.pid();

            if (
                pid == null
                    || !seenPids.add(
                        pid
                    )
            ) {
                continue;
            }

            target.add(
                mapper.map(
                    item
                )
            );
        }
    }

    private String safeMessage(
        Exception e
    ) {

        String message =
            e.getMessage();

        if (
            message == null
                || message.isBlank()
        ) {
            return e
                .getClass()
                .getSimpleName();
        }

        return message;
    }
}