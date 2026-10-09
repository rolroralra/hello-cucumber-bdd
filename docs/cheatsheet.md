# 09. 치트시트

> 01~08장의 내용을 한 장으로 요약했습니다. 자세한 설명은 각 항목의 링크를 참고하세요.

---

## Gherkin 키워드 ([02장](gherkin-basics.md), [03장](gherkin-advanced.md))

| 키워드 | 동의어 / 한국어 | 위치 | 설명 |
|--------|----------------|------|------|
| `Feature:` | `기능:` | 파일 최상단, 1개 | 기능 이름 + 설명 |
| `Rule:` | | Feature 안 | 비즈니스 규칙별 그룹 |
| `Background:` | `배경:` | Feature/Rule 안, 최대 1개 | 각 Scenario 앞에 실행되는 공통 Step |
| `Scenario:` | `Example:`, `시나리오:` | Feature/Rule 안 | 테스트 하나 |
| `Scenario Outline:` | `Scenario Template:`, `시나리오 개요:` | Feature/Rule 안 | 데이터만 바꿔 반복하는 템플릿 |
| `Examples:` | `Scenarios:`, `예:` | Outline 안 | Outline에 넣을 데이터 표 |
| `Given` | `조건`, `먼저` | Step | 사전 상태 |
| `When` | `만일`, `만약` | Step | 행동 |
| `Then` | `그러면` | Step | 기대 결과 |
| `And` / `But` | `그리고` / `하지만`, `단` | Step | 앞 Step 연장 |
| `*` | | Step | 글머리표 |
| `\|` | | Step 아래 | Data Table |
| `"""` | ` ``` ` | Step 아래 | Doc String |
| `@tag` | | 키워드 위 줄 | Tag |
| `#` | | 줄 시작 | 주석 |
| `<name>` | | Outline 안 | Examples 플레이스홀더 |
| `# language: ko` | | 파일 첫 줄 | 키워드 언어 지정 |

## 기본 템플릿

```gherkin
@tag
Feature: 기능 이름
  <역할>로서
  <원하는 것>을 하고 싶다
  그래서 <얻는 가치>

  Background:
    Given 공통 전제 조건

  Rule: 비즈니스 규칙

    Scenario: 구체적인 예시
      Given 사전 상태
      When 행동
      Then 기대 결과
      And 추가 결과

    Scenario Outline: <변수>가 포함된 제목
      When <input>으로 무언가를 한다
      Then 결과는 <output>이다

      Examples:
        | input | output |
        | a     | b      |
```

---

## Cucumber Expression ([04장](step-definitions.md))

| 문법 | 의미 | 예 |
|------|------|----|
| `{int}` `{long}` `{byte}` `{short}` | 정수 | `{int}권` ← `3권` |
| `{double}` `{float}` `{bigdecimal}` | 소수 | `{double}로` ← `29.99로` |
| `{biginteger}` | 큰 정수 | |
| `{string}` | 따옴표 문자열 (따옴표 제외하고 전달) | `{string} 도서` ← `"Clean Code" 도서` |
| `{word}` | 공백 없는 단어 | |
| `{}` | 아무 텍스트 | |
| `a/b` | 대체 텍스트 | `{int}으로/로` |
| `(text)` | 선택 텍스트 | `{int}권(이)` |
| `\\(` `\\{` `\\/` | 특수 문자 이스케이프 (Java 문자열) | |
| `^...$` | 정규식 모드로 전환 | `"^ID (\\d+)로 조회한다$"` |

## Step Definition

```java
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class BookStepDefinitions {

    @Given("{string} 도서가 등록되어 있다")
    public void 도서가_등록되어_있다(String title) { ... }

    @When("다음 정보로 도서를 등록한다:")
    public void 도서를_등록한다(DataTable table) { ... }           // Data Table은 마지막 파라미터

    @When("다음 JSON으로 도서 등록을 요청한다:")
    public void json으로_등록한다(String docString) { ... }       // Doc String은 마지막 파라미터

    @Then("응답 상태 코드는 {int}이다")
    public void 상태_코드_검증(int expected) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expected);
    }
}
```

## DataTable API

| 표 모양 | 코드 | 결과 |
|---------|------|------|
| `\| a \|` `\| b \|` | `table.asList()` | `List<String>` |
| `\| key \| value \|` (세로형) | `table.asMap(String.class, String.class)` | `Map<String,String>` |
| 헤더 + 행 (가로형) | `table.asMaps()` | `List<Map<String,String>>` |
| 헤더 없는 2차원 | `table.asLists()` | `List<List<String>>` |
| 헤더 + 행 → 객체 | `List<BookRequest>` 파라미터 + `@DataTableType` | `List<BookRequest>` |
| 세로형 → 객체 | `@Transpose List<BookRequest>` | `List<BookRequest>` |

빈 셀은 `null`입니다.

## 사용자 정의 타입

```java
@ParameterType(value = "성공|실패", name = "outcome")
public boolean outcome(String value) { return "성공".equals(value); }

@DataTableType
public BookRequest bookRequest(Map<String, String> row) { return new BookRequest(...); }
```

---

## Hook ([05장](project-architecture.md))

```java
import io.cucumber.java.*;

@BeforeAll  public static void beforeAll() { }          // 전체 시작 1회 (static)
@AfterAll   public static void afterAll() { }           // 전체 종료 1회 (static)
@Before     public void before() { }                    // Scenario 시작 전
@After      public void after(Scenario scenario) { }    // Scenario 종료 후 (실패해도 실행)
@BeforeStep public void beforeStep() { }
@AfterStep  public void afterStep() { }

@Before(order = 1)          // 작은 수부터 실행 (@After는 큰 수부터)
@Before("@database")        // Tag Expression에 맞는 Scenario에만
```

실행 순서: `@BeforeAll` → [`@Before` → Background → Steps → `@After`] x Scenario 수 → `@AfterAll`

`Scenario` 객체: `getName()`, `getSourceTagNames()`, `isFailed()`, `getStatus()`, `log(text)`, `attach(data, mediaType, name)`

---

## Tag Expression ([03장](gherkin-advanced.md#5-tag--분류하고-골라서-실행하기))

```
@smoke
not @wip
@smoke and @book
@smoke or @critical
(@smoke or @critical) and not @slow
```

Tag 상속: Feature → Rule → Scenario / Outline → Examples

---

## 설정 프로퍼티

| 키 | 예시 |
|----|------|
| `cucumber.glue` | `com.example.cucumber` |
| `cucumber.plugin` | `pretty, html:build/reports/cucumber/cucumber.html` |
| `cucumber.filter.tags` | `@smoke and not @wip` |
| `cucumber.filter.name` | `^도서를` |
| `cucumber.execution.dry-run` | `true` (매칭만 검사) |
| `cucumber.snippet-type` | `underscore` / `camelcase` |
| `cucumber.publish.quiet` | `true` |

우선순위: Suite `@ConfigurationParameter` > 테스트 JVM 시스템 프로퍼티 > `junit-platform.properties`

---

## 명령어

```bash
./gradlew test                                            # 전체 실행
./gradlew cleanTest test                                  # 캐시 무시하고 재실행
./gradlew test -Dcucumber.filter.tags="@smoke"            # Tag 필터 (build.gradle.kts 전달 설정 필요)
open build/reports/cucumber/cucumber.html                 # HTML 리포트 (macOS)
```

---

## Spring 연동 핵심

```java
@CucumberContextConfiguration                                   // Glue 안에 딱 하나
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration { }
```

| 대상 | 생명주기 |
|------|----------|
| Spring 컨텍스트, DB 컨테이너 | 전체 실행 동안 1개 (공유) |
| Step Definition 인스턴스 | Scenario마다 새로 생성 |
| `@ScenarioScope` 빈 | Scenario마다 새로 생성 |
| DB 데이터 | **자동 초기화 안 됨** → Background/Hook에서 정리 |

---

## Step 결과 상태

| 상태 | 의미 |
|------|------|
| passed | 통과 |
| failed | 예외 발생 (검증 실패 포함) |
| skipped | 앞 Step 실패로 건너뜀 |
| undefined | 매칭되는 Step Definition 없음 |
| pending | `PendingException` (구현 중) |
| ambiguous | 매칭되는 Step Definition 2개 이상 |

---

## 좋은 시나리오 체크 ([06장](writing-good-scenarios.md))

- 제목만 읽어도 의도가 보인다
- Given → When → Then, When은 하나
- 기술 용어(URL, JSON, SQL)가 없다 — 무엇(What)만 쓰고 어떻게(How)는 숨긴다
- 결과에 영향을 주는 데이터만 드러낸다
- 다른 Scenario에 의존하지 않는다
- 10줄을 넘지 않는다

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| [← 08. 트러블슈팅과 FAQ](troubleshooting.md) | [학습 로드맵](../README.md#학습-로드맵) | [부록. API 레퍼런스 →](api-reference.md) |
