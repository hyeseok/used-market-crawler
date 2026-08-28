# Crawler Architecture & Features

`used-market-crawler`의 전체 기능, 모듈 구조, 검색 흐름 및 확장 방향을 설명합니다.

---

# Overview

`used-market-crawler`는 여러 중고거래 플랫폼의 공개 상품 검색 결과를 하나의 공통 Java API로 조회하기 위한 오픈소스 라이브러리입니다.

현재 지원:

| Market | Code | Status |
| --- | --- | --- |
| 당근 | `DAANGN` | ✅ Supported |
| 중고나라 | `JOONGNA` | ✅ Supported |
| 번개장터 | `BUNJANG` | 🚧 Planned |

---

# Goal

각 중고거래 플랫폼은 서로 다른 데이터 구조를 가지고 있습니다.

예:

```text
검색 URL
상품 ID
HTML
JSON
가격
지역
상태
이미지
등록시간
```

`used-market-crawler`는 이러한 차이를 Provider 내부에서 처리하고 최종적으로:

```java
UsedItem
```

이라는 하나의 모델로 표준화합니다.

---

# Technology

현재 프로젝트 기준:

```text
Java 21
Gradle
Spring Boot 4.x
Jsoup
Jackson
```

Core는 Spring에 종속되지 않습니다.

---

# Project Structure

```text
used-market-crawler/
├── crawler-core/
├── crawler-provider-daangn/
├── crawler-provider-joongna/
├── crawler-spring-boot-starter/
├── crawler-demo/
└── docs/
```

---

# Module Responsibilities

## crawler-core

공통 API와 모델을 제공합니다.

```text
UsedMarketCrawler
DefaultUsedMarketCrawler

UsedMarketProvider

MarketType
UsedItem
UsedItemStatus

UsedMarketSearchRequest
UsedMarketSearchResult
UsedMarketProviderResult
```

Core는 특정 사이트의 구현을 알지 못합니다.

---

## crawler-provider-daangn

당근 전용 기능:

```text
HTTP Request
Region Resolution
Response Parsing
Daangn Raw Item
UsedItem Mapping
```

---

## crawler-provider-joongna

중고나라 전용 기능:

```text
HTTP Request
HTML Parsing
Product ID Extraction
Title Parsing
Price Parsing
Image URL Parsing
UsedItem Mapping
```

---

## crawler-spring-boot-starter

Spring Boot Auto Configuration을 담당합니다.

```text
Crawler Properties
Crawler Config
Client Bean
Parser Bean
Mapper Bean
Provider Bean
UsedMarketCrawler Bean
```

---

## crawler-demo

라이브러리를 실제 Spring Boot 환경에서 실행하고 테스트합니다.

제공 API:

```text
GET /api/v1/crawler/search
```

---

# High-Level Architecture

```text
                       Application
                            │
                            ▼
                  UsedMarketCrawler
                            │
                            ▼
              DefaultUsedMarketCrawler
                            │
           ┌────────────────┴────────────────┐
           │                                 │
           ▼                                 ▼
DaangnUsedMarketProvider          JoongnaUsedMarketProvider
           │                                 │
           ▼                                 ▼
     DaangnClient                       JoongnaClient
           │                                 │
           ▼                                 ▼
     DaangnParser                       JoongnaParser
           │                                 │
           ▼                                 ▼
   DaangnItemMapper                   JoongnaItemMapper
           │                                 │
           └────────────────┬────────────────┘
                            │
                            ▼
                         UsedItem
                            │
                            ▼
                UsedMarketSearchResult
```

---

# Search Flow

사용자가 검색합니다.

```text
keyword = 아이폰
markets = DAANGN, JOONGNA
minPrice = 100000
maxPrice = 500000
limit = 10
```

흐름:

```text
UsedMarketSearchRequest
          │
          ▼
DefaultUsedMarketCrawler
          │
          ▼
Provider Selection
          │
          ├────────── DAANGN
          │
          └────────── JOONGNA
          │
          ▼
Provider Execution
          │
          ▼
UsedMarketProviderResult
          │
          ▼
Successful Items
          │
          ▼
Price Filter
          │
          ▼
publishedAt Sort
          │
          ▼
limit
          │
          ▼
UsedMarketSearchResult
```

---

# UsedMarketCrawler

공통 진입점입니다.

```java
public interface UsedMarketCrawler {

    UsedMarketSearchResult search(
        UsedMarketSearchRequest request
    );
}
```

애플리케이션은 특정 Provider를 직접 호출할 필요 없이 이 인터페이스를 사용할 수 있습니다.

---

# DefaultUsedMarketCrawler

현재 통합 Crawler 구현체입니다.

담당:

- Request validation
- Provider selection
- Provider execution
- Provider exception isolation
- Price filtering
- Result aggregation
- Sorting
- Limit

---

# UsedMarketProvider

모든 플랫폼 Provider가 구현해야 하는 인터페이스입니다.

```java
public interface UsedMarketProvider {

    MarketType market();

    UsedMarketProviderResult search(
        UsedMarketSearchRequest request
    );
}
```

Provider 추가 시 Core 검색 로직을 다시 구현할 필요가 없습니다.

---

# UsedItem

모든 플랫폼의 상품을 표현하는 공통 모델입니다.

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

---

# UsedItemStatus

공통 상태:

```text
AVAILABLE
RESERVED
SOLD
UNKNOWN
```

각 사이트의 원본 상태값은 Provider Mapper에서 변환합니다.

예:

```text
판매중
ON_SALE
SELLING
    ↓
AVAILABLE
```

---

# UsedMarketSearchRequest

검색 조건:

```text
keyword
markets
minPrice
maxPrice
location
limit
```

---

## keyword

검색 키워드입니다.

예:

```text
아이폰
맥북
에어팟
플레이스테이션
```

필수값입니다.

---

## markets

검색할 Provider 목록입니다.

예:

```text
DAANGN
```

또는:

```text
DAANGN, JOONGNA
```

빈 Set이면 등록된 모든 Provider를 검색합니다.

---

## minPrice / maxPrice

통합 가격 필터입니다.

예:

```text
100000 <= price <= 500000
```

Core에서 처리합니다.

---

## location

지역 검색 조건입니다.

단, 실제 의미는 Provider가 해석합니다.

Core에서 literal string matching을 하지 않습니다.

---

## limit

최종 통합 결과의 최대 상품 개수입니다.

Provider가 수집한 원본 상품 수를 제한하는 값과 반드시 동일하지는 않습니다.

---

# UsedMarketProviderResult

각 Provider 실행 결과를 표현합니다.

성공:

```json
{
  "market": "DAANGN",
  "items": [],
  "success": true,
  "errorMessage": null
}
```

실패:

```json
{
  "market": "JOONGNA",
  "items": [],
  "success": false,
  "errorMessage": "request failed"
}
```

---

# Provider Failure Isolation

멀티 Provider 시스템에서는 한 사이트 장애가 전체 검색을 중단시키면 안 됩니다.

예:

```text
DAANGN
    ↓
SUCCESS

JOONGNA
    ↓
TIMEOUT
```

최종 검색은:

```text
DAANGN 결과 반환
+
JOONGNA failure 정보 반환
```

으로 처리됩니다.

---

# Aggregated Result

최종 결과:

```java
UsedMarketSearchResult
```

은 크게:

```text
items
providers
```

두 영역을 가집니다.

---

## items

여러 Provider의 상품을 합친 최종 목록입니다.

```text
DAANGN items
       +
JOONGNA items
       ↓
Price Filter
       ↓
Sort
       ↓
Limit
       ↓
items
```

---

## providers

Provider별 원본 실행 결과입니다.

디버깅과 장애 확인에 유용합니다.

예:

```text
providers
├── DAANGN
│   ├── success
│   └── items
│
└── JOONGNA
    ├── success
    └── items
```

---

# Sorting

현재 기본 통합 정렬:

```text
publishedAt DESC
```

등록시간이 없는 상품:

```text
null last
```

로 처리합니다.

---

## Current Joongna Sorting Limitation

현재 중고나라 검색 결과에서는 정확한 `publishedAt`을 아직 매핑하지 않을 수 있습니다.

따라서:

```text
DAANGN
publishedAt != null

JOONGNA
publishedAt == null
```

인 경우 당근 상품이 먼저 배치될 수 있습니다.

이 때문에 통합 `limit`이 작으면 최상위 `items`에 Joongna 상품이 포함되지 않을 수도 있습니다.

Provider 자체 결과는:

```text
providers[].items
```

에서 확인할 수 있습니다.

향후 Joongna의 정확한 등록시간을 파싱하는 것이 개선 대상입니다.

---

# Price Filtering

가격 필터는 Core에서 수행합니다.

조건이 없는 경우:

```text
price == null
```

상품도 유지할 수 있습니다.

가격 조건이 존재하는 경우:

```text
minPrice
maxPrice
```

를 비교할 수 없는 `price == null` 상품은 제외합니다.

---

# Region Architecture

지역은 플랫폼마다 의미가 다릅니다.

예:

```text
사용자 입력
분당
```

당근에서는:

```text
분당
↓
Region Resolver
↓
Daangn Region ID
↓
Search
```

가 될 수 있습니다.

실제 상품의 지역:

```text
정자동
수내동
서현동
```

따라서 Core에서:

```java
item.location()
    .contains(
        "분당"
    )
```

을 실행하면 잘못된 결과가 됩니다.

지역은 Provider가 책임집니다.

---

# Daangn Provider

당근 Provider의 전체 흐름:

```text
UsedMarketSearchRequest
          ↓
DaangnUsedMarketProvider
          ↓
DaangnClient
          ↓
Public Daangn Search Response
          ↓
DaangnParser
          ↓
DaangnItem
          ↓
DaangnItemMapper
          ↓
UsedItem
```

---

## Daangn Region Search

공개 지역 resolver를 통해 사람이 입력한 지역을 검색 가능한 값으로 변환할 수 있습니다.

개념적 흐름:

```text
분당
↓
Region Resolver
↓
Region name + ID
↓
Daangn Search
```

지역 로직은 Core가 아닌 Daangn Provider에 존재해야 합니다.

---

# Joongna Provider

중고나라 검색 페이지:

```text
https://web.joongna.com/search/{keyword}
```

검색 HTML에 상품 카드가 서버 렌더링되어 있습니다.

확인된 상품 링크 형태:

```text
/product/231432565
/product/231371579
/product/231433590
```

상품 등록 링크:

```text
/product/form?type=regist
```

는 상품에서 제외합니다.

---

## Joongna Product ID

상품 ID는 숫자 부분만 사용합니다.

```text
/product/231432565
         ↓
231432565
```

---

## Joongna Title

검색 카드 전체 text에는:

```text
안심결제
상품명
가격
숫자
시간
```

등이 섞여 있습니다.

따라서 현재는:

```html
<img alt="상품명 이미지">
```

의 `alt` 값을 우선 사용합니다.

마지막:

```text
이미지
```

를 제거하여 title을 만듭니다.

---

## Joongna Price

예:

```text
90,000 원
```

을:

```text
90000
```

으로 변환합니다.

---

## Joongna Current Limitations

현재 추가 개선이 필요한 데이터:

- `publishedAt`
- 상세 `location`
- `description`
- 더 정확한 판매 상태

향후 공개 embedded JSON 또는 공개 상세 페이지를 이용해 보강할 수 있습니다.

---

# Spring Boot Starter

Starter는 애플리케이션이 직접 Provider를 조립하지 않아도 되도록 Auto Configuration을 제공합니다.

---

## Daangn Beans

```text
DaangnCrawlerProperties
        ↓
DaangnCrawlerConfig
        ↓
DaangnClient

DaangnParser

DaangnItemMapper
        ↓
DaangnUsedMarketProvider
```

---

## Joongna Beans

```text
JoongnaCrawlerProperties
        ↓
JoongnaCrawlerConfig
        ↓
JoongnaClient

JoongnaParser

JoongnaItemMapper
        ↓
JoongnaUsedMarketProvider
```

---

## Aggregation Bean

Spring이:

```java
List<UsedMarketProvider>
```

를 자동으로 주입합니다.

예:

```text
[
    DaangnUsedMarketProvider,
    JoongnaUsedMarketProvider
]
```

이를:

```java
new DefaultUsedMarketCrawler(
    providers
)
```

에 전달합니다.

따라서 새 Provider가 Spring Bean으로 등록되면 전체 검색에도 자연스럽게 포함됩니다.

---

# Configuration

예:

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

# Demo API

Endpoint:

```text
GET /api/v1/crawler/search
```

---

## All Markets

```text
/api/v1/crawler/search?keyword=아이폰&limit=10
```

---

## Daangn

```text
/api/v1/crawler/search?keyword=아이폰&markets=DAANGN&location=분당&limit=5
```

---

## Joongna

```text
/api/v1/crawler/search?keyword=아이폰&markets=JOONGNA&limit=5
```

---

## Multiple Markets

```text
/api/v1/crawler/search?keyword=아이폰&markets=DAANGN,JOONGNA&limit=10
```

---

## Price Range

```text
/api/v1/crawler/search?keyword=아이폰&minPrice=100000&maxPrice=500000&limit=10
```

---

# Future Architecture

현재 구조에서 다음 기능들을 확장할 수 있습니다.

---

## Additional Providers

예:

```text
crawler-provider-bunjang
```

Provider만 추가하고 Starter에 등록하면 됩니다.

---

## Detail Enrichment

검색 결과에서 부족한:

```text
description
location
publishedAt
status
```

를 상세 페이지 요청으로 보강할 수 있습니다.

다만 상품마다 추가 HTTP 요청이 발생하기 때문에:

```text
enabled = false
```

가 기본인 Optional 기능으로 두는 것이 좋습니다.

---

## Cache

동일한:

```text
keyword
market
location
```

검색이 반복될 경우 TTL Cache를 적용할 수 있습니다.

예:

```text
아이폰 + DAANGN + 분당
```

결과를 짧은 시간 캐싱하면 외부 사이트 요청량을 줄일 수 있습니다.

---

## Rate Limit

Provider별 요청 속도를 제한할 수 있습니다.

예:

```text
DAANGN
→ N requests / second

JOONGNA
→ N requests / second
```

각 외부 서비스에 불필요하게 많은 요청을 보내지 않도록 하는 것이 목적입니다.

---

## Market Price Analysis

수집한 상품을 이용해 시세 분석 기능을 만들 수 있습니다.

예:

```text
평균 가격
중앙값
최저가
최고가
플랫폼별 평균가
이상치 제거 평균
최근 가격 추이
```

예:

```text
아이폰 16 Pro 256GB

DAANGN 평균
1,050,000원

JOONGNA 평균
1,020,000원

통합 중앙값
1,035,000원
```

---

## New Listing Detection

이전 검색 결과의:

```text
market + externalId
```

를 저장하면 신규 등록 상품을 식별할 수 있습니다.

```text
Previous IDs
      ↓
Current IDs
      ↓
Difference
      ↓
New Listings
```

---

## Alert

향후:

```text
keyword = 아이폰 16 프로
maxPrice = 1000000
location = 분당
```

같은 조건을 저장하고 신규 상품이 조건에 맞으면 알림을 보내는 기능으로 확장할 수 있습니다.

---

# Design Principles

## Core

> Core does not know individual markets.

Core는 사이트 구조를 몰라야 합니다.

---

## Provider

> Provider owns market-specific behavior.

Provider가 다음을 담당합니다.

```text
URL
HTTP
HTML
JSON
Region
Status
External ID
Date
```

---

## Starter

> Starter assembles providers.

Spring 환경에서 필요한 Bean을 자동으로 조립합니다.

---

## Demo

> Demo runs and verifies the library.

실제 라이브러리 기능과 테스트 애플리케이션을 분리합니다.

---

# Crawling Policy

프로젝트는 공개적으로 접근 가능한 데이터를 이용하는 방향으로 유지합니다.

데이터 소스 우선순위:

1. Official API
2. Public HTML
3. Public embedded JSON
4. Public read-only endpoint

공개 HTTP 방식으로 필요한 데이터를 얻을 수 없다면 Browser Automation을 선택적으로 검토할 수 있지만, 접근 제한을 우회하기 위한 용도로 사용하지 않습니다.

다음 기능은 프로젝트의 대상이 아닙니다.

- CAPTCHA bypass
- Login bypass
- Authentication bypass
- Access-control bypass
- Bot-protection evasion
- Private credential extraction
- Proxy-based blocking circumvention

---

# Current Status

## Core

- [x] Common crawler API
- [x] Common listing model
- [x] Provider abstraction
- [x] Multi-provider aggregation
- [x] Provider failure isolation
- [x] Price filtering
- [x] Market filtering
- [x] Result limit
- [x] Published time sorting

## Daangn

- [x] Public search
- [x] Region-aware search architecture
- [x] Response parsing
- [x] Product normalization
- [x] Price
- [x] Location
- [x] Image
- [x] URL
- [x] Status
- [x] Published time

## Joongna

- [x] Public HTML search
- [x] Product link parsing
- [x] Product ID
- [x] Title
- [x] Price
- [x] Image URL
- [x] Item URL
- [x] Basic status
- [ ] Published time
- [ ] Detailed location
- [ ] Description enrichment

## Next

- [ ] Joongna published time
- [ ] Joongna location
- [ ] Daangn external ID final normalization check
- [ ] Daangn region resolver formalization
- [ ] Parser fixture tests
- [ ] Schema mismatch detection
- [ ] Rate limiting
- [ ] Caching
- [ ] Bunjang Provider
- [ ] Market price analysis
- [ ] New listing detection
- [ ] Alerts

---

# Related Documentation

- [README](../README.md)
- [Boot & Test Guide](./BOOT_TEST_GUIDE.md)
- [Adding a New Provider](./ADD_PROVIDER_GUIDE.md)