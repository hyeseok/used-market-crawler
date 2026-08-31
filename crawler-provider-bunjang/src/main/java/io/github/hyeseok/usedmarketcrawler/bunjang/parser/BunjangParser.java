package io.github.hyeseok.usedmarketcrawler.bunjang.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.hyeseok.usedmarketcrawler.bunjang.model.BunjangItem;
import io.github.hyeseok.usedmarketcrawler.bunjang.model.BunjangSearchPage;

import java.util.ArrayList;
import java.util.List;

public class BunjangParser {

    private static final String PRODUCT_TYPE =
        "PRODUCT";

    private static final String MAIN_GRID_BLOCK_TYPE =
        "productList.grid.main";

    private final ObjectMapper objectMapper;

    public BunjangParser() {
        this.objectMapper =
            new ObjectMapper();
    }

    public BunjangSearchPage parseSpec(
        String json
    ) {

        JsonNode root =
            readTree(
                json
            );

        JsonNode searchSpec =
            root
                .path("data")
                .path("searchSpec");

        if (
            searchSpec.isMissingNode()
                || searchSpec.isNull()
        ) {

            throw new IllegalStateException(
                "Bunjang searchSpec schema mismatch."
            );
        }

        String policyKey =
            nullableText(
                searchSpec,
                "policyKey"
            );

        if (
            policyKey == null
        ) {

            throw new IllegalStateException(
                "Bunjang policyKey was not found."
            );
        }

        JsonNode uiBlockList =
            searchSpec.path(
                "uiBlockList"
            );

        if (
            !uiBlockList.isArray()
        ) {

            throw new IllegalStateException(
                "Bunjang uiBlockList schema mismatch."
            );
        }

        for (
            JsonNode block
                : uiBlockList
        ) {

            String blockType =
                nullableText(
                    block,
                    "blockType"
                );

            if (
                !MAIN_GRID_BLOCK_TYPE.equals(
                    blockType
                )
            ) {
                continue;
            }

            JsonNode searchResponse =
                block.path(
                    "searchResponse"
                );

            return parseSearchResponse(
                policyKey,
                searchResponse
            );
        }

        /*
         * uiBlockList 자체는 정상인데 mainGrid가 없다면
         * 단순 0건이 아니라 schema 변화일 가능성이 높다.
         */
        throw new IllegalStateException(
            "Bunjang mainGrid block was not found."
        );
    }

    public BunjangSearchPage parseSearch(
        String json,
        String policyKey
    ) {

        JsonNode root =
            readTree(
                json
            );

        JsonNode searchResponse =
            root
                .path("data")
                .path("responses")
                .path("mainGrid")
                .path("searchResponse");

        if (
            searchResponse.isMissingNode()
                || searchResponse.isNull()
        ) {

            throw new IllegalStateException(
                "Bunjang search response schema mismatch."
            );
        }

        return parseSearchResponse(
            policyKey,
            searchResponse
        );
    }

    private BunjangSearchPage parseSearchResponse(
        String policyKey,
        JsonNode searchResponse
    ) {

        JsonNode data =
            searchResponse.path(
                "data"
            );

        if (
            !data.isArray()
        ) {

            throw new IllegalStateException(
                "Bunjang search data schema mismatch."
            );
        }

        List<BunjangItem> items =
            new ArrayList<>();

        for (
            JsonNode node
                : data
        ) {

            /*
             * 실제 응답에는 PRODUCT 외에
             * EXT_AD가 섞여 들어온다.
             *
             * 외부 광고는 crawler 결과에서 제외한다.
             */
            String type =
                nullableText(
                    node,
                    "type"
                );

            if (
                !PRODUCT_TYPE.equals(
                    type
                )
            ) {
                continue;
            }

            JsonNode pidNode =
                node.get(
                    "pid"
                );

            if (
                pidNode == null
                    || pidNode.isNull()
                    || !pidNode.canConvertToLong()
            ) {
                continue;
            }

            BunjangItem item =
                new BunjangItem(
                    pidNode.asLong(),
                    nullableText(
                        node,
                        "name"
                    ),
                    nullableLong(
                        node,
                        "price"
                    ),
                    nullableText(
                        node,
                        "status"
                    ),
                    nullableText(
                        node,
                        "productImage"
                    ),
                    nullableLong(
                        node.path("shop"),
                        "uid"
                    ),
                    nullableInteger(
                        node,
                        "favoriteCount"
                    ),
                    nullableInteger(
                        node,
                        "buntalkCount"
                    ),
                    nullableText(
                        node,
                        "updatedAt"
                    ),
                    node.path(
                        "care"
                    ).asBoolean(
                        false
                    ),
                    node.path(
                        "video"
                    ).asBoolean(
                        false
                    ),
                    node.path(
                        "ad"
                    ).asBoolean(
                        false
                    )
                );

            items.add(
                item
            );
        }

        return new BunjangSearchPage(
            policyKey,
            items,
            nullableText(
                searchResponse,
                "cursor"
            ),
            nullableText(
                searchResponse,
                "nextCursor"
            ),
            nullableLong(
                searchResponse,
                "totalCount"
            )
        );
    }

    private JsonNode readTree(
        String json
    ) {

        try {

            return objectMapper.readTree(
                json
            );

        } catch (
            Exception e
        ) {

            throw new IllegalStateException(
                "Failed to parse Bunjang JSON response.",
                e
            );
        }
    }

    private String nullableText(
        JsonNode node,
        String field
    ) {

        JsonNode value =
            node.get(
                field
            );

        if (
            value == null
                || value.isNull()
        ) {
            return null;
        }

        String text =
            value.asText();

        if (
            text == null
                || text.isBlank()
        ) {
            return null;
        }

        return text;
    }

    private Long nullableLong(
        JsonNode node,
        String field
    ) {

        JsonNode value =
            node.get(
                field
            );

        if (
            value == null
                || value.isNull()
                || !value.canConvertToLong()
        ) {
            return null;
        }

        return value.asLong();
    }

    private Integer nullableInteger(
        JsonNode node,
        String field
    ) {

        JsonNode value =
            node.get(
                field
            );

        if (
            value == null
                || value.isNull()
                || !value.canConvertToInt()
        ) {
            return null;
        }

        return value.asInt();
    }
}