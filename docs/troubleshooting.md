# 08. 트러블슈팅과 FAQ

> **이 글의 사용법**
> 오류 메시지의 핵심 단어(예: `undefined`, `Ambiguous`, `context configuration`)로 이 페이지를 검색하세요.
> 각 항목은 **증상 → 원인 → 해결** 순서로 정리되어 있습니다.

---

## 빠른 진단표

| 증상 / 메시지 키워드 | 바로가기 |
|----------------------|----------|
| `The step ... is undefined` | [1. Undefined Step](#1-undefined-step) |
| `matches more than one step definition` | [2. Ambiguous Step](#2-ambiguous-step) |
| `Duplicate step definitions` | [3. Duplicate Step Definition](#3-duplicate-step-definition) |
| `Please annotate a glue class with some context configuration` | [4. Spring 컨텍스트 설정 누락](#4-spring-컨텍스트-설정-누락--중복) |
| `Could not find a valid Docker environment` | [5. Docker 관련 오류](#5-docker-관련-오류) |
| `Undefined parameter type` | [6. 파라미터 타입 오류](#6-파라미터-타입-오류) |
| `NullPointerException` (Step 안에서) | [7. NullPointerException](#7-step-안에서-nullpointerexception) |
| 혼자 돌리면 통과, 같이 돌리면 실패 | [8. 테스트 간 데이터 오염](#8-혼자-돌리면-통과-같이-돌리면-실패) |
| Scenario가 하나도 실행되지 않음 | [9. 실행되는 Scenario가 없음](#9-실행되는-scenario가-없음) |
| Tag 필터가 적용되지 않음 | [10. Tag 필터가 동작하지 않음](#10-tag-필터가-동작하지-않음) |
| IntelliJ에서 Step이 노란색 | [11. IntelliJ 관련](#11-intellij-관련) |
| 400 응답인데 오류 필드 검증 Step이 실패 | [12. 이 프로젝트에서 만날 수 있는 함정](#12-이-프로젝트에서-만날-수-있는-함정) |

---

## 1. Undefined Step

**증상**

```
io.cucumber.junit.platform.engine.UndefinedStepException: The step '응답 상태코드는 201이다' is undefined.
You can implement this step using the snippet(s) below:
...
```

**원인**: Step 문장과 일치하는 Step Definition이 없습니다. 대부분 다음 중 하나입니다.

| 원인 | 예 |
|------|----|
| 띄어쓰기/조사 차이 | Feature: `응답 상태코드는` / Java: `응답 상태 코드는` |
| 콜론 차이 | Feature: `도서를 등록한다:` / Java: `도서를 등록한다` |
| `{string}`인데 따옴표 없이 작성 | Feature: `Clean Code 도서가` / Java: `{string} 도서가` |
| Step Definition 클래스가 Glue 패키지 밖에 있음 | `com.example.other.steps` (Glue는 `com.example.cucumber`) |
| Step Definition 메서드가 `public`이 아님 | `void 메서드()` → `public void 메서드()` |
| 문장 끝에 주석을 붙임 | `Then 응답 상태 코드는 201이다 # 확인` |
| Scenario Outline 플레이스홀더 오타 | `<prcie>` → 치환되지 않고 글자 그대로 남음 |

**해결**

1. 콘솔에 출력된 스니펫의 패턴과 기존 Step Definition 패턴을 **한 글자씩** 비교합니다.
2. 새 Step이 맞다면 스니펫을 복사해 구현합니다. ([04장 11절](step-definitions.md#11-미구현-step과-스니펫))
3. `cucumber.execution.dry-run=true`로 실행하면 서버를 띄우지 않고 모든 Undefined Step을 빠르게 찾을 수 있습니다.

---

## 2. Ambiguous Step

**증상**

```
io.cucumber.core.runner.AmbiguousStepDefinitionsException: "ID 5로 도서를 조회한다" matches more than one step definition:
  "ID {long}로 도서를 조회한다" in com.example.cucumber.steps.BookStepDefinitions.ID_로_도서를_조회한다(long)
  "ID {int}로 도서를 조회한다" in com.example.cucumber.steps.OtherSteps.조회(int)
```

**원인**: 하나의 Step 문장이 두 개 이상의 패턴과 매칭됩니다. `{}`(익명 파라미터)나 정규식처럼 넓게 매칭되는 패턴을 쓸 때 자주 발생합니다.

**해결**

- 메시지에 나온 두 Step Definition 중 하나를 삭제하거나 합칩니다.
- 패턴을 더 구체적으로 바꿉니다. (예: `{}` → `{string}`, `.*` → `\\d+`)
- Step Definition은 **전역**입니다. 다른 클래스, 다른 Feature용으로 만든 Step이라도 충돌합니다.

---

## 3. Duplicate Step Definition

**증상**: `DuplicateStepDefinitionException: Duplicate step definitions in ...`

**원인**: **완전히 같은 패턴**이 두 메서드에 붙어 있습니다. 복사-붙여넣기 후 수정하지 않은 경우가 대부분입니다.

**해결**: 하나를 지우고, 필요하면 두 Scenario가 같은 Step을 재사용하게 합니다.

---

## 4. Spring 컨텍스트 설정 누락 / 중복

**증상 A — 누락**

```
Please annotate a glue class with some context configuration.
For example:
   @CucumberContextConfiguration
   @SpringBootTest(classes = TestConfig.class)
   public class CucumberSpringConfiguration { }
```

**원인**: Glue 패키지 안에 `@CucumberContextConfiguration` 클래스가 없습니다.
설정 클래스를 Glue 밖의 패키지로 옮겼거나, Glue 설정(`cucumber.glue`)을 너무 좁게(예: `com.example.cucumber.steps`) 바꾼 경우입니다.

**해결**: `CucumberSpringConfiguration`이 Glue 패키지(`com.example.cucumber`) 아래에 있는지 확인합니다.

**증상 B — 중복**

```
Glue class ... and ... are both (meta-)annotated with @CucumberContextConfiguration.
Please ensure only one class configures the spring context
```

**해결**: `@CucumberContextConfiguration`은 Glue 전체에서 **딱 하나**만 둡니다.

---

## 5. Docker 관련 오류

**증상**

```
java.lang.IllegalStateException: Could not find a valid Docker environment.
```

**원인과 해결**

| 원인 | 해결 |
|------|------|
| Docker Desktop이 꺼져 있음 | Docker Desktop 실행 후 `docker ps`가 동작하는지 확인 |
| 이미지 다운로드 실패 (사내망 등) | `docker pull postgres:16-alpine`을 먼저 직접 실행 |
| Docker 소켓 경로 문제 (Colima, Rancher Desktop 등) | 사용하는 런타임의 Testcontainers 설정 가이드에 따라 `DOCKER_HOST` 지정 |

---

## 6. 파라미터 타입 오류

**증상 A**: `UndefinedParameterTypeException: Undefined parameter type 'isbn'`

**원인**: 패턴에 `{isbn}`을 썼지만 `@ParameterType(name = "isbn")`이 정의되지 않았거나 Glue 밖에 있습니다.

**증상 B**: Step은 매칭되는 것 같은데 Undefined가 남

```gherkin
When 해당 도서 가격을 29.99로 수정한다
```
```java
@When("해당 도서 가격을 {int}로 수정한다")    // 29.99는 {int}와 매칭되지 않음
```

**해결**: 값의 형태에 맞는 타입을 씁니다. 소수는 `{double}` 또는 `{bigdecimal}`, 따옴표 문자열은 `{string}`.

**증상 C**: `NumberFormatException` — Data Table이나 Outline 값에 숫자가 아닌 값이 들어가서 Step 안의 `Integer.parseInt()` 등이 실패한 경우입니다.

---

## 7. Step 안에서 NullPointerException

| 원인 | 확인 방법 | 해결 |
|------|-----------|------|
| Hook 어노테이션을 잘못 import함 | `import org.junit.Before;` 또는 `org.junit.jupiter.api.BeforeEach` | `import io.cucumber.java.Before;` |
| Then Step이 When 없이 실행됨 | `lastResponse`가 `null` | Scenario에 When이 있는지, When Step이 `lastResponse`에 값을 저장하는지 확인 |
| Data Table의 빈 셀 | 빈 셀은 `null`로 변환됨 | 셀에 값을 채우거나 Step에서 `null` 처리 |
| 다른 Step Definition 클래스의 필드를 기대함 | 클래스 A가 저장한 필드를 클래스 B가 읽으려 함 | 공유 상태를 `@ScenarioScope` 빈으로 분리 ([04장 8.2절](step-definitions.md#82-여러-step-definition-클래스가-상태를-공유해야-할-때)) |

---

## 8. 혼자 돌리면 통과, 같이 돌리면 실패

**원인**: Scenario 사이에 **DB 데이터가 남아** 있습니다.
Step Definition의 필드는 Scenario마다 초기화되지만, Testcontainers DB와 Spring 컨텍스트는 전체 실행 동안 공유됩니다. ([05장 4절](project-architecture.md#4-생명주기-무엇이-한-번-만들어지고-무엇이-매번-만들어지나))

대표적인 증상:
- `도서 목록에 3권이 포함되어 있다` 검증이 `5`로 실패
- 같은 ISBN 때문에 등록이 409로 실패

**해결**

- 새로 만든 Feature 파일에도 `Background: Given 도서 데이터베이스가 초기화되어 있다`가 있는지 확인합니다.
- 또는 `@Before` Hook에서 데이터를 정리합니다.
- `@Transactional` 롤백은 `RANDOM_PORT` 환경에서 서버 쪽 데이터를 되돌리지 못하므로 해결책이 아닙니다.
- `static` 필드로 상태를 저장하고 있지 않은지 확인합니다.

---

## 9. 실행되는 Scenario가 없음

**증상**: `./gradlew test`가 성공하지만 Cucumber 출력이 없거나 `0 Scenarios`

| 원인 | 해결 |
|------|------|
| `.feature` 파일이 `src/test/resources/features` 밖에 있음 | `@SelectClasspathResource("features")` 경로 아래로 이동 |
| 확장자가 `.feature`가 아님 (`.features`, `.txt`) | 확장자 수정 |
| Tag 필터가 모든 Scenario를 걸러냄 | `cucumber.filter.tags` 설정 확인 |
| Gradle이 이전 결과를 재사용 (UP-TO-DATE) | `./gradlew cleanTest test` 또는 `--rerun-tasks` |

---

## 10. Tag 필터가 동작하지 않음

**증상**: `./gradlew test -Dcucumber.filter.tags=@smoke`를 실행했는데 모든 Scenario가 실행됨

**원인**: Gradle의 `-D`는 Gradle 프로세스의 시스템 프로퍼티입니다. 테스트가 실행되는 JVM에는 전달되지 않습니다.

**해결**: `build.gradle.kts`에 전달 코드를 추가합니다. ([05장 6.2절](project-architecture.md#6-특정-시나리오만-실행하기))

```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
    System.getProperty("cucumber.filter.tags")?.let { systemProperty("cucumber.filter.tags", it) }
}
```

---

## 11. IntelliJ 관련

| 증상 | 해결 |
|------|------|
| `.feature` 파일이 일반 텍스트로 보임 | **Gherkin** 플러그인 설치 |
| Step 문장에 노란 밑줄 (정의 없음으로 표시) | **Cucumber for Java** 플러그인 설치. 설치 후에도 그러면 Step Definition 패턴과 문장을 비교 |
| Feature 파일에서 직접 실행 시 모든 Step이 Undefined | Run Configuration의 **Glue**에 `com.example.cucumber` 입력 |
| Feature 파일에서 직접 실행 시 Spring 오류 | Gradle로 실행(`Settings > Build Tools > Gradle > Run tests using: Gradle`)하거나 Glue 설정 확인 |
| Step 문장에서 Java 메서드로 이동하고 싶음 | `Cmd+클릭`(macOS) / `Ctrl+클릭`(Windows) |
| 새 Step의 메서드를 자동 생성하고 싶음 | 노란 밑줄 위에서 `Option+Enter` / `Alt+Enter` → **Create step definition** |

---

## 12. 이 프로젝트에서 만날 수 있는 함정

### 12.1 한 필드에 검증 오류가 두 개 이상이면 응답 형식이 달라질 수 있음

`GlobalExceptionHandler.handleValidationErrors()`는 필드 오류를 `Collectors.toMap(FieldError::getField, ...)`으로 모읍니다.
`toMap`은 **같은 키가 두 번 나오면 `IllegalStateException`** 을 던집니다.

예를 들어 ISBN을 빈 문자열로 보내면 `@NotBlank`와 `@Pattern` 두 검증이 모두 실패해서 `isbn` 키가 두 번 생깁니다.
이 경우 예외 핸들러 자체가 실패해서 Spring 기본 오류 처리로 넘어갑니다. 그 결과 **상태 코드는 400이지만**
응답 본문이 이 프로젝트의 `ApiResponse` 형식(`{"success":..,"data":{..},"message":..}`)이 아니게 되어,
`오류 메시지에 "isbn" 필드 검증 오류가 포함된다` Step이 다음과 같이 실패합니다.

```
org.opentest4j.AssertionFailedError: [응답 data에 'isbn' 필드 오류가 있어야 합니다]
Expecting value to be true but was false
```

- 시나리오를 작성할 때 한 번에 **하나의 검증 규칙만 위반하는 값**을 고르면 피할 수 있습니다. (예: ISBN은 빈 값 대신 `abc`)
- 근본적으로는 `toMap`에 병합 함수를 넘겨 해결할 수 있습니다: `Collectors.toMap(FieldError::getField, msg, (a, b) -> a)`
- **이것이 바로 BDD 시나리오가 찾아주는 종류의 버그입니다.** 직접 재현하는 Scenario를 먼저 쓰고(RED) 고쳐보는 것도 좋은 연습입니다.

### 12.2 `"..." 도서가 등록되어 있다`의 ISBN은 제목으로부터 계산됨

`generateIsbn(title)`은 제목의 `hashCode()`로 ISBN을 만듭니다. 그래서 **같은 Scenario에서 같은 제목으로 두 번 등록하면 409(ISBN 중복)** 가 납니다.
다른 제목을 쓰거나 Data Table로 ISBN을 명시하세요.

---

## FAQ

<details>
<summary>Q. 모든 테스트를 Cucumber로 작성해야 하나요?</summary>

아닙니다. Cucumber는 **비즈니스 관계자와 공유할 가치가 있는 행동**에 사용합니다.
계산 로직의 경계값, 예외 처리 분기, 유틸리티 함수 등은 JUnit 단위 테스트가 더 빠르고 정확합니다.
일반적인 비율은 "소수의 Cucumber 시나리오 + 다수의 단위 테스트"입니다.
</details>

<details>
<summary>Q. Step Definition 클래스는 Feature 파일마다 하나씩 만들어야 하나요?</summary>

아닙니다. Step Definition은 전역이므로 **도메인 개념** 기준으로 나누는 것이 좋습니다.
(예: `BookStepDefinitions`, `MemberStepDefinitions`, `CommonResponseStepDefinitions`)
Feature 기준으로 나누면 같은 문장을 여러 클래스에 중복 정의하게 되어 Duplicate/Ambiguous 오류의 원인이 됩니다.
</details>

<details>
<summary>Q. Java 메서드 이름을 한글로 써도 되나요?</summary>

Java는 유니코드 식별자를 지원하므로 문제없습니다. 이 프로젝트처럼 Step 문장을 메서드 이름으로 쓰면 스택 트레이스 가독성이 좋아집니다.
다만 팀 컨벤션에 따라 영문 이름을 쓰는 것도 전혀 문제없습니다. Cucumber는 메서드 이름을 매칭에 사용하지 않습니다.
</details>

<details>
<summary>Q. Scenario 실행 순서는 보장되나요?</summary>

기본적으로 파일과 Scenario가 작성된 순서대로 실행되지만, **순서에 의존하면 안 됩니다.**
Tag 필터, 병렬 실행, IDE에서 단일 실행 등으로 순서는 언제든 바뀔 수 있습니다.
</details>

<details>
<summary>Q. 병렬로 실행할 수 있나요?</summary>

`cucumber.execution.parallel.enabled=true`로 가능합니다. 하지만 이 프로젝트처럼 **하나의 DB를 공유하고 `deleteAll()`로 초기화**하는 구조에서는
한 Scenario가 다른 Scenario의 데이터를 지워버리므로 병렬 실행하면 안 됩니다.
병렬화하려면 Scenario마다 데이터를 격리(고유 키 사용, 스키마 분리 등)하는 설계가 먼저 필요합니다.
</details>

<details>
<summary>Q. PendingException은 언제 쓰나요?</summary>

Step 문장은 합의했지만 구현은 아직 안 된 상태를 표시할 때 사용합니다. 해당 Scenario는 실패가 아닌 **pending**으로 표시됩니다.
"작성 중인 명세"를 저장소에 먼저 올려두고 싶을 때 `@wip` Tag와 함께 쓰는 경우가 많습니다.
</details>

<details>
<summary>Q. Feature 파일을 영어로 써야 하나요, 한국어로 써야 하나요?</summary>

**독자가 쓰는 언어**로 쓰세요. 기획자와 고객이 한국어로 소통한다면 한국어 문장이 맞습니다.
키워드(`Given` vs `조건`)는 팀이 하나로 정하면 됩니다. ([02장 9절](gherkin-basics.md#9-한국어-키워드))
</details>

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| [← 07. 단계별 실습 가이드](bdd-guide.md) | [학습 로드맵](../README.md#학습-로드맵) | [09. 치트시트 →](cheatsheet.md) |
