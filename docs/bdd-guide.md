# 07. 단계별 실습 가이드

> **이 글에서 할 것**
> 지금까지 배운 내용을 이 프로젝트에서 직접 손으로 해봅니다. 각 실습은 앞 실습에 이어서 진행됩니다.
>
> | 실습 | 내용 | 배우는 것 |
> |:---:|------|-----------|
> | 0 | 테스트 실행과 리포트 보기 | 실행 방법, 리포트 읽기 |
> | 1 | 기존 Step만으로 시나리오 추가 | Step 재사용 |
> | 2 | 일부러 실패시키기 | 실패 메시지 읽기, skipped 상태 |
> | 3 | 새 Step 구현하기 (Undefined → Green) | 스니펫, Step Definition 작성 |
> | 4 | Scenario Outline 만들기 | Examples, 플레이스홀더 |
> | 5 | Tag로 골라서 실행하기 | Tag, Tag Expression, Gradle 설정 |
> | 6 | 실패 시 응답을 리포트에 첨부하기 | `@After` Hook, `Scenario` 객체 |
> | 7 | `@DataTableType`으로 리팩터링 | DataTable 객체 변환 |
> | 8 | 도전 과제: 새 기능 시나리오 처음부터 작성 | 종합 |
>
> **선수 지식**: 01~06장 (막히면 해당 장으로 돌아가세요)

---

## 실습 0. 테스트 실행과 리포트 보기

### 0-1. 실행

Docker Desktop이 실행 중인지 확인한 뒤 프로젝트 루트에서 실행합니다.

```bash
./gradlew test
```

처음 실행하면 PostgreSQL 이미지를 내려받느라 시간이 조금 걸립니다.

### 0-2. 콘솔 출력 읽기

`pretty` 플러그인이 Scenario와 Step을 출력합니다. 각 줄 오른쪽 주석에는 Scenario의 Feature 파일 위치와, 각 Step에 매칭된 Step Definition 메서드가 표시됩니다.

```
Scenario: 도서를 삭제한다                                # classpath:features/book_management.feature:<줄번호>
  Given 도서 데이터베이스가 초기화되어 있다                # com.example.cucumber.steps.BookStepDefinitions.도서_데이터베이스가_초기화되어_있다()
  Given "Test-Driven Development" 도서가 등록되어 있다   # com.example.cucumber.steps.BookStepDefinitions.도서가_등록되어_있다(java.lang.String)
  When 해당 도서를 삭제한다                               # ...
  Then 응답 상태 코드는 204이다                           # ...
  And 해당 도서는 더 이상 조회되지 않는다                   # ...
```

### 0-3. HTML 리포트 열기

```bash
open build/reports/cucumber/cucumber.html        # macOS
# 또는 브라우저로 파일을 직접 엽니다
```

확인할 것:

- [ ] Feature 이름과 설명(Description)이 보인다
- [ ] Scenario 10개가 모두 초록색(passed)이다
- [ ] Scenario를 펼치면 Background Step이 각 Scenario 맨 앞에 포함되어 있다
- [ ] Data Table도 리포트에 그대로 표시된다

---

## 실습 1. 기존 Step만으로 시나리오 추가하기

**목표**: Java 코드를 한 줄도 쓰지 않고 새 테스트를 만든다.

`src/test/resources/features/book_management.feature`의 **도서 수정 시나리오** 구역에 다음을 추가합니다.

```gherkin
  Scenario: 도서 재고를 0권으로 수정할 수 있다
    Given 재고가 3권인 "Clean Architecture" 도서가 등록되어 있다
    When 해당 도서 재고를 0으로 수정한다
    Then 응답 상태 코드는 200이다
    And 수정된 도서 재고는 0권이다
```

```bash
./gradlew test
```

**결과**: Scenario가 11개로 늘었고 모두 통과합니다.

> **생각해보기**: 이 Scenario는 "재고 0은 유효한 값이다"라는 비즈니스 규칙을 문서화합니다.
> `BookRequest`의 `@Min(value = 0)` 검증 규칙과 일치하는지 확인해보세요.

---

## 실습 2. 일부러 실패시키기

**목표**: 실패 메시지를 읽는 법과 skipped 상태를 직접 확인한다.

### 2-1. 기대값을 틀리게 바꾸기

"새 도서를 성공적으로 등록한다" Scenario의 `201`을 `200`으로 바꿉니다.

```gherkin
    Then 응답 상태 코드는 200이다
    And 등록된 도서 제목은 "Clean Code"이다
    And 등록된 도서 재고는 10권이다
```

```bash
./gradlew test
```

### 2-2. 실패 메시지 읽기

```
org.opentest4j.AssertionFailedError: [HTTP 상태 코드]
expected: 200
 but was: 201
	at com.example.cucumber.steps.BookStepDefinitions.응답_상태_코드는_이다(BookStepDefinitions.java:...)
	at ✽.응답 상태 코드는 200이다(classpath:features/book_management.feature:...)
```

- `[HTTP 상태 코드]` : Step Definition에서 `.as("HTTP 상태 코드")`로 붙인 설명입니다. 검증에 설명을 붙이면 실패 메시지가 훨씬 친절해집니다.
- `at ✽.응답 상태 코드는 200이다(...feature:줄번호)` : **Feature 파일의 몇 번째 줄**에서 실패했는지 알려줍니다.

### 2-3. HTML 리포트에서 확인

- 실패한 Step은 빨간색입니다.
- 그 아래 `And 등록된 도서 제목은...`, `And 등록된 도서 재고는...` 두 Step은 **skipped** 상태입니다.
  앞 Step이 실패하면 같은 Scenario의 나머지는 실행되지 않습니다.
- 다른 Scenario들은 정상적으로 실행되어 통과했습니다. Scenario는 서로 독립적이기 때문입니다.

### 2-4. 되돌리기

`200`을 다시 `201`로 바꿉니다. (실습 6에서 이 실패를 한 번 더 사용합니다.)

---

## 실습 3. 새 Step 구현하기 (Undefined → Green)

**목표**: 아직 없는 문장을 쓰고, Cucumber의 안내에 따라 Step Definition을 구현한다.
이것이 BDD에서 **바깥에서 안으로(Outside-in)** 개발하는 기본 흐름입니다.

### 3-1. 시나리오 먼저 작성 (RED)

도서 등록 시나리오 구역에 추가합니다.

```gherkin
  Scenario: 가격이 0인 도서는 등록할 수 없다
    When 가격이 0인 도서를 등록하려 한다
    Then 응답 상태 코드는 400이다
    And 오류 메시지에 "price" 필드 검증 오류가 포함된다
```

### 3-2. 실행하고 메시지 확인

```bash
./gradlew test
```

Scenario가 **undefined** 상태로 실패하고, 콘솔에 다음과 비슷한 스니펫이 출력됩니다.

```
You can implement this step using the snippet(s) below:

@When("가격이 {int}인 도서를 등록하려 한다")
public void 가격이_인_도서를_등록하려_한다(Integer int1) {
    // Write code here that turns the phrase above into concrete actions
    throw new io.cucumber.java.PendingException();
}
```

Cucumber는 `0`을 보고 `{int}`로 추정했습니다. 하지만 가격은 `19.99`처럼 소수가 될 수 있으므로 `{bigdecimal}`로 바꾸는 것이 맞습니다.
**스니펫은 출발점일 뿐, 그대로 쓸 필요는 없습니다.**

### 3-3. Step Definition 구현 (GREEN)

`BookStepDefinitions.java`의 `// ── 도서 등록 ──` 구역에 추가합니다.

```java
@When("가격이 {bigdecimal}인 도서를 등록하려 한다")
public void 가격이_N인_도서를_등록하려_한다(BigDecimal price) {
    BookRequest request = new BookRequest("가격 테스트 도서", "테스트 저자", "978-0000000001",
            price, 5);
    lastResponse = execute(() -> restClient.post()
            .uri("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(String.class));
}
```

포인트:

- 시나리오에 드러나지 않은 값(제목, 저자, ISBN, 재고)은 **유효한 기본값**으로 채웁니다. 그래야 가격 외의 이유로 실패하지 않습니다.
- 결과는 `lastResponse`에 저장합니다. 그래야 기존 Then Step들(`응답 상태 코드는 {int}이다`)이 그대로 동작합니다.
- 4xx 응답도 예외 없이 받기 위해 기존 헬퍼 `execute()`를 재사용합니다.

### 3-4. 다시 실행

```bash
./gradlew test
```

`BookRequest`의 `@DecimalMin(value = "0.0", inclusive = false)` 검증 덕분에 400이 반환되고,
응답 `data`에 `price` 키가 있으므로 Scenario가 통과합니다.

### 3-5. Step 재사용 확인

방금 만든 Step은 다른 가격에도 바로 쓸 수 있습니다. 다음 Scenario를 추가해보세요. 코드 수정 없이 통과해야 합니다.

```gherkin
  Scenario: 가격이 0.01이면 등록할 수 있다
    When 가격이 0.01인 도서를 등록하려 한다
    Then 응답 상태 코드는 201이다
```

---

## 실습 4. Scenario Outline 만들기

**목표**: 같은 흐름의 검증 실패 케이스를 표 하나로 정리한다.

### 4-1. Outline 추가

새로운 Step 없이 기존 Step과 플레이스홀더만으로 작성합니다.
`다음 정보로 도서를 등록한다:` 의 **Data Table 셀 안에서도** `<변수>`가 치환된다는 점을 활용합니다.

```gherkin
  Scenario Outline: 유효하지 않은 <field> 값으로는 도서를 등록할 수 없다 (<reason>)
    When 다음 정보로 도서를 등록한다:
      | title  | 테스트 도서 |
      | author | 테스트 저자 |
      | isbn   | <isbn>     |
      | price  | <price>    |
      | stock  | <stock>    |
    Then 응답 상태 코드는 400이다
    And 오류 메시지에 "<field>" 필드 검증 오류가 포함된다

    Examples:
      | field | isbn           | price | stock | reason         |
      | price | 978-0000000011 | 0     | 5     | 가격 0          |
      | price | 978-0000000012 | -5.00 | 5     | 가격 음수       |
      | stock | 978-0000000013 | 19.99 | -1    | 재고 음수       |
      | isbn  | abc            | 19.99 | 5     | ISBN 형식 오류  |
```

### 4-2. 실행 및 확인

```bash
./gradlew test
```

- HTML 리포트에 Scenario가 **4개** 추가되었는지 확인합니다.
- 제목의 `<field>`, `<reason>`이 각 행의 값으로 바뀌어 표시되는지 확인합니다.

### 4-3. 생각해보기

<details>
<summary>왜 Examples에 "제목이 빈 값" 케이스를 넣지 않았을까요?</summary>

Data Table의 **빈 셀은 `null`로 변환**됩니다. 이 Step은 `price`, `stock`을 `new BigDecimal(...)`, `Integer.parseInt(...)`로 바로 변환하므로,
해당 셀을 비우면 서버에 요청을 보내기도 전에 테스트 코드에서 예외가 납니다.
또한 "제목 없음" 케이스는 이미 `제목 없이 도서를 등록하면 실패한다` Scenario가 다루고 있습니다.

빈 값을 Outline으로 다루고 싶다면 Step Definition에서 `null`을 처리하도록 바꾸거나,
`[blank]` 같은 표시를 쓰고 `@DataTableType(replaceWithEmptyString = "[blank]")`를 적용하는 방법이 있습니다. ([03장](gherkin-advanced.md) 3.4절)
</details>

---

## 실습 5. Tag로 골라서 실행하기

**목표**: 원하는 Scenario만 빠르게 실행한다.

### 5-1. Tag 붙이기

```gherkin
@book
Feature: 도서 관리
  ...

  @smoke
  Scenario: 새 도서를 성공적으로 등록한다
    ...

  @smoke
  Scenario: 전체 도서 목록을 조회한다
    ...

  @validation
  Scenario Outline: 유효하지 않은 <field> 값으로는 도서를 등록할 수 없다 (<reason>)
    ...
```

### 5-2. Gradle이 Tag 필터를 테스트 JVM에 전달하도록 설정

`build.gradle.kts`의 `tasks.withType<Test>` 블록을 다음과 같이 바꿉니다.
(이유는 [05장 6.2절](project-architecture.md#6-특정-시나리오만-실행하기) 참고)

```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    System.getProperty("cucumber.filter.tags")?.let { systemProperty("cucumber.filter.tags", it) }
}
```

### 5-3. 실행

```bash
# @smoke만 (2개)
./gradlew cleanTest test -Dcucumber.filter.tags="@smoke"

# 검증 관련만 (Outline에서 생성된 4개)
./gradlew cleanTest test -Dcucumber.filter.tags="@validation"

# 스모크와 검증을 제외한 나머지
./gradlew cleanTest test -Dcucumber.filter.tags="@book and not (@smoke or @validation)"
```

`cleanTest`는 Gradle이 "이미 실행한 테스트"라며 건너뛰지 않게 하기 위해 붙였습니다.

> Gradle/JUnit 리포트(`build/reports/tests/test/index.html`)에서는 필터에 걸러진 Scenario도 목록에 남고 **skipped**로 표시됩니다.
> 예를 들어 `@validation`으로 실행하면 4개는 실행되고 나머지는 skipped로 집계됩니다. 실패가 아니므로 걱정하지 않아도 됩니다.

### 5-4. IntelliJ에서

Feature 파일에서 Scenario 옆의 실행 아이콘을 누르면 해당 Scenario 하나만 실행됩니다.
Tag 필터는 Cucumber Run Configuration의 **Tags** 항목에 `@smoke`처럼 입력합니다.

---

## 실습 6. 실패 시 응답을 리포트에 첨부하기

**목표**: 실패 원인을 빠르게 파악할 수 있도록 Hook으로 디버깅 정보를 남긴다.

### 6-1. `@After` Hook 추가

`BookStepDefinitions.java`에 추가합니다.

```java
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

@After
public void 실패하면_마지막_응답을_첨부한다(Scenario scenario) {
    if (scenario.isFailed() && lastResponse != null) {
        scenario.log("마지막 응답 상태: " + lastResponse.getStatusCode());
        scenario.attach(String.valueOf(lastResponse.getBody()), "application/json", "last-response.json");
    }
}
```

### 6-2. 실습 2의 실패를 다시 만들기

"새 도서를 성공적으로 등록한다"의 `201`을 `200`으로 바꾸고 실행합니다.

### 6-3. 리포트 확인

HTML 리포트의 실패한 Scenario 하단에 `마지막 응답 상태: 201 CREATED` 로그와 `last-response.json` 첨부가 보입니다.
첨부를 열면 서버가 실제로 돌려준 JSON 본문을 확인할 수 있습니다.

확인했다면 `200`을 다시 `201`로 되돌립니다.

> **생각해보기**: 이 Hook은 성공한 Scenario에서는 아무것도 하지 않습니다.
> 모든 Scenario에 첨부하면 리포트가 너무 커지기 때문입니다.

---

## 실습 7. `@DataTableType`으로 리팩터링 (심화)

**목표**: `Map` → `BookRequest` 변환 코드를 한 곳으로 모은다. 동작은 바꾸지 않는다(리팩터링).

### 7-1. 변환기 클래스 추가

`src/test/java/com/example/cucumber/steps/BookDataTableTypes.java`

```java
package com.example.cucumber.steps;

import com.example.cucumber.book.BookRequest;
import io.cucumber.java.DataTableType;

import java.math.BigDecimal;
import java.util.Map;

public class BookDataTableTypes {

    @DataTableType
    public BookRequest bookRequest(Map<String, String> row) {
        return new BookRequest(
                row.get("title"),
                row.get("author"),
                row.get("isbn"),
                new BigDecimal(row.get("price")),
                Integer.parseInt(row.get("stock"))
        );
    }
}
```

### 7-2. Step Definition 수정

`다음_도서들이_등록되어_있다`를 다음과 같이 바꿉니다.

```java
@Given("다음 도서들이 등록되어 있다:")
public void 다음_도서들이_등록되어_있다(List<BookRequest> books) {
    for (BookRequest request : books) {
        execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
    }
}
```

### 7-3. 리팩터링 검증

```bash
./gradlew test
```

모든 Scenario가 **리팩터링 전과 똑같이** 통과해야 합니다. Feature 파일은 한 글자도 바뀌지 않았다는 점에 주목하세요.
명세(Feature)와 구현(Step Definition)이 분리되어 있기 때문에 가능한 일입니다.

---

## 실습 8. 도전 과제: 새 기능 시나리오 처음부터 작성하기

**요구사항** (기획자에게서 받은 문장이라고 생각하세요)

> 관리자는 도서의 제목을 바꿀 수 있어야 합니다. 제목을 빈 값으로 바꾸려고 하면 거절되어야 합니다.

### 과제

1. Example Mapping처럼 **규칙(Rule)** 과 **예시(Example)** 를 먼저 정리합니다.
2. `src/test/resources/features/book_title_update.feature` 파일을 **새로** 만들고 `Rule`을 사용해 작성합니다.
3. 기존 Step을 최대한 재사용하고, 꼭 필요한 Step만 새로 구현합니다.
4. `./gradlew test`로 통과를 확인합니다.

<details>
<summary>힌트</summary>

- 새 Feature 파일에도 DB 초기화 Background가 필요합니다. 기존 Step(`도서 데이터베이스가 초기화되어 있다`)을 재사용하세요.
- 제목 수정은 `해당 도서 가격을 {double}로 수정한다`의 구현(현재 값 조회 → 일부만 바꿔 PUT)을 참고하세요.
- `{string}(으)로` 처럼 선택 텍스트를 쓰면 `"클린 코드"로`와 `"Clean Code 2판"으로`를 하나의 패턴으로 처리할 수 있습니다.
</details>

<details>
<summary>예시 답안 — Feature</summary>

```gherkin
Feature: 도서 제목 수정
  도서관 관리자로서
  잘못 등록된 도서 제목을 고치고 싶다
  그래서 이용자가 올바른 제목으로 도서를 찾을 수 있다

  Background:
    Given 도서 데이터베이스가 초기화되어 있다

  Rule: 관리자는 도서 제목을 바꿀 수 있다

    Scenario: 도서 제목을 수정한다
      Given "Clean Cod" 도서가 등록되어 있다
      When 해당 도서 제목을 "Clean Code"로 수정한다
      Then 응답 상태 코드는 200이다
      And 수정된 도서 제목은 "Clean Code"이다

  Rule: 제목은 비워둘 수 없다

    Scenario: 제목을 빈 값으로 수정하면 거절된다
      Given "Clean Code" 도서가 등록되어 있다
      When 해당 도서 제목을 ""로 수정한다
      Then 응답 상태 코드는 400이다
      And 오류 메시지에 "title" 필드 검증 오류가 포함된다
```
</details>

<details>
<summary>예시 답안 — Step Definition</summary>

`BookStepDefinitions.java`에 추가합니다.

```java
@When("해당 도서 제목을 {string}(으)로 수정한다")
public void 해당_도서_제목을_수정한다(String newTitle) {
    ResponseEntity<String> current = execute(() -> restClient.get()
            .uri("/api/books/{id}", savedBookId)
            .retrieve()
            .toEntity(String.class));
    try {
        JsonNode data = objectMapper.readTree(current.getBody()).path("data");
        BookRequest request = new BookRequest(
                newTitle, data.path("author").asText(),
                data.path("isbn").asText(),
                new BigDecimal(data.path("price").asText()),
                data.path("stock").asInt()
        );
        lastResponse = execute(() -> restClient.put()
                .uri("/api/books/{id}", savedBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
    } catch (Exception e) {
        throw new RuntimeException("도서 제목 수정 중 오류", e);
    }
}

@Then("수정된 도서 제목은 {string}이다")
public void 수정된_도서_제목은_이다(String expectedTitle) {
    assertThat(parseBody().path("data").path("title").asText()).isEqualTo(expectedTitle);
}
```

- `{string}`은 빈 따옴표 `""`도 매칭합니다. 제목이 빈 값으로 전달되므로 `@NotBlank` 검증에 걸려 400이 반환됩니다.
- 새 Feature 파일에서도 기존 Step(`"..." 도서가 등록되어 있다`, `응답 상태 코드는 {int}이다` 등)이 그대로 동작합니다.
  Step Definition은 Feature 파일에 묶여 있지 않고 **전역**이기 때문입니다.
</details>

---

## 실습 완료 체크리스트

- [ ] 콘솔과 HTML 리포트에서 Scenario/Step 결과를 읽을 수 있다
- [ ] 기존 Step을 조합해 새 Scenario를 만들 수 있다
- [ ] 실패 메시지에서 실패한 Feature 줄 번호와 원인을 찾을 수 있다
- [ ] undefined Step의 스니펫을 보고 Step Definition을 구현할 수 있다
- [ ] Scenario Outline과 Examples로 여러 사례를 표현할 수 있다
- [ ] Tag Expression으로 원하는 Scenario만 실행할 수 있다
- [ ] Hook으로 실패 시 디버깅 정보를 남길 수 있다
- [ ] Feature를 건드리지 않고 Step Definition을 리팩터링할 수 있다
- [ ] 요구사항 문장에서 Rule과 Example을 뽑아 새 Feature를 작성할 수 있다

모두 체크했다면 Cucumber 입문 과정을 마친 것입니다.
막혔던 부분은 [08. 트러블슈팅과 FAQ](troubleshooting.md)에서 찾아보세요.

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| [← 06. 좋은 시나리오 작성법](writing-good-scenarios.md) | [학습 로드맵](../README.md#학습-로드맵) | [08. 트러블슈팅과 FAQ →](troubleshooting.md) |
