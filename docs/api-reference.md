# 부록. API 레퍼런스

> Cucumber 시나리오가 검증하는 **테스트 대상 REST API**의 명세입니다.
> Step Definition을 새로 작성할 때 요청 형식, 검증 규칙, 응답 코드를 확인하는 용도로 사용하세요.
>
> - 로컬 실행(`./gradlew bootRun`) 시 Base URL은 아래와 같습니다.
> - 테스트 실행 시에는 서버가 **랜덤 포트**로 뜨며, Step Definition이 `@LocalServerPort`로 포트를 받아 요청합니다.

Base URL: `http://localhost:8080`

모든 응답은 공통 포맷을 따릅니다:

```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

---

## 도서 (Books)

### GET /api/books

전체 도서 목록을 조회합니다.

**응답 예시 (200 OK)**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "978-0132350884",
      "price": 35.99,
      "stock": 10,
      "createdAt": "2026-04-11T10:00:00",
      "updatedAt": "2026-04-11T10:00:00"
    }
  ],
  "message": null
}
```

---

### GET /api/books/{id}

특정 도서를 조회합니다.

**경로 변수**
- `id` (Long): 도서 ID

**응답 예시 (200 OK)**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "978-0132350884",
    "price": 35.99,
    "stock": 10,
    "createdAt": "2026-04-11T10:00:00",
    "updatedAt": "2026-04-11T10:00:00"
  },
  "message": null
}
```

**오류 응답 (404 Not Found)**

```json
{
  "success": false,
  "data": null,
  "message": "도서를 찾을 수 없습니다. ID: 999"
}
```

---

### POST /api/books

새 도서를 등록합니다.

**요청 본문**

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "price": 35.99,
  "stock": 10
}
```

**필드 검증**

| 필드 | 타입 | 제약 조건 |
|------|------|-----------|
| `title` | String | 필수, 비어있지 않아야 함 |
| `author` | String | 필수, 비어있지 않아야 함 |
| `isbn` | String | 필수, `^[0-9\-]{10,17}$` 패턴, 고유값 |
| `price` | BigDecimal | 필수, 0보다 커야 함 |
| `stock` | Integer | 필수, 0 이상 |

**응답 예시 (201 Created)**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Clean Code",
    ...
  },
  "message": null
}
```

**오류 응답 (400 Bad Request - 유효성 검증 실패)**

```json
{
  "success": false,
  "data": {
    "title": "제목은 필수입니다",
    "price": "가격은 0보다 커야 합니다"
  },
  "message": "입력값 검증 실패"
}
```

**오류 응답 (409 Conflict - 중복 ISBN)**

```json
{
  "success": false,
  "data": null,
  "message": "이미 등록된 ISBN입니다: 978-0132350884"
}
```

---

### PUT /api/books/{id}

기존 도서 정보를 수정합니다.

**경로 변수**
- `id` (Long): 도서 ID

**요청 본문** (POST와 동일한 형식)

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "978-0132350884",
  "price": 29.99,
  "stock": 15
}
```

**응답 예시 (200 OK)**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "price": 29.99,
    "stock": 15,
    ...
  },
  "message": null
}
```

---

### DELETE /api/books/{id}

도서를 삭제합니다.

**경로 변수**
- `id` (Long): 도서 ID

**응답**: `204 No Content` (응답 본문 없음)

**오류 응답 (404 Not Found)**

```json
{
  "success": false,
  "data": null,
  "message": "도서를 찾을 수 없습니다. ID: 999"
}
```

---

## HTTP 상태 코드 요약

| 코드 | 의미 | 사용 상황 |
|------|------|-----------|
| 200 | OK | 조회, 수정 성공 |
| 201 | Created | 등록 성공 |
| 204 | No Content | 삭제 성공 |
| 400 | Bad Request | 입력값 검증 실패 |
| 404 | Not Found | 도서를 찾을 수 없음 |
| 409 | Conflict | ISBN 중복 |
| 500 | Internal Server Error | 서버 오류 |

---

## 시나리오와 API의 대응

| Feature 시나리오 | 호출 API | 기대 상태 |
|------------------|----------|:---:|
| 새 도서를 성공적으로 등록한다 | `POST /api/books` | 201 |
| 제목 없이 도서를 등록하면 실패한다 | `POST /api/books` | 400 |
| 중복 ISBN으로 도서를 등록하면 실패한다 | `POST /api/books` | 409 |
| 전체 도서 목록을 조회한다 | `GET /api/books` | 200 |
| ID로 특정 도서를 조회한다 | `GET /api/books/{id}` | 200 |
| 존재하지 않는 ID로 조회하면 404를 반환한다 | `GET /api/books/{id}` | 404 |
| 도서 가격을 수정한다 / 도서 재고를 추가한다 | `GET` 후 `PUT /api/books/{id}` | 200 |
| 도서를 삭제한다 | `DELETE /api/books/{id}` | 204 |
| 존재하지 않는 도서를 삭제하면 404를 반환한다 | `DELETE /api/books/{id}` | 404 |

---

| 이전 글 | 목차 | 다음 글 |
|:---|:---:|---:|
| [← 09. 치트시트](cheatsheet.md) | [학습 로드맵](../README.md#학습-로드맵) | [처음으로: README →](../README.md) |
