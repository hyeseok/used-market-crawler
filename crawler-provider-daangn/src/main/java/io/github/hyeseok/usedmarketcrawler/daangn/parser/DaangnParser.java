package io.github.hyeseok.usedmarketcrawler.daangn.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.hyeseok.usedmarketcrawler.daangn.model.DaangnItem;

import java.util.ArrayList;
import java.util.List;

public class DaangnParser {

    private static final String BASE_URL =
        "https://www.daangn.com";

    private final ObjectMapper objectMapper;

    public DaangnParser() {

        this(
            new ObjectMapper()
        );
    }

    public DaangnParser(
        ObjectMapper objectMapper
    ) {

        if (
            objectMapper == null
        ) {

            throw new IllegalArgumentException(
                "objectMapper must not be null"
            );
        }

        this.objectMapper =
            objectMapper;
    }

    /**
     * 당근 중고거래 검색 _data 응답을 파싱한다.
     *
     * 현재 확인된 구조:
     *
     * {
     *   "allPage": {
     *     "fleamarketArticles": [
     *       ...
     *     ]
     *   }
     * }
     */
    public List<DaangnItem> parseSearchResults(
        String payload
    ) {

        if (
            payload == null ||
            payload.isBlank()
        ) {

            return List.of();
        }

        JsonNode root;

        try {

            root =
                objectMapper.readTree(
                    payload
                );

        } catch (
            Exception e
        ) {

            throw new IllegalStateException(
                "Failed to parse Daangn search response.",
                e
            );
        }

        JsonNode allPage =
            root.get(
                "allPage"
            );

        if (
            allPage == null ||
            !allPage.isObject()
        ) {

            return List.of();
        }

        JsonNode articles =
            allPage.get(
                "fleamarketArticles"
            );

        if (
            articles == null ||
            !articles.isArray()
        ) {

            return List.of();
        }

        List<DaangnItem> result =
            new ArrayList<>();

        for (
            JsonNode article :
            articles
        ) {

            DaangnItem item =
                parseArticle(
                    article
                );

            if (
                item != null
            ) {

                result.add(
                    item
                );
            }
        }

        return List.copyOf(
            result
        );
    }

    private DaangnItem parseArticle(
        JsonNode article
    ) {

        if (
            article == null ||
            !article.isObject()
        ) {

            return null;
        }

        String title =
            text(
                article,
                "title"
            );

        if (
            title == null ||
            title.isBlank()
        ) {

            return null;
        }

        String href =
            firstText(
                article,
                "href",
                "webUrl"
            );

        String itemUrl =
            normalizeUrl(
                href
            );

        String externalId =
            firstText(
                article,
                "id",
                "articleId"
            );

        if (
            externalId == null
        ) {

            externalId =
                extractArticleId(
                    itemUrl
                );
        }

        Long price =
            parsePrice(
                article.get(
                    "price"
                )
            );

        String location =
            extractRegionName(
                article
            );

        String imageUrl =
            extractImageUrl(
                article
            );

        String description =
            firstText(
                article,
                "description",
                "content"
            );

        String status =
            text(
                article,
                "status"
            );

        String publishedAt =
            firstText(
                article,
                "createdAt",
                "publishedAt"
            );

        return new DaangnItem(
            externalId,
            title,
            price,
            location,
            imageUrl,
            itemUrl,
            description,
            status,
            publishedAt
        );
    }

    private String extractRegionName(
        JsonNode article
    ) {

        JsonNode region =
            article.get(
                "region"
            );

        if (
            region == null ||
            region.isNull()
        ) {

            return firstText(
                article,
                "regionName",
                "locationName"
            );
        }

        if (
            region.isTextual()
        ) {

            return normalizeText(
                region.asText()
            );
        }

        if (
            region.isObject()
        ) {

            return firstText(
                region,
                "name",
                "displayName"
            );
        }

        return null;
    }

    private String extractImageUrl(
        JsonNode article
    ) {

        String direct =
            firstText(
                article,
                "thumbnail",
                "thumbnailUrl",
                "imageUrl"
            );

        if (
            direct != null
        ) {

            return direct;
        }

        JsonNode images =
            article.get(
                "images"
            );

        if (
            images == null ||
            !images.isArray() ||
            images.isEmpty()
        ) {

            return null;
        }

        JsonNode first =
            images.get(
                0
            );

        if (
            first == null
        ) {

            return null;
        }

        if (
            first.isTextual()
        ) {

            return normalizeText(
                first.asText()
            );
        }

        if (
            first.isObject()
        ) {

            return firstText(
                first,
                "url",
                "imageUrl",
                "src"
            );
        }

        return null;
    }

    private Long parsePrice(
        JsonNode node
    ) {

        if (
            node == null ||
            node.isNull()
        ) {

            return null;
        }

        if (
            node.isIntegralNumber()
        ) {

            return node.longValue();
        }

        if (
            node.isFloatingPointNumber()
        ) {

            return node.longValue();
        }

        if (
            node.isObject()
        ) {

            JsonNode value =
                firstNode(
                    node,
                    "value",
                    "amount",
                    "price"
                );

            if (
                value != null &&
                value != node
            ) {

                return parsePrice(
                    value
                );
            }
        }

        String text =
            node.asText();

        if (
            text == null ||
            text.isBlank()
        ) {

            return null;
        }

        if (
            text.contains(
                "나눔"
            )
        ) {

            return 0L;
        }

        String normalized =
            text.replaceAll(
                "[^0-9]",
                ""
            );

        if (
            normalized.isBlank()
        ) {

            return null;
        }

        try {

            return Long.parseLong(
                normalized
            );

        } catch (
            NumberFormatException e
        ) {

            return null;
        }
    }

    private JsonNode firstNode(
        JsonNode node,
        String... fields
    ) {

        if (
            node == null ||
            !node.isObject()
        ) {

            return null;
        }

        for (
            String field :
            fields
        ) {

            JsonNode value =
                node.get(
                    field
                );

            if (
                value != null &&
                !value.isNull()
            ) {

                return value;
            }
        }

        return null;
    }

    private String firstText(
        JsonNode node,
        String... fields
    ) {

        JsonNode value =
            firstNode(
                node,
                fields
            );

        if (
            value == null ||
            !value.isValueNode()
        ) {

            return null;
        }

        return normalizeText(
            value.asText()
        );
    }

    private String text(
        JsonNode node,
        String field
    ) {

        if (
            node == null
        ) {

            return null;
        }

        JsonNode value =
            node.get(
                field
            );

        if (
            value == null ||
            value.isNull() ||
            !value.isValueNode()
        ) {

            return null;
        }

        return normalizeText(
            value.asText()
        );
    }

    private String normalizeText(
        String value
    ) {

        if (
            value == null
        ) {

            return null;
        }

        String normalized =
            value.trim();

        if (
            normalized.isBlank()
        ) {

            return null;
        }

        return normalized;
    }

    private String normalizeUrl(
        String value
    ) {

        if (
            value == null ||
            value.isBlank()
        ) {

            return null;
        }

        String normalized =
            value.trim();

        if (
            normalized.startsWith(
                "https://"
            ) ||
            normalized.startsWith(
                "http://"
            )
        ) {

            return normalized;
        }

        if (
            normalized.startsWith(
                "/"
            )
        ) {

            return BASE_URL
                + normalized;
        }

        return BASE_URL
            + "/"
            + normalized;
    }

    private String extractArticleId(
        String url
    ) {

        if (
            url == null ||
            url.isBlank()
        ) {

            return null;
        }

        String normalized =
            url;

        int queryIndex =
            normalized.indexOf(
                '?'
            );

        if (
            queryIndex >= 0
        ) {

            normalized =
                normalized.substring(
                    0,
                    queryIndex
                );
        }

        while (
            normalized.endsWith(
                "/"
            )
        ) {

            normalized =
                normalized.substring(
                    0,
                    normalized.length() - 1
                );
        }

        int slashIndex =
            normalized.lastIndexOf(
                '/'
            );

        if (
            slashIndex < 0 ||
            slashIndex ==
                normalized.length() - 1
        ) {

            return null;
        }

        String slug =
            normalized.substring(
                slashIndex + 1
            );

        int dashIndex =
            slug.lastIndexOf(
                '-'
            );

        if (
            dashIndex >= 0 &&
            dashIndex <
                slug.length() - 1
        ) {

            return slug.substring(
                dashIndex + 1
            );
        }

        return slug;
    }
}