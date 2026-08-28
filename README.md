# used-market-crawler

Reusable Java crawler library for aggregating used-market listings across multiple platforms.

`used-market-crawler` provides a common Java API for searching and normalizing listings from multiple used-market services.

Currently supported:

- 🥕 Daangn (당근)
- 🛒 Joongna (중고나라)

The project is designed around a provider-based architecture so additional used-market services can be added without changing the core crawler logic.

---

## Documentation

Detailed documentation is available in the `docs` directory.

### 🚀 Boot & Test Guide

How to build the project, run the Spring Boot demo application, and test each provider.

[BOOT_TEST_GUIDE.md](./docs/BOOT_TEST_GUIDE.md)

### 🔌 Adding a New Provider

Step-by-step guide for adding another used-market service such as Bunjang.

[ADD_PROVIDER_GUIDE.md](./docs/ADD_PROVIDER_GUIDE.md)

### 🏗 Crawler Architecture & Features

Full overview of the crawler architecture, modules, provider responsibilities, aggregation behavior, and future extensions.

[CRAWLER_OVERVIEW.md](./docs/CRAWLER_OVERVIEW.md)

---

## Features

- Multi-platform used-market search
- Provider-based architecture
- Common normalized listing model
- Spring-independent core module
- Spring Boot auto-configuration
- Provider failure isolation
- Price filtering
- Market filtering
- Result aggregation
- Result limit
- Region-aware provider architecture
- Easy provider extension

---

## Supported Markets

| Market | Code | Status |
| --- | --- | --- |
| Daangn (당근) | `DAANGN` | ✅ Supported |
| Joongna (중고나라) | `JOONGNA` | ✅ Supported |
| Bunjang (번개장터) | `BUNJANG` | 🚧 Planned |

---

## Requirements

- Java 21
- Gradle
- Spring Boot 4.x for the starter/demo modules

The `crawler-core` module itself is designed to remain independent of Spring.

---

## Project Structure

```text
used-market-crawler/
├── crawler-core/
├── crawler-provider-daangn/
├── crawler-provider-joongna/
├── crawler-spring-boot-starter/
├── crawler-demo/
└── docs/
```

### crawler-core

Contains the platform-independent crawler API and common models.

```text
UsedMarketCrawler
DefaultUsedMarketCrawler
UsedMarketProvider

UsedItem
UsedItemStatus
MarketType

UsedMarketSearchRequest
UsedMarketSearchResult
UsedMarketProviderResult
```

### crawler-provider-daangn

Daangn-specific implementation.

Responsibilities include:

- Daangn search requests
- Region handling
- Daangn response parsing
- Listing normalization
- Daangn status mapping

### crawler-provider-joongna

Joongna-specific implementation.

Responsibilities include:

- Joongna public search requests
- Search HTML parsing
- Product ID extraction
- Title extraction
- Price extraction
- Image URL extraction
- Listing normalization

### crawler-spring-boot-starter

Provides Spring Boot auto-configuration.

It automatically registers enabled providers and creates:

```text
UsedMarketCrawler
```

with all available:

```text
UsedMarketProvider
```

beans.

### crawler-demo

Spring Boot demo application for development and integration testing.

---

## Architecture

```text
                     UsedMarketSearchRequest
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
        DaangnClient                      JoongnaClient
              │                                 │
              ▼                                 ▼
        DaangnParser                      JoongnaParser
              │                                 │
              ▼                                 ▼
      DaangnItemMapper                  JoongnaItemMapper
              │                                 │
              └──────────────┬──────────────────┘
                             │
                             ▼
                         UsedItem
                             │
                             ▼
                  UsedMarketSearchResult
```

Each provider owns all platform-specific behavior.

The core crawler does not need to understand individual website URLs, HTML structures, region IDs, or platform-specific status values.

---

## Common Listing Model

All provider results are normalized into `UsedItem`.

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

Supported common statuses:

```text
AVAILABLE
RESERVED
SOLD
UNKNOWN
```

---

## Build

From the project root:

```bash
./gradlew clean build
```

Expected result:

```text
BUILD SUCCESSFUL
```

For detailed troubleshooting and testing instructions, see:

[Boot & Test Guide](./docs/BOOT_TEST_GUIDE.md)

---

## Run Demo Application

```bash
./gradlew :crawler-demo:bootRun
```

The demo exposes:

```text
GET /api/v1/crawler/search
```

---

## Search All Markets

If the `markets` parameter is omitted, all registered providers are searched.

```text
GET /api/v1/crawler/search?keyword=아이폰&limit=10
```

Example:

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&limit=10
```

---

## Search Daangn Only

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=DAANGN&limit=5
```

With a location:

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=DAANGN&location=분당&limit=5
```

---

## Search Joongna Only

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=JOONGNA&limit=5
```

---

## Search Multiple Markets

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=DAANGN,JOONGNA&limit=10
```

---

## Price Filtering

Minimum price:

```text
GET /api/v1/crawler/search?keyword=아이폰&minPrice=100000
```

Maximum price:

```text
GET /api/v1/crawler/search?keyword=아이폰&maxPrice=500000
```

Price range:

```text
GET /api/v1/crawler/search?keyword=아이폰&minPrice=100000&maxPrice=500000
```

---

## Spring Boot Configuration

Example `application.yml`:

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

Both providers are enabled by default.

A provider can be disabled independently.

```yaml
used-market-crawler:
  joongna:
    enabled: false
```

---

## Provider Architecture

Each market is implemented as an independent `UsedMarketProvider`.

```java
public interface UsedMarketProvider {

    MarketType market();

    UsedMarketProviderResult search(
        UsedMarketSearchRequest request
    );
}
```

This allows new markets to be added without modifying the aggregation logic.

For the full implementation procedure, see:

[Adding a New Provider](./docs/ADD_PROVIDER_GUIDE.md)

---

## Region Handling

Region semantics are owned by each provider.

For example:

```text
location = 분당
```

may be converted by the Daangn provider into a platform-specific region identifier.

Listings returned for that search may contain locations such as:

```text
정자동
서현동
수내동
```

For this reason, the core crawler does **not** re-filter results using literal location string matching.

Provider-specific region behavior must remain inside the provider implementation.

---

## Provider Failure Isolation

One provider failure does not cause the entire search to fail.

Example:

```text
DAANGN
→ success

JOONGNA
→ request failure
```

The final result can still contain Daangn listings while exposing the Joongna failure through:

```text
UsedMarketProviderResult
```

This is important when aggregating multiple external services with different availability and response structures.

---

## Result Aggregation

Provider results are collected and normalized into a common list.

The core currently handles:

```text
Provider selection
        ↓
Provider execution
        ↓
Failure isolation
        ↓
Price filtering
        ↓
publishedAt sorting
        ↓
limit
        ↓
Final result
```

The top-level `items` field contains the aggregated result.

The `providers` field preserves provider-specific results for diagnostics.

---

## Example Response

```json
{
  "keyword": "아이폰",
  "items": [
    {
      "externalId": "231432565",
      "market": "JOONGNA",
      "title": "아이폰6S 16GB 로즈골드 중고폰",
      "price": 90000,
      "location": null,
      "imageUrl": "https://...",
      "itemUrl": "https://web.joongna.com/product/231432565",
      "description": null,
      "status": "AVAILABLE",
      "publishedAt": null
    }
  ],
  "providers": [
    {
      "market": "DAANGN",
      "items": [],
      "success": true,
      "errorMessage": null
    },
    {
      "market": "JOONGNA",
      "items": [],
      "success": true,
      "errorMessage": null
    }
  ]
}
```

---

## Adding Another Market

The project is designed so a new provider can be added using the following structure:

```text
crawler-provider-{market}/
├── client/
├── config/
├── mapper/
├── model/
├── parser/
└── provider/
```

Typical flow:

```text
HTTP response
    ↓
Provider Parser
    ↓
Provider Raw Item
    ↓
Provider Mapper
    ↓
UsedItem
```

After the provider is registered as a Spring Bean, `DefaultUsedMarketCrawler` can automatically include it in searches.

Full guide:

[ADD_PROVIDER_GUIDE.md](./docs/ADD_PROVIDER_GUIDE.md)

---

## Current Implementation Status

### Core

- [x] Common crawler API
- [x] Common listing model
- [x] Provider abstraction
- [x] Multi-provider aggregation
- [x] Provider failure isolation
- [x] Price filtering
- [x] Result limit
- [x] Market filtering

### Daangn

- [x] Public search request
- [x] Search response parsing
- [x] Listing normalization
- [x] Region-aware architecture
- [x] Price
- [x] Location
- [x] Image URL
- [x] Item URL
- [x] Status
- [x] Published time parsing

### Joongna

- [x] Public search HTML request
- [x] Product link extraction
- [x] Numeric product ID extraction
- [x] Title parsing
- [x] Price parsing
- [x] Image URL parsing
- [x] Item URL
- [x] Basic status mapping
- [ ] Detailed location
- [ ] Published time
- [ ] Description enrichment

### Planned

- [ ] Bunjang provider
- [ ] Provider fixture tests
- [ ] Response schema change detection
- [ ] Rate limiting
- [ ] Caching
- [ ] Optional detail-page enrichment
- [ ] Market price analysis
- [ ] New-listing detection
- [ ] Alert support

---

## Crawling Policy

The project is intended to use publicly accessible information.

Preferred data sources:

1. Official API
2. Public HTML
3. Public embedded JSON
4. Public read-only web endpoints

The project does not aim to implement:

- CAPTCHA bypass
- Login bypass
- Access-control bypass
- Authentication bypass
- Bot-protection evasion
- Private credential extraction
- Proxy-based blocking circumvention

If a provider changes its public response structure, the provider parser should be updated rather than bypassing access controls.

---

## Documentation

For more information:

- [Boot & Test Guide](./docs/BOOT_TEST_GUIDE.md)
- [Adding a New Provider](./docs/ADD_PROVIDER_GUIDE.md)
- [Crawler Architecture & Features](./docs/CRAWLER_OVERVIEW.md)

---

## License

Apache License 2.0