# 03. Gherkin 심화 문법

> **이 글에서 배울 것**
> - `Background`: 반복되는 Given을 한 곳에 모으기
> - `Scenario Outline` + `Examples`: 같은 시나리오를 여러 데이터로 실행하기
> - Data Table: 표 형태의 데이터를 Step에 넘기기
> - Doc String: 여러 줄 텍스트(JSON 등)를 Step에 넘기기
> - Tag: 시나리오 분류와 선택 실행
> - `Rule`: 하나의 Feature 안에서 비즈니스 규칙별로 묶기
>
> **선수 지식**: [02. Gherkin 기초 문법](gherkin-basics.md)

---

## 1. Background — 공통 전제 조건

같은 Feature의 모든 Scenario가 똑같은 `Given`으로 시작한다면 `Background`로 뽑아낼 수 있습니다.

```gherkin
Feature: 도서 관리

  Background:
    Given 도서 데이터베이스가 초기화되어 있다

  Scenario: 새 도서를 성공적으로 등록한다
    When 다음 정보로 도서를 등록한다:
      ...

  Scenario: 존재하지 않는 ID로 조회하면 404를 반환한다
    When ID 999999로 도서를 조회한다
    Then 응답 상태 코드는 404이다
```

Cucumber는 실행 시 Background를 **각 Scenario 앞에 복사해 붙인 것처럼** 실행합니다.
즉 위 예에서 두 번째 Scenario는 실제로 이렇게 실행됩니다.

```gherkin
  Scenario: 존재하지 않는 ID로 조회하면 404를 반환한다
    Given 도서 데이터베이스가 초기화되어 있다     <- Background에서 온 Step
    When ID 999999로 도서를 조회한다
    Then 응답 상태 코드는 404이다
```

### 규칙과 권장 사항

- Feature(또는 Rule) 하나에 Background는 **최대 하나**입니다.
- Background는 `@Before` Hook **다음**, Scenario의 첫 Step **이전**에 실행됩니다. (실행 순서는 [05장](project-architecture.md)에서 자세히)
- **짧게 유지하세요.** 4줄이 넘어가면 읽는 사람이 Scenario를 이해하기 위해 위로 스크롤해야 합니다.
- **독자가 알아야 하는 맥락만** 넣습니다. "DB 초기화"처럼 기술적인 준비 작업은 Hook으로 옮기는 것도 방법입니다.

> 이 프로젝트는 "각 Scenario가 깨끗한 DB에서 시작한다"는 사실을 독자에게 보여주기 위해
> DB 초기화를 Background에 두었습니다. Hook으로 옮기면 Feature 파일은 더 깔끔해지지만 이 사실이 숨겨집니다.
> 둘 다 정답이 될 수 있는 설계 선택입니다.

---

## 2. Scenario Outline — 데이터만 바꿔서 반복하기

입력값만 다르고 흐름은 같은 Scenario가 여러 개 필요할 때 사용합니다.

### Outline 없이 쓰면

```gherkin
Scenario: 가격이 0이면 등록할 수 없다
  When 다음 정보로 도서를 등록한다:
    | title  | 테스트 도서     |
    | author | 테스트 저자     |
    | isbn   | 978-0000000001 |
    | price  | 0              |
    | stock  | 5              |
  Then 응답 상태 코드는 400이다

Scenario: 가격이 음수이면 등록할 수 없다
  When 다음 정보로 도서를 등록한다:
    ... (price만 -5로 바뀐 똑같은 내용)
```

### Outline으로 쓰면

```gherkin
Scenario Outline: 유효하지 않은 <field> 값으로는 도서를 등록할 수 없다
  When 다음 정보로 도서를 등록한다:
    | title  | 테스트 도서     |
    | author | 테스트 저자     |
    | isbn   | <isbn>         |
    | price  | <price>        |
    | stock  | <stock>        |
  Then 응답 상태 코드는 400이다
  And 오류 메시지에 "<field>" 필드 검증 오류가 포함된다

  Examples:
    | isbn           | price | stock | field |
    | 978-0000000001 | 0     | 5     | price |
    | 978-0000000002 | -5.00 | 5     | price |
    | 978-0000000003 | 19.99 | -1    | stock |
    | abc            | 19.99 | 5     | isbn  |
```

동작 원리:

1. `Examples` 표의 **헤더 행**이 변수 이름이 됩니다. (`isbn`, `price`, `stock`, `field`)
2. **나머지 각 행마다** Scenario가 하나씩 생성됩니다. 위 예는 Scenario 4개가 실행됩니다.
3. `<변수명>`이 해당 행의 값으로 치환됩니다. 치환은 다음 위치에서 모두 일어납니다.
   - Step 문장
   - Data Table의 셀 (위 예처럼)
   - Doc String 내용
   - Scenario Outline의 **제목** (리포트에서 각 행을 구분하기 쉬워짐)

> 위 예제는 이 프로젝트의 기존 Step만으로 동작합니다. [07. 실습 가이드](bdd-guide.md)에서 직접 추가해봅니다.

### 여러 개의 Examples 블록

Examples 블록에 이름과 Tag를 붙여 여러 개로 나눌 수 있습니다.

```gherkin
Scenario Outline: 가격 검증
  When 가격이 <price>인 도서를 등록하려 한다
  Then 응답 상태 코드는 <status>이다

  @happy
  Examples: 유효한 가격
    | price | status |
    | 0.01  | 201    |
    | 19.99 | 201    |

  @negative
  Examples: 유효하지 않은 가격
    | price | status |
    | 0     | 400    |
    | -1    | 400    |
```

### 언제 Outline을 쓰지 말아야 할까?

- Examples 행이 **10개를 넘어가면** 비즈니스 예시라기보다 데이터 테스트에 가깝습니다. 경계값 전수 검사는 단위 테스트로 옮기세요.
- 행마다 **기대 결과의 성격이 다르다면**(성공/실패가 섞임) 의도가 흐려집니다. 성공과 실패를 별도의 Outline이나 Examples 블록으로 나누세요.
- `Scenario Template`과 `Scenarios`는 각각 `Scenario Outline`과 `Examples`의 동의어입니다.

---

## 3. Data Table — 표 형태로 데이터 넘기기

Step 바로 아래에 `|`로 구분된 표를 붙이면 그 표가 Step Definition 메서드의 **마지막 파라미터**(`DataTable`)로 전달됩니다.
이 프로젝트에서는 두 가지 모양을 모두 사용합니다.

### 3.1 세로형 (키-값) 테이블

하나의 객체를 표현할 때 적합합니다.

```gherkin
When 다음 정보로 도서를 등록한다:
  | title  | Clean Code       |
  | author | Robert C. Martin |
  | isbn   | 978-0132350884   |
  | price  | 35.99            |
  | stock  | 10               |
```

```java
@When("다음 정보로 도서를 등록한다:")
public void 다음_정보로_도서를_등록한다(DataTable dataTable) {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    // data.get("title") -> "Clean Code"
}
```

### 3.2 가로형 (헤더 + 행) 테이블

같은 종류의 객체 여러 개를 표현할 때 적합합니다.

```gherkin
Given 다음 도서들이 등록되어 있다:
  | title                    | author           | isbn           | price | stock |
  | Clean Code               | Robert C. Martin | 978-0132350884 | 35.99 | 10    |
  | The Pragmatic Programmer | Andrew Hunt      | 978-0135957059 | 42.99 | 5     |
```

```java
@Given("다음 도서들이 등록되어 있다:")
public void 다음_도서들이_등록되어_있다(DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    // rows.get(0).get("title") -> "Clean Code"
}
```

### 3.3 DataTable 변환 메서드 요약

| 표 모양 | 메서드 | 결과 타입 |
|---------|--------|-----------|
| 1열 목록 | `asList()` | `List<String>` |
| 2열 키-값 | `asMap(String.class, String.class)` | `Map<String, String>` |
| 헤더 + 여러 행 | `asMaps()` | `List<Map<String, String>>` |
| 헤더 없는 2차원 | `asLists()` | `List<List<String>>` |
| 헤더 + 여러 행 → 객체 | `List<BookRequest>` 파라미터 + `@DataTableType` | `List<BookRequest>` ([04장](step-definitions.md)) |

### 3.4 주의할 점

- 셀 안의 앞뒤 공백은 잘려나갑니다. `|  Clean Code  |` → `"Clean Code"`
- **빈 셀은 `null`로 변환됩니다.** (빈 문자열 `""`이 아님) 빈 문자열이 필요하면 `[blank]` 같은 표시를 쓰고 `@DataTableType(replaceWithEmptyString = "[blank]")`로 변환합니다.
- 셀 안에 `|` 문자를 쓰려면 `\|`로 이스케이프합니다.
- Step 문장 끝의 콜론(`:`)은 문법이 아니라 **문장의 일부**입니다. 이 프로젝트처럼 콜론을 붙였다면 Step Definition 패턴에도 콜론이 있어야 합니다.

---

## 4. Doc String — 여러 줄 텍스트 넘기기

JSON 요청 본문, 이메일 내용처럼 **여러 줄로 된 텍스트**를 넘길 때 사용합니다. `"""`(또는 ` ``` `)로 감쌉니다.

```gherkin
Scenario: JSON 본문으로 도서를 등록한다
  When 다음 JSON으로 도서 등록을 요청한다:
    """json
    {
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "978-0132350884",
      "price": 35.99,
      "stock": 10
    }
    """
  Then 응답 상태 코드는 201이다
```

```java
@When("다음 JSON으로 도서 등록을 요청한다:")
public void 다음_JSON으로_도서_등록을_요청한다(String json) {
    lastResponse = execute(() -> restClient.post()
            .uri("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .body(json)
            .retrieve()
            .toEntity(String.class));
}
```

- 여는 `"""` 뒤의 `json`은 **콘텐츠 타입** 힌트입니다. 리포트 하이라이팅 등에 쓰이며 생략 가능합니다.
- 여는 `"""`의 들여쓰기 위치를 기준으로 공통 들여쓰기가 제거됩니다.
- 메서드 파라미터는 `String` 또는 `io.cucumber.docstring.DocString`으로 받습니다.
- 위 예제의 Step은 이 프로젝트에 아직 없습니다. 직접 추가해보는 것도 좋은 연습입니다.

> **팁**: Doc String에 JSON 전체를 넣으면 기술적인 세부 사항이 시나리오에 드러납니다.
> "API 계약 자체"가 관심사일 때만 쓰고, 대부분은 Data Table로 핵심 값만 보여주는 편이 읽기 좋습니다.

---

## 5. Tag — 분류하고 골라서 실행하기

`@`로 시작하는 Tag를 키워드 **위 줄**에 붙입니다. 공백으로 구분해 여러 개를 붙일 수 있습니다.

```gherkin
@book
Feature: 도서 관리

  @smoke
  Scenario: 새 도서를 성공적으로 등록한다
    ...

  @negative @validation
  Scenario: 제목 없이 도서를 등록하면 실패한다
    ...
```

### 붙일 수 있는 위치와 상속

| 위치 | 효과 |
|------|------|
| `Feature` 위 | 그 파일의 **모든** Scenario에 상속 |
| `Rule` 위 | 그 Rule 안의 모든 Scenario에 상속 |
| `Scenario` / `Scenario Outline` 위 | 해당 Scenario에만 적용 |
| `Examples` 위 | 그 Examples 블록에서 생성된 Scenario에만 적용 |

위 예에서 "제목 없이 도서를 등록하면 실패한다"는 `@book @negative @validation` 세 개의 Tag를 갖습니다.

### Tag Expression

실행할 Scenario를 Tag의 논리식으로 고를 수 있습니다.

| 표현식 | 의미 |
|--------|------|
| `@smoke` | `@smoke`가 붙은 것만 |
| `not @slow` | `@slow`가 없는 것만 |
| `@smoke and @book` | 둘 다 붙은 것만 |
| `@smoke or @critical` | 둘 중 하나라도 붙은 것 |
| `(@smoke or @critical) and not @wip` | 괄호로 우선순위 지정 |

실제로 Tag Expression을 넘겨 실행하는 방법은 [05장](project-architecture.md#6-특정-시나리오만-실행하기)에서 다룹니다.

### 자주 쓰는 Tag 관례

| Tag | 용도 |
|-----|------|
| `@smoke` | 배포 직후 빠르게 확인할 핵심 시나리오 |
| `@wip` | 작업 중 (Work In Progress). CI에서는 `not @wip`으로 제외 |
| `@slow` | 오래 걸리는 시나리오 |
| `@issue-123` | 관련 이슈 번호 연결 |
| `@ignore` / `@disabled` | 임시 제외. Cucumber에 특별한 의미는 없으므로 필터에서 직접 제외해야 함 |

Tag는 **Hook을 특정 Scenario에만 적용**하는 데도 쓰입니다. (`@Before("@database")`, [05장](project-architecture.md) 참고)

---

## 6. Rule — 비즈니스 규칙별로 묶기

Gherkin 6부터 추가된 키워드입니다. 하나의 Feature에 여러 비즈니스 규칙이 있을 때, 규칙별로 예시(Scenario)를 묶어줍니다.
01장에서 본 Example Mapping의 **파란 카드(Rule) - 초록 카드(Example)** 구조를 그대로 옮긴 것입니다.

```gherkin
Feature: 도서 등록

  Background:
    Given 도서 데이터베이스가 초기화되어 있다

  Rule: ISBN은 중복될 수 없다

    Scenario: 새로운 ISBN이면 등록에 성공한다
      When 다음 정보로 도서를 등록한다:
        | title  | Clean Code       |
        | author | Robert C. Martin |
        | isbn   | 978-0132350884   |
        | price  | 35.99            |
        | stock  | 10               |
      Then 응답 상태 코드는 201이다

    Scenario: 이미 등록된 ISBN이면 등록에 실패한다
      Given ISBN "978-0132350884"인 도서가 이미 등록되어 있다
      When ISBN "978-0132350884"로 다른 도서를 등록하려 한다
      Then 응답 상태 코드는 409이다

  Rule: 제목은 반드시 있어야 한다

    Scenario: 제목이 비어 있으면 등록에 실패한다
      When 제목이 비어있는 도서를 등록하려 한다
      Then 응답 상태 코드는 400이다
```

- Rule 안에도 `Background`를 둘 수 있습니다. 이 경우 **Feature의 Background → Rule의 Background → Scenario** 순서로 실행됩니다.
- Rule은 실행에 영향을 주지 않는 **구조화 도구**입니다. 리포트에서 규칙별로 그룹화되어 보입니다.
- 위 예제의 Step들은 모두 이 프로젝트에 이미 구현되어 있어 그대로 실행됩니다.

---

## 7. 전체 문법이 들어간 예제

지금까지 배운 요소를 한 파일에 모두 넣으면 다음과 같은 모양이 됩니다.

```gherkin
# language: en                               <- (선택) 언어 지정, 생략 시 영어
@book                                        <- Feature Tag (모든 Scenario에 상속)
Feature: 도서 관리                            <- Feature
  도서관 관리자로서 도서 목록을 관리하고 싶다     <- Description

  Background:                                <- Feature 공통 전제 조건
    Given 도서 데이터베이스가 초기화되어 있다

  Rule: 등록 시 입력값을 검증한다               <- Rule

    @smoke                                   <- Scenario Tag
    Scenario: 올바른 정보로 등록하면 성공한다     <- Scenario
      When 다음 정보로 도서를 등록한다:          <- Step + Data Table
        | title  | Clean Code       |
        | author | Robert C. Martin |
        | isbn   | 978-0132350884   |
        | price  | 35.99            |
        | stock  | 10               |
      Then 응답 상태 코드는 201이다
      And 등록된 도서 제목은 "Clean Code"이다

    @negative
    Scenario Outline: <field> 값이 잘못되면 실패한다   <- Outline (제목에도 치환)
      When 다음 정보로 도서를 등록한다:
        | title  | 테스트 도서 |
        | author | 테스트 저자 |
        | isbn   | <isbn>     |
        | price  | <price>    |
        | stock  | <stock>    |
      Then 응답 상태 코드는 400이다

      Examples:
        | field | isbn           | price | stock |
        | price | 978-0000000001 | 0     | 5     |
        | stock | 978-0000000002 | 19.99 | -1    |
```

> 위 블록의 `<-` 설명은 학습용 표시입니다. 실제 파일에 그대로 붙여넣으면 문장의 일부가 되어 동작하지 않습니다.

---

## 확인 문제

<details>
<summary>Q1. Background에 Step이 2개, Scenario가 5개 있다면 Background Step은 총 몇 번 실행되나요?</summary>

**10번**입니다. Background는 Scenario마다 반복 실행됩니다. (2 Step x 5 Scenario)
</details>

<details>
<summary>Q2. Scenario Outline의 Examples에 헤더 1행 + 데이터 3행이 있다면 리포트에는 Scenario가 몇 개 표시되나요?</summary>

**3개**입니다. 헤더 행은 변수 이름이고, 데이터 행마다 Scenario가 하나씩 만들어집니다.
</details>

<details>
<summary>Q3. Feature에 <code>@book</code>, Scenario에 <code>@smoke</code>가 붙어 있을 때 Tag Expression <code>@book and not @smoke</code>로 실행하면 이 Scenario는 실행되나요?</summary>

**실행되지 않습니다.** 이 Scenario는 상속받은 `@book`과 자신의 `@smoke`를 모두 갖기 때문에 `not @smoke` 조건에 걸립니다.
</details>

<details>
<summary>Q4. Data Table의 빈 셀은 Java에서 어떤 값으로 들어오나요?</summary>

`null`입니다. 빈 문자열이 아니라는 점에 주의하세요. 예를 들어 `new BigDecimal(data.get("price"))`에서 price 셀이 비어 있으면 `NullPointerException`이 발생합니다.
</details>

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| [← 02. Gherkin 기초 문법](gherkin-basics.md) | [학습 로드맵](../README.md#학습-로드맵) | [04. Step Definition 작성법 →](step-definitions.md) |
