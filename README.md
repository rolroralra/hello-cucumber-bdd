# Hello Cucumber BDD

Cucumber와 Spring Boot를 활용한 **BDD(Behavior-Driven Development) 테스트 실습 프로젝트**.

Gherkin 문법으로 시나리오를 작성하고, 이를 자동화된 통합 테스트로 실행하는 전 과정을 학습합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL 16 |
| DB Migration | Flyway |
| BDD Framework | Cucumber 7.x |
| Test Runner | JUnit Platform 5 |
| Test DB | Testcontainers |
| HTTP Test | REST Assured |
| Container | Docker Compose |

---

## 프로젝트 구조

```
hello-cucumber-bdd/
├── build.gradle.kts                    # Gradle 빌드 설정
├── compose.yaml                        # Docker Compose (PostgreSQL)
├── src/
│   ├── main/
│   │   ├── java/com/example/cucumber/
│   │   │   ├── HelloCucumberApplication.java
│   │   │   ├── book/                   # 도서 도메인
│   │   │   │   ├── Book.java           # JPA 엔티티
│   │   │   │   ├── BookRepository.java
│   │   │   │   ├── BookService.java
│   │   │   │   ├── BookController.java
│   │   │   │   ├── BookRequest.java    # 요청 DTO (Record)
│   │   │   │   ├── BookResponse.java   # 응답 DTO (Record)
│   │   │   │   ├── BookNotFoundException.java
│   │   │   │   └── DuplicateIsbnException.java
│   │   │   └── common/
│   │   │       ├── ApiResponse.java        # 공통 응답 포맷
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           └── V1__create_books_table.sql
│   └── test/
│       ├── java/com/example/cucumber/
│       │   ├── CucumberSpringConfiguration.java  # Cucumber + Spring 설정
│       │   ├── CucumberIntegrationTest.java       # 테스트 실행 진입점
│       │   └── steps/
│       │       └── BookStepDefinitions.java       # Step Definitions
│       └── resources/
│           ├── features/
│           │   └── book_management.feature        # Gherkin 시나리오
│           ├── application-test.yml
│           └── junit-platform.properties
├── docs/                               # 상세 문서
│   ├── bdd-guide.md
│   ├── cucumber-concepts.md
│   └── api-reference.md
└── http/                               # IntelliJ HTTP Client 파일
    ├── books.http
    └── http-client.env.json
```

---

## 빠른 시작

### 1. 애플리케이션 실행

```bash
# Docker Desktop이 실행 중이어야 합니다
# Spring Boot Docker Compose Support가 PostgreSQL을 자동으로 시작합니다
./gradlew bootRun
```

### 2. Cucumber BDD 테스트 실행

```bash
# Testcontainers가 자동으로 PostgreSQL 컨테이너를 시작합니다
# Docker Desktop이 실행 중이어야 합니다
./gradlew test
```

### 3. 테스트 리포트 확인

```
build/reports/cucumber/cucumber.html   # HTML 리포트
build/reports/tests/test/index.html    # JUnit 리포트
```

---

## REST API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/books` | 전체 도서 목록 조회 |
| `GET` | `/api/books/{id}` | 도서 단건 조회 |
| `POST` | `/api/books` | 도서 등록 |
| `PUT` | `/api/books/{id}` | 도서 수정 |
| `DELETE` | `/api/books/{id}` | 도서 삭제 |

자세한 API 문서: [docs/api-reference.md](docs/api-reference.md)

---

## BDD 시나리오 예시

```gherkin
Feature: 도서 관리
  도서관 관리자로서
  도서 목록을 관리하고 싶다
  그래서 이용자들이 도서를 쉽게 찾고 빌릴 수 있다

  Scenario: 새 도서를 성공적으로 등록한다
    Given 도서 데이터베이스가 초기화되어 있다
    When 다음 정보로 도서를 등록한다:
      | title  | Clean Code       |
      | author | Robert C. Martin |
      | isbn   | 978-0132350884   |
      | price  | 35.99            |
      | stock  | 10               |
    Then 응답 상태 코드는 201이다
    And 등록된 도서 제목은 "Clean Code"이다
```

Cucumber 학습 가이드: [docs/bdd-guide.md](docs/bdd-guide.md)

---

## IntelliJ IDEA HTTP Client

`http/books.http` 파일을 IntelliJ에서 열고 각 요청 옆의 실행 버튼을 클릭하여 API를 직접 테스트할 수 있습니다.

환경은 `http/http-client.env.json`에서 `development` 환경을 선택하세요.
