# BDD 실습 가이드

이 가이드는 프로젝트를 통해 Cucumber BDD를 단계별로 실습하는 방법을 설명합니다.

---

## 실습 1: 첫 Feature 파일 읽기

`src/test/resources/features/book_management.feature` 파일을 열어보세요.

### 주목할 점

1. **Feature 블록**: 기능의 목적을 사용자 스토리 형식으로 작성
   ```gherkin
   Feature: 도서 관리
     도서관 관리자로서          ← 사용자 역할
     도서 목록을 관리하고 싶다  ← 원하는 기능
     그래서 이용자들이 ...      ← 비즈니스 가치
   ```

2. **Background**: 모든 시나리오 전에 실행되는 공통 설정
   ```gherkin
   Background:
     Given 도서 데이터베이스가 초기화되어 있다
   ```
   > 각 테스트를 독립적으로 만들기 위해 DB를 초기화합니다.

3. **시나리오**: 하나의 구체적인 테스트 케이스
   ```gherkin
   Scenario: 새 도서를 성공적으로 등록한다
     When ...
     Then ...
   ```

---

## 실습 2: Step Definition 매핑 이해

`src/test/java/com/example/cucumber/steps/BookStepDefinitions.java`를 열어보세요.

### Gherkin → Java 매핑 규칙

```
Feature 파일의 단계 텍스트
         ↕ (정확히 일치해야 함)
@When("다음 정보로 도서를 등록한다:")
public void 다음_정보로_도서를_등록한다(DataTable dataTable) { ... }
```

### IntelliJ에서 확인하는 방법

Feature 파일에서 각 단계 왼쪽의 초록색 아이콘을 클릭하면 해당 Step Definition으로 이동합니다.
구현되지 않은 단계는 노란색으로 표시됩니다.

---

## 실습 3: 새 시나리오 추가하기

**목표**: "가격이 0 이하인 도서는 등록할 수 없다" 시나리오 추가

### Step 1: Feature 파일에 시나리오 작성

`book_management.feature`에 추가:

```gherkin
Scenario: 가격이 0인 도서는 등록할 수 없다
  When 가격이 0인 도서를 등록하려 한다
  Then 응답 상태 코드는 400이다
```

### Step 2: 테스트 실행 (RED 단계)

```bash
./gradlew test
```

> 새로 추가한 시나리오의 Step Definition이 없으므로 **PendingException**이 발생합니다.
> 이것이 TDD의 RED 단계입니다 — 테스트가 실패함을 확인.

### Step 3: Step Definition 구현 (GREEN 단계)

`BookStepDefinitions.java`에 추가:

```java
@When("가격이 0인 도서를 등록하려 한다")
public void 가격이_0인_도서를_등록하려_한다() {
    BookRequest request = new BookRequest(
            "Test Book",
            "Test Author",
            "978-0000000099",
            BigDecimal.ZERO,  // 가격 0
            5
    );

    lastResponse = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/books");
}
```

### Step 4: 다시 실행 (GREEN 확인)

```bash
./gradlew test
```

`BookRequest`의 `@DecimalMin(value = "0.0", inclusive = false)` 검증 덕분에 400이 반환되어 테스트가 통과합니다.

---

## 실습 4: Scenario Outline으로 데이터 기반 테스트

같은 시나리오를 여러 데이터로 테스트하고 싶을 때 사용합니다.

```gherkin
Scenario Outline: 유효하지 않은 데이터로 도서를 등록하면 실패한다
  When 다음 데이터로 도서를 등록하려 한다:
    | title   | <title>  |
    | author  | <author> |
    | isbn    | <isbn>   |
    | price   | <price>  |
    | stock   | <stock>  |
  Then 응답 상태 코드는 400이다

  Examples:
    | title      | author   | isbn           | price | stock | 설명              |
    |            | 저자     | 978-0000000001 | 19.99 | 5     | 제목 없음         |
    | 도서 제목  |          | 978-0000000002 | 19.99 | 5     | 저자 없음         |
    | 도서 제목  | 저자     | 978-0000000003 | 0.00  | 5     | 가격 0            |
    | 도서 제목  | 저자     | 978-0000000004 | 19.99 | -1    | 재고 음수         |
```

---

## 실습 5: 태그로 테스트 분류하기

### Feature 파일에 태그 추가

```gherkin
@smoke
Scenario: 새 도서를 성공적으로 등록한다
  ...

@slow @database
Scenario: 전체 도서 목록을 조회한다
  ...
```

### 특정 태그만 실행

`CucumberIntegrationTest.java`에 필터 추가:

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.cucumber")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke")  // 추가
public class CucumberIntegrationTest {}
```

또는 Gradle 실행 시:

```bash
./gradlew test -Dcucumber.filter.tags="@smoke"
```

---

## 실습 6: Cucumber 리포트 분석

테스트 실행 후 `build/reports/cucumber/cucumber.html`을 브라우저로 열어보세요.

### 리포트에서 확인할 것

- **Feature 별 통과/실패 현황**
- **각 시나리오의 단계별 실행 결과**
- **실패한 단계의 스택 트레이스**
- **전체 실행 시간**

---

## 자주 발생하는 오류

### 1. "No step definition found"

```
io.cucumber.core.runtime.UndefinedStepException
```

**원인**: Feature 파일의 단계 텍스트와 `@Given`/`@When`/`@Then` 어노테이션의 문자열이 일치하지 않음.

**해결**: IntelliJ의 노란색 밑줄 위에서 `Alt+Enter` → "Create step definition" 자동 생성 활용.

### 2. "Ambiguous step definitions"

**원인**: 두 개 이상의 Step Definition이 같은 Gherkin 단계와 매칭됨.

**해결**: 정규식 패턴을 더 구체적으로 작성하거나, 중복된 Step Definition을 제거.

### 3. 테스트 간 데이터 오염

**원인**: `Background`의 DB 초기화가 올바르게 실행되지 않음.

**해결**: `@Transactional`을 Cucumber에서 사용하면 예상과 다르게 동작할 수 있으므로 명시적인 `deleteAll()`을 사용합니다.

---

## 참고 자료

- [Cucumber 공식 문서](https://cucumber.io/docs)
- [Cucumber JVM GitHub](https://github.com/cucumber/cucumber-jvm)
- [Gherkin 레퍼런스](https://cucumber.io/docs/gherkin/reference)
- [프로젝트 개념 문서](cucumber-concepts.md)
- [API 레퍼런스](api-reference.md)
