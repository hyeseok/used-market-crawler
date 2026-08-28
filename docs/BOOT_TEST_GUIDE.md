# Boot & Test Guide

`used-market-crawler` 프로젝트를 로컬에서 빌드하고 Spring Boot Demo 애플리케이션을 실행한 뒤, 각 중고거래 Provider를 테스트하는 방법을 설명합니다.

현재 지원 Provider:

- `DAANGN` - 당근
- `JOONGNA` - 중고나라

---

## Requirements

프로젝트 실행을 위해 다음 환경이 필요합니다.

- Java 21
- Gradle Wrapper
- Internet connection

Java 버전을 확인합니다.

```bash
java -version
```

예:

```text
openjdk version "21"
```

---

## Project Structure

현재 프로젝트는 다음과 같은 Multi-module 구조입니다.

```text
used-market-crawler/
├── crawler-core/
├── crawler-provider-daangn/
├── crawler-provider-joongna/
├── crawler-spring-boot-starter/
├── crawler-demo/
└── docs/
```

각 모듈의 역할은 다음과 같습니다.

| Module | Description |
| --- | --- |
| `crawler-core` | 공통 Crawler API 및 모델 |
| `crawler-provider-daangn` | 당근 Provider |
| `crawler-provider-joongna` | 중고나라 Provider |
| `crawler-spring-boot-starter` | Spring Boot Auto Configuration |
| `crawler-demo` | 실제 실행 및 API 테스트 |

---

## Build

프로젝트 루트에서 전체 빌드를 실행합니다.

```bash
./gradlew clean build
```

정상적으로 완료되면:

```text
BUILD SUCCESSFUL
```

이 출력되어야 합니다.

---

## Build Failure Checklist

빌드가 실패한다면 먼저 `settings.gradle`에 모든 모듈이 등록되어 있는지 확인합니다.

```gradle
rootProject.name = 'used-market-crawler'

include 'crawler-core'
include 'crawler-provider-daangn'
include 'crawler-provider-joongna'
include 'crawler-spring-boot-starter'
include 'crawler-demo'
```

Starter에도 Provider dependency가 등록되어 있어야 합니다.

```gradle
dependencies {
    api project(':crawler-core')

    api project(':crawler-provider-daangn')
    api project(':crawler-provider-joongna')
}
```

Java 21이 사용되고 있는지도 확인합니다.

```bash
java -version
```

---

## Run Demo Application

Demo 애플리케이션을 실행합니다.

```bash
./gradlew :crawler-demo:bootRun
```

정상적으로 실행되면 Spring Boot 애플리케이션이 시작됩니다.

기본 포트:

```text
8080
```

따라서 기본 주소는:

```text
http://localhost:8080
```

입니다.

`application.yml`에서 포트를 변경했다면 변경된 포트를 사용합니다.

---

# Search API

Demo에서는 다음 API를 제공합니다.

```text
GET /api/v1/crawler/search
```

지원 파라미터:

| Parameter | Required | Description |
| --- | --- | --- |
| `keyword` | Yes | 검색 키워드 |
| `markets` | No | 검색할 마켓 |
| `location` | No | 지역 |
| `minPrice` | No | 최소 가격 |
| `maxPrice` | No | 최대 가격 |
| `limit` | No | 최대 통합 결과 수 |

기본 `limit`:

```text
20
```

---

## Search All Markets

`markets`를 생략하면 현재 등록된 모든 Provider를 검색합니다.

```text
GET /api/v1/crawler/search?keyword=아이폰&limit=10
```

브라우저:

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&limit=10
```

curl:

```bash
curl "http://localhost:8080/api/v1/crawler/search?keyword=%EC%95%84%EC%9D%B4%ED%8F%B0&limit=10"
```

현재 등록된 Provider가 모두 활성화되어 있다면:

```text
DAANGN
JOONGNA
```

두 Provider가 실행됩니다.

---

# Daangn Test

## 당근만 검색

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=DAANGN&limit=5
```

브라우저:

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&markets=DAANGN&limit=5
```

---

## 당근 지역 검색

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=DAANGN&location=분당&limit=5
```

브라우저:

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&markets=DAANGN&location=분당&limit=5
```

확인할 항목:

```text
market
externalId
title
price
location
imageUrl
itemUrl
status
publishedAt
```

Provider 결과에서:

```json
{
  "market": "DAANGN",
  "success": true
}
```

가 확인되어야 합니다.

---

## Daangn Region Handling

당근의 지역 검색은 `Daangn Provider`가 담당합니다.

예를 들어 사용자가:

```text
분당
```

을 검색해도 실제 상품의 지역은:

```text
정자동
서현동
수내동
```

등으로 반환될 수 있습니다.

따라서 Core에서 다음과 같은 재필터링을 하면 안 됩니다.

```java
item.location()
    .contains(
        request.location()
    );
```

지역의 의미와 범위는 Provider가 처리합니다.

---

# Joongna Test

## 중고나라만 검색

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=JOONGNA&limit=5
```

브라우저:

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&markets=JOONGNA&limit=5
```

curl:

```bash
curl "http://localhost:8080/api/v1/crawler/search?keyword=%EC%95%84%EC%9D%B4%ED%8F%B0&markets=JOONGNA&limit=5"
```

---

## Joongna Check List

다음 값을 확인합니다.

```text
market == JOONGNA
success == true
```

각 상품에서는:

```text
externalId
title
price
imageUrl
itemUrl
status
```

를 확인합니다.

`externalId`는 URL 전체가 아닌 숫자 상품 ID여야 합니다.

좋은 예:

```text
231432565
```

잘못된 예:

```text
https://web.joongna.com/product/231432565
```

---

## Joongna Example

예상되는 상품 형태:

```json
{
  "externalId": "231432565",
  "market": "JOONGNA",
  "title": "아이폰6S 16GB 로즈골드 중고폰 배터리90% 기능정상 사진폰 277104",
  "price": 90000,
  "location": null,
  "imageUrl": "https://img2.joongna.com/...",
  "itemUrl": "https://web.joongna.com/product/231432565",
  "description": null,
  "status": "AVAILABLE",
  "publishedAt": null
}
```

현재 중고나라 검색 카드에서는 다음 정보가 제한적일 수 있습니다.

```text
location
description
publishedAt
```

---

# Multiple Markets Test

당근과 중고나라를 명시적으로 함께 검색할 수도 있습니다.

```text
GET /api/v1/crawler/search?keyword=아이폰&markets=DAANGN,JOONGNA&limit=10
```

브라우저:

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&markets=DAANGN,JOONGNA&limit=10
```

---

# Price Filter Test

## 최소 가격

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&minPrice=100000
```

## 최대 가격

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&maxPrice=500000
```

## 가격 범위

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&minPrice=100000&maxPrice=500000
```

가격 필터는 Core에서 처리합니다.

---

# Limit Test

```text
http://localhost:8080/api/v1/crawler/search?keyword=아이폰&limit=5
```

Provider가 실제로 가져온 상품 개수와 최종 `items` 개수는 다를 수 있습니다.

예:

```text
DAANGN Provider = 40 items
JOONGNA Provider = 50 items

Final items = 5
```

정상입니다.

`limit`은 최종 통합 결과에 적용됩니다.

---

# Response Structure

대표 응답 구조:

```json
{
  "keyword": "아이폰",
  "items": [],
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

`providers`:

```text
각 Provider의 실제 실행 결과
```

`items`:

```text
여러 Provider 결과를 통합한 최종 결과
```

입니다.

---

# Provider Enable / Disable Test

`application.yml`에서 Provider별 활성화 여부를 설정할 수 있습니다.

```yaml
used-market-crawler:
  daangn:
    enabled: true

  joongna:
    enabled: true
```

중고나라를 비활성화하려면:

```yaml
used-market-crawler:
  joongna:
    enabled: false
```

당근만 비활성화:

```yaml
used-market-crawler:
  daangn:
    enabled: false
```

비활성화된 Provider는 Spring Bean으로 등록되지 않습니다.

---

# Recommended Test Sequence

개발 중에는 다음 순서로 테스트하는 것을 권장합니다.

## 1. Build

```bash
./gradlew clean build
```

## 2. Boot

```bash
./gradlew :crawler-demo:bootRun
```

## 3. Joongna

```text
/api/v1/crawler/search?keyword=아이폰&markets=JOONGNA&limit=5
```

## 4. Daangn

```text
/api/v1/crawler/search?keyword=아이폰&markets=DAANGN&location=분당&limit=5
```

## 5. All Markets

```text
/api/v1/crawler/search?keyword=아이폰&limit=10
```

## 6. Price Filter

```text
/api/v1/crawler/search?keyword=아이폰&minPrice=100000&maxPrice=500000&limit=10
```

---

# Troubleshooting

## Provider에는 상품이 있는데 최상위 items가 비어 있음

예:

```text
providers[DAANGN].items = 40

items = []
```

Core에서 Provider 결과를 다시 필터링하고 있는지 확인합니다.

특히 `location`을 Core에서 문자 비교로 재필터링하면 안 됩니다.

---

## JOONGNA가 providers에 없음

다음을 확인합니다.

`settings.gradle`:

```gradle
include 'crawler-provider-joongna'
```

Starter:

```gradle
api project(':crawler-provider-joongna')
```

AutoConfiguration:

```text
JoongnaCrawlerConfig
JoongnaClient
JoongnaParser
JoongnaItemMapper
JoongnaUsedMarketProvider
```

Bean이 등록되어 있는지 확인합니다.

---

## 전체 검색인데 JOONGNA가 실행되지 않음

Demo Controller에서 다음처럼 DAANGN으로 고정되어 있으면 안 됩니다.

```java
Set.of(
    MarketType.DAANGN
)
```

전체 Provider 검색은:

```java
Set.of()
```

이어야 합니다.

---

## JOONGNA 결과가 최종 items 뒤로 밀림

현재 Joongna의:

```text
publishedAt
```

이 `null`일 수 있습니다.

Core가:

```text
publishedAt DESC
null last
```

정렬을 사용하기 때문에 timestamp가 존재하는 당근 결과가 먼저 배치될 수 있습니다.

이 경우 `providers[].items`를 통해 Joongna Provider 자체의 정상 동작 여부를 먼저 확인합니다.

---

# Crawling Policy

이 프로젝트는 공개적으로 접근 가능한 검색 데이터를 사용하는 것을 원칙으로 합니다.

우선순위:

1. Official API
2. Public HTML
3. Public embedded JSON
4. Public read-only endpoint

다음 기능은 구현하지 않습니다.

- CAPTCHA bypass
- Login bypass
- Authentication bypass
- Access-control bypass
- Bot protection evasion
- Private credential extraction
- Proxy-based blocking circumvention

사이트 구조가 변경되면 우회하는 것이 아니라 해당 Provider Parser를 수정합니다.

---

# Related Documentation

- [README](../README.md)
- [Adding a New Provider](./ADD_PROVIDER_GUIDE.md)
- [Crawler Architecture & Features](./CRAWLER_OVERVIEW.md)