# Hello Cucumber BDD

Cucumber와 Spring Boot로 배우는 **BDD(Behavior-Driven Development) 입문 프로젝트**입니다.

"테스트 코드는 써봤지만 Cucumber와 Gherkin은 처음"인 개발자를 대상으로,
**개념 → 문법 → 코드 연결 → 실행 원리 → 실습 → 트러블슈팅** 순서로 학습할 수 있도록 문서를 구성했습니다.
모든 예제는 이 저장소에 실제로 들어 있는 도서 관리(Book) REST API를 대상으로 합니다.

---

## 이 프로젝트로 배울 수 있는 것

- BDD가 해결하려는 문제와 Cucumber의 역할
- Gherkin 문법 전체 (`Feature`, `Scenario`, `Given/When/Then`, `Background`, `Scenario Outline`, `Rule`, Data Table, Doc String, Tag)
- Gherkin 문장을 Java 코드로 연결하는 Step Definition과 Cucumber Expression
- Cucumber + Spring Boot + Testcontainers 통합 테스트가 실행되는 내부 흐름
- 읽기 좋고 유지보수하기 쉬운 시나리오를 쓰는 방법
- 직접 시나리오를 추가하고, 실패시키고, 고쳐보는 실습

---

## 학습 로드맵

처음이라면 위에서부터 순서대로 읽는 것을 권장합니다. 각 문서 하단의 **이전 글 / 다음 글** 링크로 이동할 수 있습니다.

| 순서 | 문서 | 배우는 내용 | 예상 시간 |
|:---:|------|-------------|:---:|
| 01 | [BDD와 Cucumber 핵심 개념](docs/cucumber-concepts.md) | BDD가 왜 필요한가, Cucumber 동작 원리, 핵심 용어 | 20분 |
| 02 | [Gherkin 기초 문법](docs/gherkin-basics.md) | Feature, Scenario, Given/When/Then, And/But, 주석, 한국어 키워드 | 30분 |
| 03 | [Gherkin 심화 문법](docs/gherkin-advanced.md) | Background, Scenario Outline, Data Table, Doc String, Tag, Rule | 40분 |
| 04 | [Step Definition 작성법](docs/step-definitions.md) | Cucumber Expression, 파라미터 타입, DataTable 변환, 상태 공유 | 40분 |
| 05 | [프로젝트 구조와 실행 원리](docs/project-architecture.md) | JUnit Platform, Spring 연동, Hook, 설정, 리포트 | 30분 |
| 06 | [좋은 시나리오 작성법](docs/writing-good-scenarios.md) | 선언형 vs 명령형, 안티 패턴, 리뷰 체크리스트 | 30분 |
| 07 | [단계별 실습 가이드](docs/bdd-guide.md) | 실패 체험, 새 Step 구현, Outline, Tag, Hook 실습 | 90분 |
| 08 | [트러블슈팅과 FAQ](docs/troubleshooting.md) | 자주 만나는 오류 메시지와 해결법 | 필요 시 |
| 09 | [치트시트](docs/cheatsheet.md) | 문법과 API 한 장 요약 | 필요 시 |
| 부록 | [API 레퍼런스](docs/api-reference.md) | 테스트 대상 REST API 명세 | 필요 시 |

> 빠르게 손부터 움직여보고 싶다면 [01](docs/cucumber-concepts.md) → [02](docs/gherkin-basics.md) → [07](docs/bdd-guide.md) 순서로 읽고, 막히는 부분이 생길 때 03~05를 참고해도 좋습니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL 16 |
| DB Migration | Flyway |
| BDD Framework | Cucumber JVM 7.x |
| Test Runner | JUnit Platform (Suite Engine) |
| Test DB | Testcontainers |
| HTTP Client (테스트) | Spring `RestClient` |
| Assertion | AssertJ |
| Container | Docker Compose |

---

## 빠른 시작

### 사전 준비

- JDK 21
- Docker Desktop (실행 중이어야 함) — 테스트와 로컬 실행 모두 PostgreSQL 컨테이너를 사용합니다.
- IntelliJ IDEA 사용 시 **Gherkin**, **Cucumber for Java** 플러그인 설치를 권장합니다.

### 1. Cucumber BDD 테스트 실행

```bash
# Testcontainers가 PostgreSQL 컨테이너를 자동으로 띄웁니다
./gradlew test
```

콘솔에 다음과 같이 시나리오와 각 단계가 출력되면 성공입니다.

```
Scenario: 새 도서를 성공적으로 등록한다
  Given 도서 데이터베이스가 초기화되어 있다
  When 다음 정보로 도서를 등록한다:
  Then 응답 상태 코드는 201이다
  ...
```

### 2. 테스트 리포트 확인

```
build/reports/cucumber/cucumber.html   # Cucumber HTML 리포트 (시나리오 단위)
build/reports/tests/test/index.html    # Gradle/JUnit 리포트
```

### 3. 애플리케이션 직접 실행 (선택)

```bash
# Spring Boot Docker Compose Support가 compose.yaml의 PostgreSQL을 자동으로 시작합니다
./gradlew bootRun
```

실행 후 `http/books.http` 파일을 IntelliJ에서 열어 API를 직접 호출해볼 수 있습니다.
환경은 `http/http-client.env.json`의 `development`를 선택하세요.

---

## 프로젝트 구조

```
hello-cucumber-bdd/
├── build.gradle.kts                    # Gradle 빌드 설정 (Cucumber BOM 포함)
├── compose.yaml                        # Docker Compose (로컬 실행용 PostgreSQL)
├── src/
│   ├── main/
│   │   ├── java/com/example/cucumber/
│   │   │   ├── HelloCucumberApplication.java
│   │   │   ├── book/                   # 도서 도메인 (테스트 대상)
│   │   │   │   ├── Book.java           # JPA 엔티티
│   │   │   │   ├── BookRepository.java
│   │   │   │   ├── BookService.java
│   │   │   │   ├── BookController.java
│   │   │   │   ├── BookRequest.java    # 요청 DTO (Record + Bean Validation)
│   │   │   │   ├── BookResponse.java   # 응답 DTO (Record)
│   │   │   │   ├── BookNotFoundException.java
│   │   │   │   └── DuplicateIsbnException.java
│   │   │   └── common/
│   │   │       ├── ApiResponse.java        # 공통 응답 포맷
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/V1__create_books_table.sql
│   └── test/
│       ├── java/com/example/cucumber/
│       │   ├── CucumberIntegrationTest.java       # [실행 진입점] JUnit Suite
│       │   ├── CucumberSpringConfiguration.java   # [연결] Cucumber <-> Spring Boot
│       │   └── steps/
│       │       └── BookStepDefinitions.java       # [Glue] Gherkin 문장 -> Java 메서드
│       └── resources/
│           ├── features/
│           │   └── book_management.feature        # [명세] Gherkin 시나리오
│           ├── application-test.yml
│           └── junit-platform.properties          # Cucumber 설정
├── docs/                               # 학습 문서 (위 학습 로드맵 참고)
└── http/                               # IntelliJ HTTP Client 파일
```

Cucumber 학습에서 가장 중요한 파일은 `[ ]` 표시가 붙은 네 개입니다.
이 파일들이 어떻게 맞물려 동작하는지는 [05. 프로젝트 구조와 실행 원리](docs/project-architecture.md)에서 자세히 다룹니다.

---

## 맛보기: Gherkin 시나리오와 Step Definition

**명세 (`book_management.feature`)** — 비개발자도 읽을 수 있는 문장

```gherkin
Feature: 도서 관리
  도서관 관리자로서
  도서 목록을 관리하고 싶다
  그래서 이용자들이 도서를 쉽게 찾고 빌릴 수 있다

  Background:
    Given 도서 데이터베이스가 초기화되어 있다

  Scenario: ID로 특정 도서를 조회한다
    Given "Effective Java" 도서가 등록되어 있다
    When 해당 도서를 ID로 조회한다
    Then 응답 상태 코드는 200이다
    And 조회된 도서 제목은 "Effective Java"이다
```

**자동화 (`BookStepDefinitions.java`)** — 문장과 1:1로 연결되는 Java 메서드

```java
@Then("조회된 도서 제목은 {string}이다")
public void 조회된_도서_제목은_이다(String expectedTitle) {
    assertThat(parseBody().path("data").path("title").asText()).isEqualTo(expectedTitle);
}
```

`{string}` 자리에 들어간 `"Effective Java"`가 메서드 파라미터 `expectedTitle`로 전달됩니다.
이 연결 규칙이 Cucumber의 전부라고 해도 과언이 아닙니다. 첫 글에서 자세히 시작해봅시다.

---

## REST API 엔드포인트 (테스트 대상)

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/books` | 전체 도서 목록 조회 |
| `GET` | `/api/books/{id}` | 도서 단건 조회 |
| `POST` | `/api/books` | 도서 등록 |
| `PUT` | `/api/books/{id}` | 도서 수정 |
| `DELETE` | `/api/books/{id}` | 도서 삭제 |

자세한 명세는 [부록. API 레퍼런스](docs/api-reference.md)를 참고하세요.

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| (시작 페이지) | [학습 로드맵](#학습-로드맵) | [01. BDD와 Cucumber 핵심 개념 →](docs/cucumber-concepts.md) |
