package io.github.hyeseok.usedmarketcrawler.joongna.parser;

import io.github.hyeseok.usedmarketcrawler.joongna.model.JoongnaItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoongnaParser {

    private static final String BASE_URL =
        "https://web.joongna.com";

    /**
     * 실제 상품 URL만 허용한다.
     *
     * 허용:
     * /product/231432565
     *
     * 제외:
     * /product/form?type=regist
     */
    private static final Pattern PRODUCT_ID_PATTERN =
        Pattern.compile(
            "^/product/(\\d+)(?:[/?#].*)?$"
        );

    /**
     * 상품 카드 가격.
     *
     * 예:
     * 90,000 원
     * 630,000 원
     */
    private static final Pattern PRICE_PATTERN =
        Pattern.compile(
            "([\\d,]+)\\s*원"
        );

    public List<JoongnaItem> parseSearchResults(
        String html
    ) {
        if (
            html == null
                || html.isBlank()
        ) {
            return List.of();
        }

        Document document =
            Jsoup.parse(
                html,
                BASE_URL
            );

        Elements productLinks =
            document.select(
                "a[href^='/product/']"
            );

        Map<String, JoongnaItem>
            uniqueItems =
            new LinkedHashMap<>();

        for (
            Element productLink :
            productLinks
        ) {
            String href =
                productLink.attr(
                    "href"
                );

            String productId =
                extractProductId(
                    href
                );

            if (
                productId == null
            ) {
                continue;
            }

            JoongnaItem item =
                parseProduct(
                    productId,
                    href,
                    productLink
                );

            if (
                item == null
            ) {
                continue;
            }

            uniqueItems.putIfAbsent(
                productId,
                item
            );
        }

        return List.copyOf(
            new ArrayList<>(
                uniqueItems.values()
            )
        );
    }

    private JoongnaItem parseProduct(
        String productId,
        String href,
        Element productLink
    ) {
        String title =
            extractTitle(
                productLink
            );

        if (
            title == null
                || title.isBlank()
        ) {
            return null;
        }

        Long price =
            extractPrice(
                productLink
            );

        String imageUrl =
            extractImageUrl(
                productLink
            );

        String itemUrl =
            normalizeUrl(
                href
            );

        String status =
            extractStatus(
                productLink
            );

        return new JoongnaItem(
            productId,
            title,
            price,
            null,
            imageUrl,
            itemUrl,
            null,
            status,
            null
        );
    }

    /**
     * 현재 중고나라 검색 카드에서는
     *
     * <img
     *   alt="아이폰14프로 ... 이미지"
     * />
     *
     * 형태로 실제 상품명이 제공된다.
     *
     * a.text()는
     * 안심결제 / 가격 / 찜 / 시간 등의
     * 정보가 함께 들어오기 때문에
     * img alt를 우선 사용한다.
     */
    private String extractTitle(
        Element productLink
    ) {
        Element image =
            productLink.selectFirst(
                "img[alt]"
            );

        if (
            image != null
        ) {
            String alt =
                normalizeText(
                    image.attr(
                        "alt"
                    )
                );

            String title =
                removeImageSuffix(
                    alt
                );

            if (
                title != null
                    && !title.isBlank()
            ) {
                return title;
            }
        }

        /*
         * img alt 구조가 변경될 경우를 대비한
         * fallback.
         */
        String text =
            normalizeText(
                productLink.text()
            );

        return extractTitleFromText(
            text
        );
    }

    private String extractTitleFromText(
        String text
    ) {
        if (
            text == null
                || text.isBlank()
        ) {
            return null;
        }

        String normalized =
            text;

        /*
         * 카드 앞쪽의 안심결제 표시 제거.
         */
        normalized =
            normalized.replaceFirst(
                "^안심결제\\s*",
                ""
            );

        /*
         * 가격부터 뒤쪽은 제거.
         *
         * 예:
         *
         * 아이폰14프로 ... 630,000 원 1 1 -32308초 전
         *
         * ->
         *
         * 아이폰14프로 ...
         */
        Matcher matcher =
            PRICE_PATTERN.matcher(
                normalized
            );

        if (
            matcher.find()
        ) {
            normalized =
                normalized.substring(
                    0,
                    matcher.start()
                );
        }

        return normalizeText(
            normalized
        );
    }

    private Long extractPrice(
        Element productLink
    ) {
        String text =
            normalizeText(
                productLink.text()
            );

        if (
            text == null
                || text.isBlank()
        ) {
            return null;
        }

        Matcher matcher =
            PRICE_PATTERN.matcher(
                text
            );

        if (
            !matcher.find()
        ) {
            return null;
        }

        String rawPrice =
            matcher.group(
                    1
                )
                .replace(
                    ",",
                    ""
                );

        try {
            return Long.parseLong(
                rawPrice
            );
        } catch (
            NumberFormatException exception
        ) {
            return null;
        }
    }

    private String extractImageUrl(
        Element productLink
    ) {
        Element image =
            productLink.selectFirst(
                "img"
            );

        if (
            image == null
        ) {
            return null;
        }

        String src =
            firstNonBlank(
                image.attr(
                    "src"
                ),
                image.attr(
                    "data-src"
                )
            );

        if (
            src == null
        ) {
            return null;
        }

        if (
            src.startsWith(
                "//"
            )
        ) {
            return "https:"
                + src;
        }

        return src;
    }

    private String extractStatus(
        Element productLink
    ) {
        String text =
            normalizeText(
                productLink.text()
            );

        if (
            text == null
        ) {
            return "AVAILABLE";
        }

        if (
            text.contains(
                "판매완료"
            )
                || text.contains(
                    "거래완료"
                )
        ) {
            return "SOLD";
        }

        if (
            text.contains(
                "예약중"
            )
                || text.contains(
                    "예약중인 상품"
                )
        ) {
            return "RESERVED";
        }

        return "AVAILABLE";
    }

    private String extractProductId(
        String href
    ) {
        if (
            href == null
                || href.isBlank()
        ) {
            return null;
        }

        Matcher matcher =
            PRODUCT_ID_PATTERN.matcher(
                href.trim()
            );

        if (
            !matcher.matches()
        ) {
            return null;
        }

        return matcher.group(
            1
        );
    }

    private String normalizeUrl(
        String href
    ) {
        if (
            href == null
                || href.isBlank()
        ) {
            return null;
        }

        if (
            href.startsWith(
                "https://"
            )
                || href.startsWith(
                    "http://"
                )
        ) {
            return href;
        }

        if (
            href.startsWith(
                "/"
            )
        ) {
            return BASE_URL
                + href;
        }

        return BASE_URL
            + "/"
            + href;
    }

    private String removeImageSuffix(
        String value
    ) {
        String normalized =
            normalizeText(
                value
            );

        if (
            normalized == null
        ) {
            return null;
        }

        /*
         * 현재 alt:
         *
         * "아이폰14프로 ... 이미지"
         *
         * 실제 상품명:
         *
         * "아이폰14프로 ..."
         */
        normalized =
            normalized.replaceFirst(
                "\\s*이미지\\s*$",
                ""
            );

        return normalizeText(
            normalized
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
            value
                .replaceAll(
                    "\\s+",
                    " "
                )
                .trim();

        return normalized.isBlank()
            ? null
            : normalized;
    }

    private String firstNonBlank(
        String... values
    ) {
        if (
            values == null
        ) {
            return null;
        }

        for (
            String value :
            values
        ) {
            if (
                value != null
                    && !value.isBlank()
            ) {
                return value.trim();
            }
        }

        return null;
    }
}