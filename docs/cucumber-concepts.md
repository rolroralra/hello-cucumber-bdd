# Cucumber 핵심 개념

Cucumber를 처음 접하는 분들을 위한 개념 정리입니다.

---

## 1. BDD란?

**Behavior-Driven Development(행동 주도 개발)**은 소프트웨어가 "어떻게 동작해야 하는가"를 중심으로 개발하는 방법론입니다.

### 핵심 원칙

```
비즈니스 관계자, 개발자, QA가 함께 →  시나리오 작성  →  자동화 테스트  →  구현
```

- 기술적인 언어가 아닌 **자연어**로 요구사항 표현
- 시나리오가 곧 **살아있는 문서(Living Documentation)**
- 요구사항과 테스트 코드 간 **불일치 방지**

---

## 2. Gherkin 문법

Cucumber는 **Gherkin**이라는 DSL(Domain Specific Language)로 테스트 시나리오를 작성합니다.

### 기본 구조

```gherkin
Feature: 기능 이름
  기능에 대한 설명 (여러 줄 가능)

  Background:              # 모든 시나리오에서 공통으로 실행되는 전제 조건
    Given 공통 전제 조건

  Scenario: 시나리오 이름   # 하나의 테스트 케이스
    Given 전제 조건         # 초기 상태 설정
    When 행동               # 테스트할 행동 실행
    Then 결과 검증          # 기대 결과 확인
    And 추가 결과 검증      # 추가 검증 (Given/When/Then 연장)
```

### 키워드 설명

| 키워드 | 역할 | 예시 |
|--------|------|------|
| `Feature` | 기능 그룹 정의 | `Feature: 도서 관리` |
| `Scenario` | 단일 테스트 케이스 | `Scenario: 도서를 등록한다` |
| `Background` | 공통 전제 조건 | `Given 데이터베이스가 초기화되어 있다` |
| `Given` | 초기 상태 (컨텍스트) | `Given 도서가 5권 있다` |
| `When` | 행동 실행 | `When 도서를 삭제한다` |
| `Then` | 결과 검증 | `Then 도서는 4권이다` |
| `And` / `But` | 이전 키워드 연장 | `And 재고가 감소했다` |

### 한국어 지원

Feature 파일 상단에 `# language: ko`를 추가하면 한국어 키워드를 사용할 수 있습니다:

```gherkin
# language: ko
Feature: 도서 관리
  Scenario: 도서를 등록한다
    조건 도서 데이터베이스가 비어 있다
    만약 새 도서를 등록하면
    그러면 도서 목록에 1권이 있다
```

> 이 프로젝트는 영어 키워드를 한국어 문장과 혼용하는 방식을 채택했습니다.

---

## 3. Step Definitions (단계 정의)

Gherkin의 각 단계(Given/When/Then)를 실제 코드로 연결하는 메서드입니다.

### 어노테이션

```java
import io.cucumber.java.ko.*;  // 한국어 어노테이션
// 또는
import io.cucumber.java.*;     // 영어 어노테이션

@Given("도서 데이터베이스가 초기화되어 있다")
public void 도서_데이터베이스가_초기화되어_있다() {
    bookRepository.deleteAll();
}
```

### 매개변수 추출

Cucumber는 Gherkin 문장에서 값을 자동으로 추출합니다:

```java
// {string}: 따옴표로 감싼 문자열
@Then("등록된 도서 제목은 {string}이다")
public void 등록된_도서_제목은(String expectedTitle) { ... }

// {int}: 정수
@Then("도서 목록에 {int}권이 포함되어 있다")
public void 도서_목록에_N권이(int count) { ... }

// {double}: 실수
@When("해당 도서 가격을 {double}로 수정한다")
public void 가격을_수정한다(double price) { ... }
```

### DataTable

테이블 형태의 데이터를 전달할 때 사용합니다:

```gherkin
When 다음 정보로 도서를 등록한다:
  | title  | Clean Code       |
  | author | Robert C. Martin |
```

```java
@When("다음 정보로 도서를 등록한다:")
public void 도서를_등록한다(DataTable dataTable) {
    Map<String, String> data = dataTable.asMap(String.class, String.class);
    String title = data.get("title");
}
```

---

## 4. Scenario Outline (시나리오 아웃라인)

여러 데이터로 같은 시나리오를 반복 실행할 때 사용합니다:

```gherkin
Scenario Outline: 다양한 가격의 도서를 등록한다
  When 가격이 <price>인 도서를 등록한다
  Then 응답 상태 코드는 <status>이다

  Examples:
    | price  | status |
    | 19.99  | 201    |
    | 0.00   | 400    |
    | -5.00  | 400    |
```

---

## 5. Hooks (훅)

시나리오 전후에 실행되는 코드입니다:

```java
@Before                          // 각 시나리오 시작 전
public void setUp() { ... }

@After                           // 각 시나리오 종료 후
public void tearDown() { ... }

@Before("@database")             // 특정 태그가 붙은 시나리오에만 적용
public void setUpDatabase() { ... }
```

### 태그 사용 예시

```gherkin
@smoke @database
Scenario: 도서를 등록한다
  ...
```

---

## 6. 프로젝트에서의 실행 흐름

```
./gradlew test
    │
    ├── CucumberIntegrationTest (JUnit Platform Suite)
    │       │
    │       ├── features/book_management.feature 탐색
    │       │
    │       └── com.example.cucumber 패키지에서 Step Definitions 탐색
    │
    ├── CucumberSpringConfiguration
    │       ├── @CucumberContextConfiguration (Spring 컨텍스트 제공)
    │       ├── @SpringBootTest (실제 서버 시작, 랜덤 포트)
    │       └── Testcontainers PostgreSQL 시작
    │
    └── BookStepDefinitions
            ├── @Before: RestAssured 기본 설정
            ├── Step 메서드들: HTTP 요청 실행 및 응답 검증
            └── @After: 정리 작업 (필요 시)
```

---

## 7. 주요 파일 위치

| 역할 | 파일 경로 |
|------|-----------|
| Feature 파일 | `src/test/resources/features/*.feature` |
| Step Definitions | `src/test/java/.../steps/*StepDefinitions.java` |
| Spring 설정 | `src/test/java/.../CucumberSpringConfiguration.java` |
| 테스트 실행 | `src/test/java/.../CucumberIntegrationTest.java` |
| Cucumber 설정 | `src/test/resources/junit-platform.properties` |
