package io.github.hyeseok.usedmarketcrawler.joongna.provider;

import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketProviderResult;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.provider.UsedMarketProvider;

import io.github.hyeseok.usedmarketcrawler.joongna.client.JoongnaClient;
import io.github.hyeseok.usedmarketcrawler.joongna.mapper.JoongnaItemMapper;
import io.github.hyeseok.usedmarketcrawler.joongna.model.JoongnaItem;
import io.github.hyeseok.usedmarketcrawler.joongna.parser.JoongnaParser;

import java.util.List;

public class JoongnaUsedMarketProvider
    implements UsedMarketProvider {

    private final JoongnaClient client;
    private final JoongnaParser parser;
    private final JoongnaItemMapper mapper;

    public JoongnaUsedMarketProvider(
        JoongnaClient client,
        JoongnaParser parser,
        JoongnaItemMapper mapper
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

        return MarketType.JOONGNA;
    }

    @Override
    public UsedMarketProviderResult search(
        UsedMarketSearchRequest request
    ) {
        try {
            String html =
                client.search(
                    request.keyword()
                );

            List<JoongnaItem> parsedItems =
                parser.parseSearchResults(
                    html
                );

            List<UsedItem> items =
                parsedItems.stream()
                    .map(
                        mapper::toUsedItem
                    )
                    .toList();

            return UsedMarketProviderResult.success(
                MarketType.JOONGNA,
                items
            );
        } catch (
            Exception exception
        ) {
            return UsedMarketProviderResult.failure(
                MarketType.JOONGNA,
                buildErrorMessage(
                    exception
                )
            );
        }
    }

    private String buildErrorMessage(
        Exception exception
    ) {
        if (
            exception == null
        ) {
            return "Unknown Joongna provider error.";
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