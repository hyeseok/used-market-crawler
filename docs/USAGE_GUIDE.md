# Usage Guide

`used-market-crawler`를 개인 프로젝트 또는 별도 Spring Boot 애플리케이션에 연결해서 사용하는 방법을 설명합니다.

이 문서는 크롤러 내부 개발보다 실제 사용법에 초점을 맞춥니다.

현재 지원 Provider:

- `DAANGN` - 당근
- `JOONGNA` - 중고나라

---

# Overview

`used-market-crawler`는 여러 중고거래 플랫폼을 하나의 공통 API로 검색할 수 있도록 제공합니다.

애플리케이션에서는 직접 각 사이트의:

```text
HTML
JSON
검색 URL
지역 코드
상품 상태
상품 ID
```

를 처리할 필요 없이:

```java
UsedMarketCrawler
```

만 사용하면 됩니다.

기본 사용 흐름:

```text
Your Application
       ↓
UsedMarketCrawler
       ↓
DAANGN Provider
JOONGNA Provider
       ↓
UsedMarketSearchResult
```

---

# Integration Options

개인 프로젝트에 붙이는 방법은 크게 두 가지입니다.

## Option 1. Spring Boot Starter

Spring Boot 프로젝트라면 가장 권장하는 방식입니다.

```text
crawler-spring-boot-starter
```

의존성만 추가하면 필요한 Provider Bean과 `UsedMarketCrawler`가 자동 등록됩니다.

## Option 2. Core + Provider 직접 사용

Spring을 사용하지 않거나 Bean Auto Configuration이 필요하지 않은 프로젝트에서는 직접 객체를 생성할 수 있습니다.

---

# Option 1. Spring Boot Starter

## Gradle

라이브러리를 로컬 Maven 또는 Maven Central 등에 배포한 이후에는 다음과 같이 사용할 수 있습니다.

예:

```gradle
dependencies {
    implementation 'io.github.hyeseok:used-market-crawler-spring-boot-starter:0.1.0'
}
```

> 실제 artifact 이름과 version은 배포 설정에 맞춰 사용합니다.

현재 개발 중 로컬 Multi-module 프로젝트 내부에서 테스트한다면:

```gradle
dependencies {
    implementation project(':crawler-spring-boot-starter')
}
```

---

# Maven

Maven 프로젝트에서는:

```xml
<dependency>
    <groupId>io.github.hyeseok</groupId>
    <artifactId>used-market-crawler-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

> 실제 배포 artifact 이름과 version에 맞게 수정합니다.

---

# Spring Boot Configuration

기본적으로 Provider는 활성화됩니다.

```yaml
used-market-crawler:
  daangn:
    enabled: true

  joongna:
    enabled: true
```

필요하면 timeout과 base URL도 설정할 수 있습니다.

```yaml
used-market-crawler:
  daangn:
    enabled: true
    base-url: https://www.daangn.com
    connect-timeout: 5s
    request-timeout: 10s

  joongna:
    enabled: true
    base-url: https://web.joongna.com
    connect-timeout: 5s
    request-timeout: 10s
```

---

# Inject UsedMarketCrawler

Spring Boot에서는 Bean으로 자동 등록되므로 생성자 주입하면 됩니다.

```java
import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;

import org.springframework.stereotype.Service;

@Service
public class UsedMarketSearchService {

    private final UsedMarketCrawler crawler;

    public UsedMarketSearchService(
        UsedMarketCrawler crawler
    ) {
        this.crawler =
            crawler;
    }
}
```

---

# Search All Markets

전체 Provider를 검색하려면 `markets`에 빈 Set을 전달합니다.

```java
import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;

import java.util.Set;

public class UsedMarketSearchService {

    private final UsedMarketCrawler crawler;

    public UsedMarketSearchService(
        UsedMarketCrawler crawler
    ) {
        this.crawler =
            crawler;
    }

    public UsedMarketSearchResult search(
        String keyword
    ) {

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                Set.of(),
                null,
                null,
                null,
                20
            );

        return crawler.search(
            request
        );
    }
}
```

빈 Set의 의미:

```text
등록된 모든 Provider 검색
```

현재라면:

```text
DAANGN
JOONGNA
```

가 실행됩니다.

---

# Search Daangn Only

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(
            MarketType.DAANGN
        ),
        null,
        null,
        null,
        20
    );
```

---

# Search Joongna Only

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(
            MarketType.JOONGNA
        ),
        null,
        null,
        null,
        20
    );
```

---

# Search Multiple Markets

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(
            MarketType.DAANGN,
            MarketType.JOONGNA
        ),
        null,
        null,
        null,
        20
    );
```

---

# Price Filtering

예:

```text
100,000원 이상
500,000원 이하
```

검색:

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(),
        100_000L,
        500_000L,
        null,
        20
    );
```

Core에서 가격 필터를 적용합니다.

---

# Region Search

지역 조건도 전달할 수 있습니다.

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(
            MarketType.DAANGN
        ),
        null,
        null,
        "분당",
        20
    );
```

지역 검색 방식은 Provider마다 다릅니다.

예:

```text
DAANGN
분당
↓
Daangn Region Resolver
↓
해당 지역 검색
```

Core에서는 지역을 문자 비교로 다시 필터링하지 않습니다.

---

# Limit

최종 결과 개수를 제한할 수 있습니다.

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(),
        null,
        null,
        null,
        10
    );
```

최종:

```text
items <= 10
```

입니다.

Provider별 원본 `items`는 더 많을 수 있습니다.

---

# Search Result

검색 결과:

```java
UsedMarketSearchResult result =
    crawler.search(
        request
    );
```

---

## Aggregated Items

```java
result.items();
```

여러 Provider에서 가져온 상품을 합친 최종 목록입니다.

예:

```java
for (
    UsedItem item :
    result.items()
) {

    System.out.println(
        item.market()
            + " / "
            + item.title()
            + " / "
            + item.price()
    );
}
```

---

# Provider Results

Provider별 상태도 확인할 수 있습니다.

```java
result.providers();
```

예:

```java
result.providers()
    .forEach(
        providerResult -> {

            System.out.println(
                providerResult.market()
            );

            System.out.println(
                providerResult.success()
            );

            System.out.println(
                providerResult.errorMessage()
            );
        }
    );
```

---

# UsedItem Fields

통합 상품 모델:

```java
public record UsedItem(
    String externalId,
    MarketType market,
    String title,
    Long price,
    String location,
    String imageUrl,
    String itemUrl,
    String description,
    UsedItemStatus status,
    Instant publishedAt
) {
}
```

대표 사용:

```java
item.externalId();
item.market();
item.title();
item.price();
item.location();
item.imageUrl();
item.itemUrl();
item.status();
item.publishedAt();
```

---

# Example REST Controller

개인 Spring Boot 프로젝트에서 간단한 API로 감쌀 수도 있습니다.

```java
package com.example.market.controller;

import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class MarketSearchController {

    private final UsedMarketCrawler crawler;

    public MarketSearchController(
        UsedMarketCrawler crawler
    ) {
        this.crawler =
            crawler;
    }

    @GetMapping(
        "/api/market/search"
    )
    public UsedMarketSearchResult search(
        @RequestParam
        String keyword
    ) {

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                Set.of(),
                null,
                null,
                null,
                20
            );

        return crawler.search(
            request
        );
    }
}
```

호출:

```text
GET /api/market/search?keyword=아이폰
```

---

# Example Service Layer

실제 프로젝트에서는 Controller에서 직접 Crawler를 호출하기보다 Service로 감싸는 것을 권장합니다.

```java
package com.example.market.service;

import io.github.hyeseok.usedmarketcrawler.core.UsedMarketCrawler;
import io.github.hyeseok.usedmarketcrawler.core.model.MarketType;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedItem;
import io.github.hyeseok.usedmarketcrawler.core.model.UsedMarketSearchRequest;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MarketSearchService {

    private final UsedMarketCrawler crawler;

    public MarketSearchService(
        UsedMarketCrawler crawler
    ) {

        this.crawler =
            crawler;
    }

    public List<UsedItem> searchAll(
        String keyword
    ) {

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                Set.of(),
                null,
                null,
                null,
                20
            );

        return crawler.search(
                request
            )
            .items();
    }

    public List<UsedItem> searchDaangn(
        String keyword,
        String location
    ) {

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                Set.of(
                    MarketType.DAANGN
                ),
                null,
                null,
                location,
                20
            );

        return crawler.search(
                request
            )
            .items();
    }

    public List<UsedItem> searchJoongna(
        String keyword
    ) {

        UsedMarketSearchRequest request =
            new UsedMarketSearchRequest(
                keyword,
                Set.of(
                    MarketType.JOONGNA
                ),
                null,
                null,
                null,
                20
            );

        return crawler.search(
                request
            )
            .items();
    }
}
```

---

# Handling Provider Failure

외부 사이트는 일시적으로 실패할 수 있습니다.

예:

```text
DAANGN
→ 정상

JOONGNA
→ timeout
```

이 경우 전체 검색 자체가 반드시 실패하지는 않습니다.

Provider별 결과를 확인할 수 있습니다.

```java
result.providers()
    .stream()
    .filter(
        provider ->
            !provider.success()
    )
    .forEach(
        provider -> {

            System.err.println(
                provider.market()
                    + ": "
                    + provider.errorMessage()
            );
        }
    );
```

---

# Disable Unused Provider

개인 프로젝트에서 당근만 사용한다면 중고나라를 끌 수 있습니다.

```yaml
used-market-crawler:
  daangn:
    enabled: true

  joongna:
    enabled: false
```

중고나라만 사용:

```yaml
used-market-crawler:
  daangn:
    enabled: false

  joongna:
    enabled: true
```

이렇게 하면 비활성 Provider Bean 자체가 등록되지 않습니다.

---

# Option 2. Without Spring Boot Starter

Spring Boot Starter를 사용하지 않을 경우 직접 객체를 구성할 수 있습니다.

예를 들어 Joongna만 사용하는 경우:

```java
JoongnaCrawlerConfig config =
    new JoongnaCrawlerConfig(
        "https://web.joongna.com",
        Duration.ofSeconds(
            5
        ),
        Duration.ofSeconds(
            10
        ),
        "my-application/1.0"
    );

JoongnaClient client =
    new JoongnaClient(
        config
    );

JoongnaParser parser =
    new JoongnaParser();

JoongnaItemMapper mapper =
    new JoongnaItemMapper();

JoongnaUsedMarketProvider provider =
    new JoongnaUsedMarketProvider(
        client,
        parser,
        mapper
    );

UsedMarketCrawler crawler =
    new DefaultUsedMarketCrawler(
        List.of(
            provider
        )
    );
```

이후 사용법은 동일합니다.

```java
UsedMarketSearchRequest request =
    new UsedMarketSearchRequest(
        "아이폰",
        Set.of(
            MarketType.JOONGNA
        ),
        null,
        null,
        null,
        20
    );

UsedMarketSearchResult result =
    crawler.search(
        request
    );
```

---

# Using Multiple Providers Without Spring

직접 여러 Provider를 구성할 수도 있습니다.

```java
UsedMarketCrawler crawler =
    new DefaultUsedMarketCrawler(
        List.of(
            daangnProvider,
            joongnaProvider
        )
    );
```

Core는:

```java
List<UsedMarketProvider>
```

만 알기 때문에 Provider 개수에 제한되지 않습니다.

---

# Recommended Application Structure

개인 프로젝트에서는 다음처럼 두는 것을 권장합니다.

```text
your-application/
└── src/main/java/
    └── com/example/
        ├── controller/
        │   └── MarketSearchController.java
        │
        ├── service/
        │   └── MarketSearchService.java
        │
        └── ...
```

크롤러 내부 코드를 복사해 넣지 않습니다.

애플리케이션에서는:

```text
used-market-crawler dependency
        ↓
UsedMarketCrawler Bean
        ↓
Your Service
        ↓
Your Controller / Batch / Scheduler
```

형태로 사용하는 것이 좋습니다.

---

# Example Use Cases

`used-market-crawler`를 기반으로 다양한 기능을 만들 수 있습니다.

---

## Used Product Search

```text
사용자
↓
아이폰 검색
↓
DAANGN + JOONGNA
↓
통합 결과
```

---

## Market Price Comparison

```text
아이폰 16 Pro
↓
여러 마켓 검색
↓
가격 데이터 추출
↓
평균 / 중앙값 계산
```

---

## Cheap Listing Finder

예:

```text
keyword = 아이폰 16
maxPrice = 800000
```

조건 검색:

```java
new UsedMarketSearchRequest(
    "아이폰 16",
    Set.of(),
    null,
    800_000L,
    null,
    20
);
```

---

## Region Based Search

```text
keyword = 맥북
location = 분당
market = DAANGN
```

---

## Scheduled Search

Spring Scheduler 등과 결합할 수도 있습니다.

```java
@Scheduled(
    fixedDelay = 300_000
)
public void search() {

    UsedMarketSearchResult result =
        crawler.search(
            request
        );

    /*
     * 신규 상품 비교
     */
}
```

---

## New Listing Detection

상품의 고유 식별자로:

```text
market + externalId
```

조합을 사용할 수 있습니다.

예:

```text
DAANGN:123456
JOONGNA:231432565
```

이 값을 DB에 저장한 뒤 다음 검색 결과와 비교하면 신규 상품을 감지할 수 있습니다.

---

# Production Considerations

실제 서비스에 적용할 경우 다음 항목을 고려하는 것이 좋습니다.

---

## Timeout

외부 사이트 응답 지연이 애플리케이션 전체 요청을 오래 잡지 않도록 timeout을 설정합니다.

```yaml
request-timeout: 10s
```

---

## Cache

동일 검색어가 반복되면 짧은 TTL 캐시 사용을 고려합니다.

예:

```text
아이폰
아이폰
아이폰
```

매번 외부 요청을 보내기보다 일정 시간 결과를 재사용할 수 있습니다.

---

## Rate Limit

각 Provider로 보내는 요청 수를 제한하는 것이 좋습니다.

목적:

- 외부 사이트 부하 감소
- 불필요한 반복 요청 방지
- 애플리케이션 안정성 확보

---

## Provider Failure

Provider 하나가 실패할 수 있다는 전제로 개발해야 합니다.

따라서:

```text
전체 성공
```

만 가정하지 말고:

```text
부분 성공
```

도 정상적인 실행 결과로 처리하는 것이 좋습니다.

---

# Current Provider Limitations

## Daangn

지역 기반 검색을 지원하지만 외부 공개 응답 구조가 변경될 수 있습니다.

## Joongna

현재 다음 데이터는 제한적일 수 있습니다.

```text
publishedAt
location
description
```

따라서 해당 필드가 항상 존재한다고 가정하면 안 됩니다.

---

# Dependency Upgrade

새 버전으로 올릴 때는 release note를 확인한 뒤:

```gradle
implementation 'io.github.hyeseok:used-market-crawler-spring-boot-starter:NEW_VERSION'
```

처럼 버전을 변경합니다.

Major version이 올라가는 경우 API 변경 여부를 먼저 확인하는 것을 권장합니다.

---

# Recommended Usage Pattern

가장 권장하는 구성:

```text
Spring Boot Application
        ↓
crawler-spring-boot-starter
        ↓
UsedMarketCrawler
        ↓
Application Service
        ↓
REST API / Batch / Scheduler
```

애플리케이션은 Provider 내부 구현을 직접 의존하지 않는 것이 좋습니다.

즉 다음보다:

```java
DaangnClient
JoongnaParser
```

를 직접 사용하는 것보다:

```java
UsedMarketCrawler
```

를 사용하는 것을 권장합니다.

---

# Related Documentation

- [README](../README.md)
- [Boot & Test Guide](./BOOT_TEST_GUIDE.md)
- [Adding a New Provider](./ADD_PROVIDER_GUIDE.md)
- [Crawler Architecture & Features](./CRAWLER_OVERVIEW.md)