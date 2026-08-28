# Adding a New Provider

`used-market-crawler`에 당근(`DAANGN`), 중고나라(`JOONGNA`) 외의 새로운 중고거래 서비스를 추가하는 방법을 설명합니다.

예:

```text
BUNJANG
OTHER MARKET
```

---

# Architecture Principle

새로운 마켓을 추가할 때 가장 중요한 원칙은:

> Provider별 특수 로직을 Core에 넣지 않는다.

입니다.

전체 구조:

```text
UsedMarketCrawler
        ↓
UsedMarketProvider
        ↓
Provider Client
        ↓
Provider Parser
        ↓
Provider Raw Item
        ↓
Provider Item Mapper
        ↓
UsedItem
```

각 Provider가 자신의 사이트 구조를 책임집니다.

---

# Example Provider Structure

예를 들어 번개장터 Provider를 추가한다고 가정합니다.

```text
crawler-provider-bunjang/
└── src/main/java/
    └── io/github/hyeseok/usedmarketcrawler/bunjang/
        ├── client/
        │   └── BunjangClient.java
        ├── config/
        │   └── BunjangCrawlerConfig.java
        ├── mapper/
        │   └── BunjangItemMapper.java
        ├── model/
        │   └── BunjangItem.java
        ├── parser/
        │   └── BunjangParser.java
        └── provider/
            └── BunjangUsedMarketProvider.java
```

---

# Step 1. Add MarketType

파일:

```text
crawler-core/src/main/java/
io/github/hyeseok/usedmarketcrawler/core/model/MarketType.java
```

예:

```java
public enum MarketType {

    DAANGN,

    JOONGNA,

    BUNJANG
}
```

Core에 추가해야 하는 사이트별 정보는 가능한 한 이것뿐이어야 합니다.

Core에 다음 정보를 넣지 않습니다.

```text
Bunjang URL
Bunjang HTML selector
Bunjang region ID
Bunjang API schema
Bunjang status string
```

---

# Step 2. Add Gradle Module

루트:

```text
settings.gradle
```

에 추가합니다.

```gradle
include 'crawler-provider-bunjang'
```

전체 예:

```gradle
rootProject.name = 'used-market-crawler'

include 'crawler-core'
include 'crawler-provider-daangn'
include 'crawler-provider-joongna'
include 'crawler-provider-bunjang'
include 'crawler-spring-boot-starter'
include 'crawler-demo'
```

---

# Step 3. Provider build.gradle

생성:

```text
crawler-provider-bunjang/build.gradle
```

HTML + JSON 파싱이 모두 필요하다고 가정하면:

```gradle
dependencies {
    api project(':crawler-core')

    implementation 'org.jsoup:jsoup:1.21.2'

    implementation 'com.fasterxml.jackson.core:jackson-databind:2.20.1'

    testImplementation platform(
        'org.junit:junit-bom:5.13.4'
    )

    testImplementation 'org.junit.jupiter:junit-jupiter'
}
```

HTML만 사용한다면 Jackson은 필요하지 않습니다.

JSON만 사용한다면 Jsoup은 필요하지 않습니다.

필요한 의존성만 유지하는 것을 권장합니다.

---

# Step 4. Provider Raw Model

사이트 응답을 바로 `UsedItem`으로 변환하지 않는 것을 권장합니다.

먼저 Provider 전용 Raw Model을 만듭니다.

```java
package io.github.hyeseok.usedmarketcrawler.bunjang.model;

public record BunjangItem(
    String id,
    String title,
    Long price,
    String location,
    String imageUrl,
    String itemUrl,
    String description,
    String status,
    String publishedAt
) {
}
```

흐름:

```text
Site Response
      ↓
BunjangItem
      ↓
UsedItem
```

이 구조의 장점:

- 사이트 응답과 Core 분리
- 사이트 구조 변경 영향 최소화
- Parser 테스트 가능
- Mapper 테스트 가능
- Provider별 추가 필드 확장 가능

---

# Step 5. Provider Config

생성:

```text
config/BunjangCrawlerConfig.java
```

예:

```java
package io.github.hyeseok.usedmarketcrawler.bunjang.config;

import java.time.Duration;

public record BunjangCrawlerConfig(
    String baseUrl,
    Duration connectTimeout,
    Duration requestTimeout,
    String userAgent
) {

    public BunjangCrawlerConfig {

        if (
            baseUrl == null
                || baseUrl.isBlank()
        ) {
            throw new IllegalArgumentException(
                "baseUrl must not be blank"
            );
        }

        if (
            connectTimeout == null
        ) {
            throw new IllegalArgumentException(
                "connectTimeout must not be null"
            );
        }

        if (
            requestTimeout == null
        ) {
            throw new IllegalArgumentException(
                "requestTimeout must not be null"
            );
        }

        if (
            userAgent == null
                || userAgent.isBlank()
        ) {
            throw new IllegalArgumentException(
                "userAgent must not be blank"
            );
        }
    }
}
```

---

# Step 6. Client

Client는 HTTP 통신만 담당합니다.

책임:

```text
URL 생성
Query Parameter
HTTP 요청
Timeout
User-Agent
Status Code 검사
Response Body 반환
```

하지 말아야 할 것:

```text
UsedItem 생성
HTML 상세 파싱
JSON 상세 파싱
Core aggregation
Core price filtering
```

예:

```java
public class BunjangClient {

    private final BunjangCrawlerConfig config;

    public BunjangClient(
        BunjangCrawlerConfig config
    ) {

        this.config =
            config;
    }

    public String search(
        String keyword
    ) {

        /*
         * HTTP request
         */

        return responseBody;
    }
}
```

---

# Step 7. Parser

Parser는:

```text
HTML / JSON
      ↓
Provider Raw Item
```

변환만 담당합니다.

예:

```java
public List<BunjangItem> parseSearchResults(
    String payload
) {

    ...
}
```

---

## Parser Rules

### 1. 안정적인 상품 ID 사용

좋은 예:

```text
231432565
```

피해야 할 예:

```text
https://example.com/product/231432565
```

`externalId`는 가능하면 해당 플랫폼의 실제 상품 ID를 사용합니다.

---

### 2. 중복 제거

검색 HTML에 동일 상품 링크가 여러 번 나타날 수 있습니다.

상품 ID 기준으로 중복을 제거합니다.

예:

```java
Map<String, BunjangItem> uniqueItems =
    new LinkedHashMap<>();
```

---

### 3. 정확한 JSON Path 사용

JSON 응답을 파싱할 때:

```text
id
name
title
```

등을 무작정 재귀 탐색하지 않습니다.

잘못하면:

```text
지역
카테고리
검색 메타데이터
광고
추천 키워드
```

가 상품으로 오인될 수 있습니다.

가능하면 실제 상품 배열의 정확한 경로를 사용합니다.

---

### 4. 정상 0건과 Schema 변경 구분

정상:

```text
상품 배열 존재
items = []
```

이면:

```text
success = true
```

로 처리합니다.

하지만 원래 존재해야 할 상품 데이터 구조 자체가 사라졌다면:

```text
success = false
```

로 처리하는 것을 권장합니다.

이렇게 해야:

```text
검색 결과 0건
```

과:

```text
사이트 구조 변경으로 Parser 고장
```

을 구분할 수 있습니다.

---

# Step 8. Mapper

Mapper는 Provider Raw Item을 Core `UsedItem`으로 변환합니다.

```java
public UsedItem toUsedItem(
    BunjangItem item
) {

    return new UsedItem(
        item.id(),
        MarketType.BUNJANG,
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
```

---

## Status Mapping

Core 상태:

```text
AVAILABLE
RESERVED
SOLD
UNKNOWN
```

사이트별 값을 Mapper에서 변환합니다.

예:

```text
판매중
→ AVAILABLE

예약중
→ RESERVED

판매완료
→ SOLD

알 수 없는 값
→ UNKNOWN
```

---

# Step 9. UsedMarketProvider

새 Provider는:

```java
UsedMarketProvider
```

를 구현합니다.

예:

```java
public class BunjangUsedMarketProvider
    implements UsedMarketProvider {

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

            String payload =
                client.search(
                    request.keyword()
                );

            List<UsedItem> items =
                parser.parseSearchResults(
                        payload
                    )
                    .stream()
                    .map(
                        mapper::toUsedItem
                    )
                    .toList();

            return UsedMarketProviderResult.success(
                MarketType.BUNJANG,
                items
            );

        } catch (
            Exception exception
        ) {

            return UsedMarketProviderResult.failure(
                MarketType.BUNJANG,
                exception.getMessage()
            );
        }
    }
}
```

---

# Step 10. Region Handling

지역 검색은 반드시 Provider별로 처리합니다.

예:

```text
request.location() = 분당
```

당근:

```text
분당
↓
Daangn region resolver
↓
Daangn region ID
↓
검색
```

다른 플랫폼:

```text
분당
↓
자체 region code
```

또는 지역 검색 자체를 지원하지 않을 수 있습니다.

Core에서:

```java
item.location()
    .contains(
        request.location()
    )
```

처럼 다시 필터링하면 안 됩니다.

---

# Step 11. Starter Properties

Starter에 Provider 설정을 추가합니다.

예:

```java
@ConfigurationProperties(
    prefix = "used-market-crawler.bunjang"
)
public class BunjangCrawlerProperties {

    private boolean enabled =
        true;

    private String baseUrl =
        "https://...";

    private Duration connectTimeout =
        Duration.ofSeconds(
            5
        );

    private Duration requestTimeout =
        Duration.ofSeconds(
            10
        );

    /*
     * getters / setters
     */
}
```

---

# Step 12. Starter Dependency

`crawler-spring-boot-starter/build.gradle`:

```gradle
dependencies {

    api project(':crawler-core')

    api project(':crawler-provider-daangn')

    api project(':crawler-provider-joongna')

    api project(':crawler-provider-bunjang')
}
```

---

# Step 13. AutoConfiguration

`@EnableConfigurationProperties`에 추가합니다.

```java
@EnableConfigurationProperties({
    DaangnCrawlerProperties.class,
    JoongnaCrawlerProperties.class,
    BunjangCrawlerProperties.class
})
```

그리고 Bean을 등록합니다.

```text
BunjangCrawlerConfig
BunjangClient
BunjangParser
BunjangItemMapper
BunjangUsedMarketProvider
```

각 Bean에는:

```java
@ConditionalOnProperty(
    prefix = "used-market-crawler.bunjang",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
```

를 적용합니다.

---

# Step 14. application.yml

예:

```yaml
used-market-crawler:
  daangn:
    enabled: true

  joongna:
    enabled: true

  bunjang:
    enabled: true
```

Provider별 URL과 timeout도 설정할 수 있습니다.

```yaml
used-market-crawler:
  bunjang:
    enabled: true
    base-url: https://...
    connect-timeout: 5s
    request-timeout: 10s
```

---

# Step 15. Demo Test

신규 Provider만 테스트:

```text
/api/v1/crawler/search?keyword=아이폰&markets=BUNJANG&limit=5
```

전체 Provider:

```text
/api/v1/crawler/search?keyword=아이폰&limit=10
```

명시적으로 여러 Provider:

```text
/api/v1/crawler/search?keyword=아이폰&markets=DAANGN,JOONGNA,BUNJANG&limit=10
```

---

# Testing

Provider를 추가할 때 테스트도 함께 추가하는 것을 권장합니다.

```text
BunjangClientTest
BunjangParserTest
BunjangItemMapperTest
```

---

## Parser Fixture

실제 응답 일부를 테스트 resource로 저장합니다.

```text
crawler-provider-bunjang/
└── src/test/resources/
    └── fixtures/
        ├── search-normal.html
        ├── search-empty.html
        └── search-schema-changed.html
```

검증 대상:

```text
상품 개수
externalId
title
price
location
imageUrl
itemUrl
status
publishedAt
```

---

# Provider Addition Checklist

새로운 Provider를 추가할 때 아래를 순서대로 확인합니다.

- [ ] 대상 사이트의 공개 검색 방식 조사
- [ ] 공식 API 존재 여부 확인
- [ ] `MarketType` 추가
- [ ] `settings.gradle` 모듈 추가
- [ ] Provider `build.gradle`
- [ ] Provider Raw Item
- [ ] Provider Config
- [ ] Provider Client
- [ ] Provider Parser
- [ ] Provider Mapper
- [ ] `UsedMarketProvider` 구현
- [ ] Starter dependency 추가
- [ ] `ConfigurationProperties` 추가
- [ ] AutoConfiguration 등록
- [ ] `application.yml` 설정
- [ ] Provider 단독 검색
- [ ] 전체 통합 검색
- [ ] 가격 필터
- [ ] 지역 검색
- [ ] 중복 상품 제거
- [ ] 정상 검색 결과 0건 처리
- [ ] Schema mismatch 처리
- [ ] Parser fixture 테스트
- [ ] `./gradlew clean build`

---

# Data Source Priority

새 Provider를 조사할 때 다음 순서를 권장합니다.

## 1. Official API

공식적으로 제공되는 API가 있다면 가장 먼저 사용합니다.

## 2. Public HTML

검색 결과가 서버 렌더링 HTML에 존재한다면 HTML을 파싱합니다.

## 3. Public Embedded JSON

HTML 내부에 공개 상품 JSON이 존재한다면 사용할 수 있습니다.

## 4. Public Read-only Endpoint

브라우저가 공개적으로 사용하는 읽기 전용 endpoint를 검토합니다.

## 5. Browser Automation

공개 HTTP 방식으로 필요한 데이터를 얻을 수 없는 경우에만 선택적으로 검토합니다.

---

# Crawling Policy

새 Provider에서도 다음 기능을 구현하지 않습니다.

- CAPTCHA bypass
- Login bypass
- Authentication bypass
- Access-control bypass
- Bot-protection evasion
- Private token extraction
- Proxy-based blocking circumvention

사이트가 접근을 제한한다면 제한을 우회하는 것이 아니라 해당 Provider의 지원 범위를 조정합니다.

---

# Related Documentation

- [README](../README.md)
- [Boot & Test Guide](./BOOT_TEST_GUIDE.md)
- [Crawler Architecture & Features](./CRAWLER_OVERVIEW.md)