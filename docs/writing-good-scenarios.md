# 06. 좋은 시나리오 작성법

> **이 글에서 배울 것**
> - 선언형(Declarative)과 명령형(Imperative) 시나리오의 차이
> - 좋은 시나리오의 6가지 기준 (BRIEF)
> - 자주 보이는 안티 패턴과 고치는 방법
> - 이 프로젝트의 시나리오를 비판적으로 다시 읽어보기
> - 시나리오 리뷰 체크리스트
>
> **선수 지식**: [02. Gherkin 기초 문법](gherkin-basics.md), [04. Step Definition 작성법](step-definitions.md)

---

## 1. 문법보다 중요한 것

Gherkin 문법은 하루면 배웁니다. 하지만 **읽기 좋은 시나리오를 쓰는 것**은 계속 연습해야 하는 기술입니다.
문법적으로 올바르지만 나쁜 시나리오는 다음과 같은 문제를 만듭니다.

- 기획자가 읽지 않는다 → 협업 도구로서의 가치가 사라짐
- 화면이나 API가 조금만 바뀌어도 수십 개의 시나리오가 깨진다 → 유지보수 비용 폭증
- 실패했을 때 무엇이 잘못됐는지 알 수 없다 → 디버깅 시간 증가

판단 기준은 하나입니다.

> **이 시나리오를 비개발자 동료가 읽고, 이 시스템이 "무엇을" 하는지 이해할 수 있는가?**

---

## 2. 선언형 vs 명령형

### 명령형(Imperative): "어떻게"를 나열

```gherkin
Scenario: 도서 등록
  Given 브라우저에서 "/admin/books/new" 페이지를 연다
  When "title" 입력란에 "Clean Code"를 입력한다
  And "author" 입력란에 "Robert C. Martin"을 입력한다
  And "isbn" 입력란에 "978-0132350884"를 입력한다
  And "price" 입력란에 "35.99"를 입력한다
  And "stock" 입력란에 "10"을 입력한다
  And "저장" 버튼을 클릭한다
  Then "등록되었습니다" 메시지가 보인다
```

### 선언형(Declarative): "무엇을"을 서술

```gherkin
Scenario: 관리자는 새 도서를 등록할 수 있다
  When 관리자가 "Clean Code" 도서를 등록한다
  Then "Clean Code"가 도서 목록에 나타난다
```

| 비교 | 명령형 | 선언형 |
|------|--------|--------|
| 읽는 사람 | 테스트 자동화 담당자 | 누구나 |
| UI/API 변경 시 | 시나리오 수정 필요 | Step Definition만 수정 |
| 드러내는 것 | 조작 절차 | 비즈니스 규칙과 결과 |
| Step 재사용성 | 낮음 (문장이 화면에 종속) | 높음 |

**어떻게(How)** 는 Step Definition 안에 숨기고, Gherkin에는 **무엇을(What)** 만 남기는 것이 원칙입니다.

---

## 3. 좋은 시나리오의 기준: BRIEF

Seb Rose와 Gáspár Nagy가 『The BDD Books: Formulation』에서 제안한 기준입니다.

| 글자 | 원칙 | 질문 |
|:---:|------|------|
| **B** | Business language (비즈니스 언어) | 개발 용어(테이블, JSON, 엔드포인트) 대신 도메인 용어를 썼는가? |
| **R** | Real data (실제 같은 데이터) | `foo`, `test1` 대신 의미 있는 예시 데이터를 썼는가? |
| **I** | Intention revealing (의도 드러내기) | 시나리오 이름과 Step만 읽고 목적을 알 수 있는가? |
| **E** | Essential (필수 정보만) | 결과에 영향을 주지 않는 세부 정보는 숨겼는가? |
| **F** | Focused (하나에 집중) | 하나의 규칙(Rule)만 설명하는가? |
| **Brief** | 짧게 | 5줄 내외로 끝나는가? |

### E (Essential)의 좋은 예 — 이 프로젝트에서

```gherkin
Scenario: 도서를 삭제한다
  Given "Test-Driven Development" 도서가 등록되어 있다
  When 해당 도서를 삭제한다
  Then 응답 상태 코드는 204이다
  And 해당 도서는 더 이상 조회되지 않는다
```

삭제 기능을 검증하는 데 저자, ISBN, 가격, 재고는 중요하지 않습니다.
그래서 Step Definition(`도서가_등록되어_있다`)이 그 값들을 알아서 채우고(`generateIsbn()` 등), 시나리오에는 제목만 남겼습니다.

### R (Real data)와 E의 균형

반대로 **결과에 영향을 주는 값은 반드시 드러내야** 합니다.

```gherkin
# 나쁜 예: 왜 실패하는지 알 수 없음
Scenario: 도서 등록에 실패한다
  When 잘못된 도서를 등록하려 한다
  Then 응답 상태 코드는 400이다

# 좋은 예: 실패의 원인이 되는 값이 보임
Scenario: 가격이 0이면 도서를 등록할 수 없다
  When 가격이 0인 도서를 등록하려 한다
  Then 응답 상태 코드는 400이다
```

---

## 4. 구조 규칙

### 4.1 Given → When → Then 순서를 지킨다

```gherkin
# 나쁜 예: When-Then이 반복됨 (사실상 두 개의 테스트)
Scenario: 도서 등록 후 수정
  When "Refactoring" 도서를 등록한다
  Then 응답 상태 코드는 201이다
  When 해당 도서 가격을 29.99로 수정한다
  Then 응답 상태 코드는 200이다
```

```gherkin
# 좋은 예: 수정 기능만 검증. 등록은 전제 조건(Given)으로
Scenario: 도서 가격을 수정한다
  Given "Refactoring" 도서가 등록되어 있다
  When 해당 도서 가격을 29.99로 수정한다
  Then 응답 상태 코드는 200이다
  And 수정된 도서 가격은 29.99이다
```

### 4.2 Scenario는 서로 독립적이다

- 앞 Scenario가 만든 데이터에 기대지 않습니다.
- 각 Scenario가 필요한 상태는 자신의 Given(또는 Background)으로 직접 만듭니다.
- 이 프로젝트는 Background에서 DB를 비우고 각 Scenario가 필요한 도서를 직접 등록합니다.

### 4.3 Then은 관찰 가능한 결과만 검증한다

```gherkin
# 나쁜 예: 내부 구현을 검증
Then books 테이블에 1개의 row가 insert 된다

# 좋은 예: 사용자가 관찰할 수 있는 결과를 검증
Then 도서 목록에 1권이 포함되어 있다
```

---

## 5. 안티 패턴 모음

| 안티 패턴 | 증상 | 처방 |
|-----------|------|------|
| **UI 스크립트** | "클릭한다", "입력한다"가 가득함 | 선언형으로 바꾸고 조작은 Step Definition에 숨김 |
| **기술 용어 노출** | URL, JSON 경로, SQL, HTTP 메서드가 Step에 등장 | 도메인 용어로 표현 |
| **거대한 Background** | Background가 5줄 이상 | 꼭 필요한 맥락만 남기고 나머지는 Hook이나 Given Step 하나로 압축 |
| **긴 시나리오** | Step이 10줄 이상 | 여러 규칙이 섞였는지 확인 후 분리 |
| **의미 없는 데이터** | `test`, `aaa`, `123` | 실제 같은 데이터 사용 |
| **Then 없는 시나리오** | 검증 없이 When으로 끝남 | 기대 결과를 반드시 명시 |
| **조건 분기** | "만약 ~라면 ~하고 아니면 ~" | 경우마다 Scenario를 나눔 |
| **Step 문장 난립** | 같은 뜻의 문장이 여러 형태 (`등록되어 있다` / `존재한다` / `저장되어 있다`) | 팀 용어집(Ubiquitous Language)을 정하고 하나로 통일 |
| **테스트 이름 같은 Scenario 제목** | `Scenario: testCreateBook_success` | 행동이 드러나는 문장으로 |
| **Scenario 간 의존** | 순서를 바꾸면 실패 | 각 Scenario가 자기 상태를 만들도록 수정 |

---

## 6. 이 프로젝트의 시나리오를 비판적으로 읽어보기

이 프로젝트의 시나리오는 학습용으로 **API 수준의 인수 테스트**를 보여주기 위해 작성되었습니다.
좋은 점과 개선할 수 있는 점을 함께 살펴보면 실력이 빨리 늘어납니다.

### 잘된 점

- 시나리오 이름이 행동과 기대 결과를 드러냅니다. (`존재하지 않는 ID로 조회하면 404를 반환한다`)
- 결과와 무관한 데이터는 Step Definition이 채웁니다. (`"Effective Java" 도서가 등록되어 있다`)
- 모든 Scenario가 Background 덕분에 독립적입니다.
- Then 문장이 재사용 가능하게 파라미터화되어 있습니다. (`응답 상태 코드는 {int}이다`)

### 개선을 고민해볼 점

**(1) HTTP 상태 코드는 기술 용어다**

```gherkin
Then 응답 상태 코드는 409이다
```

API를 소비하는 개발자가 주요 독자라면 이 표현은 충분히 명확합니다.
하지만 기획자가 독자라면 "409"는 의미가 없습니다. 비즈니스 언어로 바꾸면 다음과 같습니다.

```gherkin
Then 도서 등록이 "이미 등록된 ISBN" 사유로 거절된다
```

어느 쪽이 맞는지는 **이 문서를 누가 읽는가**에 달려 있습니다. 팀이 합의해서 정하면 됩니다.

**(2) "해당 도서"는 숨겨진 상태에 의존한다**

```gherkin
Given "Refactoring" 도서가 등록되어 있다
When 해당 도서 가격을 29.99로 수정한다
```

"해당 도서"는 Step Definition 내부의 `savedBookId` 필드에 의존합니다.
한 Scenario에 도서가 하나뿐일 때는 자연스럽지만, 두 권 이상 등장하면 어떤 도서인지 모호해집니다.
그럴 때는 `When "Refactoring" 도서 가격을 29.99로 수정한다`처럼 대상을 명시하는 방식을 고려하세요.

**(3) 같은 개념, 다른 문장**

```gherkin
And 등록된 도서 제목은 "Clean Code"이다
And 조회된 도서 제목은 "Effective Java"이다
```

두 문장은 결국 "응답에 담긴 도서의 제목"을 검증합니다. 하나의 문장(예: `응답 도서의 제목은 {string}이다`)으로 통일하면 Step Definition도 하나로 줄어듭니다.
반대로 "등록"과 "조회"라는 맥락을 독자에게 보여주는 것이 더 중요하다고 판단할 수도 있습니다. 이 역시 트레이드오프입니다.

---

## 7. 연습 문제: 나쁜 시나리오 고치기

다음 시나리오의 문제점을 찾고 다시 써보세요.

```gherkin
Scenario: 테스트2
  Given POST /api/books 에 {"title":"A","author":"B","isbn":"978-0000000001","price":10,"stock":1} 를 보낸다
  When GET /api/books 를 호출한다
  Then 응답 JSON의 data 배열 길이는 1이다
  When DELETE /api/books/1 을 호출한다
  Then 응답 상태 코드는 204이다
```

<details>
<summary>문제점과 예시 답안</summary>

**문제점**

1. 제목 `테스트2`가 의도를 드러내지 않음 (I 위반)
2. HTTP 메서드, URL, JSON이 그대로 노출됨 (B 위반)
3. `"A"`, `"B"` 같은 의미 없는 데이터 (R 위반)
4. 조회와 삭제 두 가지를 한 번에 검증 (F 위반, When-Then 반복)
5. ID `1`을 하드코딩 → DB 시퀀스 상태에 따라 실패할 수 있음 (Scenario 독립성 위반)

**예시 답안** — 두 개의 Scenario로 분리

```gherkin
Scenario: 등록된 도서를 목록에서 확인할 수 있다
  Given 다음 도서들이 등록되어 있다:
    | title      | author           | isbn           | price | stock |
    | Clean Code | Robert C. Martin | 978-0132350884 | 35.99 | 10    |
  When 전체 도서 목록을 조회한다
  Then 도서 목록에 1권이 포함되어 있다

Scenario: 도서를 삭제하면 더 이상 조회되지 않는다
  Given "Clean Code" 도서가 등록되어 있다
  When 해당 도서를 삭제한다
  Then 해당 도서는 더 이상 조회되지 않는다
```

두 Scenario 모두 이 프로젝트의 기존 Step만으로 실행됩니다.
</details>

---

## 8. 시나리오 리뷰 체크리스트

PR에서 `.feature` 파일을 리뷰할 때 사용하세요.

- [ ] 시나리오 제목만 읽어도 무엇을 검증하는지 알 수 있다
- [ ] Given / When / Then 순서를 지키고, When은 하나다
- [ ] URL, JSON, SQL, CSS 선택자 같은 기술 세부사항이 없다
- [ ] 결과에 영향을 주는 데이터는 드러나 있고, 영향 없는 데이터는 숨겨져 있다
- [ ] 다른 Scenario에 의존하지 않는다
- [ ] Then이 관찰 가능한 결과를 검증한다
- [ ] 같은 의미의 기존 Step 문장을 재사용했다 (새 문장을 만들기 전에 검색했다)
- [ ] Step이 10줄을 넘지 않는다
- [ ] Scenario Outline의 Examples가 의미 있는 대표 사례들이다 (전수 검사가 아님)

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| [← 05. 프로젝트 구조와 실행 원리](project-architecture.md) | [학습 로드맵](../README.md#학습-로드맵) | [07. 단계별 실습 가이드 →](bdd-guide.md) |
