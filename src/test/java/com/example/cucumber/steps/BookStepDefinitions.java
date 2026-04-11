package com.example.cucumber.steps;

import com.example.cucumber.book.BookRepository;
import com.example.cucumber.book.BookRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 도서 관리 Feature의 Step Definition 클래스.
 *
 * <p>Cucumber는 {@code @CucumberContextConfiguration}이 선언된 Spring 컨텍스트에서
 * 이 클래스를 빈으로 등록하므로, {@code @Autowired}와 {@code @LocalServerPort}를 사용할 수 있습니다.
 *
 * <p>HTTP 클라이언트로 Spring Framework 7의 {@code RestClient}를 사용합니다.
 * (Spring Boot 4에서 TestRestTemplate 제거됨)
 */
public class BookStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    // ObjectMapper를 직접 생성 (Spring Boot 4에서 빈 자동 등록 모듈이 분리됨)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /** 각 시나리오 간 상태를 저장하는 필드 */
    private ResponseEntity<String> lastResponse;
    private Long savedBookId;
    private RestClient restClient;

    /**
     * 각 시나리오 실행 전 RestClient 초기화.
     * port는 Spring 컨텍스트 주입 후 확정되므로 @Before에서 생성합니다.
     */
    @Before
    public void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
        lastResponse = null;
        savedBookId = null;
    }

    // ── 공통 헬퍼 ─────────────────────────────────────────────────

    /**
     * RestClient 요청을 실행하고 4xx/5xx 응답도 예외 없이 ResponseEntity로 반환합니다.
     * RestClient는 기본적으로 4xx/5xx에서 예외를 던지므로 catch하여 ResponseEntity로 변환합니다.
     */
    private ResponseEntity<String> execute(RestClientRequest requestFn) {
        try {
            return requestFn.execute();
        } catch (HttpStatusCodeException ex) {
            // 4xx/5xx 응답도 ResponseEntity로 감싸서 반환
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        }
    }

    @FunctionalInterface
    private interface RestClientRequest {
        ResponseEntity<String> execute();
    }

    private JsonNode parseBody() {
        try {
            return objectMapper.readTree(lastResponse.getBody());
        } catch (Exception e) {
            throw new RuntimeException("응답 JSON 파싱 실패: " + lastResponse.getBody(), e);
        }
    }

    private String generateIsbn(String title) {
        int hash = Math.abs(title.hashCode()) % 1000000000;
        return "978-" + String.format("%09d", hash);
    }

    // ── Background ────────────────────────────────────────────────

    @Given("도서 데이터베이스가 초기화되어 있다")
    public void 도서_데이터베이스가_초기화되어_있다() {
        bookRepository.deleteAll();
    }

    // ── 도서 등록 ─────────────────────────────────────────────────

    @When("다음 정보로 도서를 등록한다:")
    public void 다음_정보로_도서를_등록한다(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        BookRequest request = new BookRequest(
                data.get("title"),
                data.get("author"),
                data.get("isbn"),
                new BigDecimal(data.get("price")),
                Integer.parseInt(data.get("stock"))
        );
        lastResponse = execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
    }

    @When("제목이 비어있는 도서를 등록하려 한다")
    public void 제목이_비어있는_도서를_등록하려_한다() {
        BookRequest request = new BookRequest("", "Test Author", "978-0000000000",
                BigDecimal.valueOf(19.99), 5);
        lastResponse = execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
    }

    @Given("ISBN {string}인 도서가 이미 등록되어 있다")
    public void ISBN_인_도서가_이미_등록되어_있다(String isbn) {
        BookRequest request = new BookRequest("기존 도서", "기존 저자", isbn,
                BigDecimal.valueOf(25.00), 5);
        execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
    }

    @When("ISBN {string}로 다른 도서를 등록하려 한다")
    public void ISBN_로_다른_도서를_등록하려_한다(String isbn) {
        BookRequest request = new BookRequest("중복 도서", "중복 저자", isbn,
                BigDecimal.valueOf(30.00), 3);
        lastResponse = execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
    }

    // ── 도서 조회 ─────────────────────────────────────────────────

    @Given("다음 도서들이 등록되어 있다:")
    public void 다음_도서들이_등록되어_있다(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            BookRequest request = new BookRequest(
                    row.get("title"), row.get("author"), row.get("isbn"),
                    new BigDecimal(row.get("price")), Integer.parseInt(row.get("stock"))
            );
            execute(() -> restClient.post()
                    .uri("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(String.class));
        }
    }

    @When("전체 도서 목록을 조회한다")
    public void 전체_도서_목록을_조회한다() {
        lastResponse = execute(() -> restClient.get()
                .uri("/api/books")
                .retrieve()
                .toEntity(String.class));
    }

    @Given("{string} 도서가 등록되어 있다")
    public void 도서가_등록되어_있다(String title) {
        BookRequest request = new BookRequest(title, "테스트 저자", generateIsbn(title),
                BigDecimal.valueOf(29.99), 5);
        ResponseEntity<String> response = execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
        try {
            savedBookId = objectMapper.readTree(response.getBody()).path("data").path("id").asLong();
        } catch (Exception e) {
            throw new RuntimeException("도서 등록 후 ID 파싱 실패", e);
        }
    }

    @When("해당 도서를 ID로 조회한다")
    public void 해당_도서를_ID로_조회한다() {
        lastResponse = execute(() -> restClient.get()
                .uri("/api/books/{id}", savedBookId)
                .retrieve()
                .toEntity(String.class));
    }

    @When("ID {long}로 도서를 조회한다")
    public void ID_로_도서를_조회한다(long id) {
        lastResponse = execute(() -> restClient.get()
                .uri("/api/books/{id}", id)
                .retrieve()
                .toEntity(String.class));
    }

    // ── 도서 수정 ─────────────────────────────────────────────────

    @When("해당 도서 가격을 {double}로 수정한다")
    public void 해당_도서_가격을_수정한다(double newPrice) {
        ResponseEntity<String> current = execute(() -> restClient.get()
                .uri("/api/books/{id}", savedBookId)
                .retrieve()
                .toEntity(String.class));
        try {
            JsonNode data = objectMapper.readTree(current.getBody()).path("data");
            BookRequest request = new BookRequest(
                    data.path("title").asText(), data.path("author").asText(),
                    data.path("isbn").asText(), BigDecimal.valueOf(newPrice),
                    data.path("stock").asInt()
            );
            lastResponse = execute(() -> restClient.put()
                    .uri("/api/books/{id}", savedBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(String.class));
        } catch (Exception e) {
            throw new RuntimeException("도서 수정 중 오류", e);
        }
    }

    @Given("재고가 {int}권인 {string} 도서가 등록되어 있다")
    public void 재고가_N권인_도서가_등록되어_있다(int initialStock, String title) {
        BookRequest request = new BookRequest(title, "테스트 저자", generateIsbn(title),
                BigDecimal.valueOf(39.99), initialStock);
        ResponseEntity<String> response = execute(() -> restClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(String.class));
        try {
            savedBookId = objectMapper.readTree(response.getBody()).path("data").path("id").asLong();
        } catch (Exception e) {
            throw new RuntimeException("도서 등록 후 ID 파싱 실패", e);
        }
    }

    @When("해당 도서 재고를 {int}으로 수정한다")
    public void 해당_도서_재고를_수정한다(int newStock) {
        ResponseEntity<String> current = execute(() -> restClient.get()
                .uri("/api/books/{id}", savedBookId)
                .retrieve()
                .toEntity(String.class));
        try {
            JsonNode data = objectMapper.readTree(current.getBody()).path("data");
            BookRequest request = new BookRequest(
                    data.path("title").asText(), data.path("author").asText(),
                    data.path("isbn").asText(),
                    new BigDecimal(data.path("price").asText()), newStock
            );
            lastResponse = execute(() -> restClient.put()
                    .uri("/api/books/{id}", savedBookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(String.class));
        } catch (Exception e) {
            throw new RuntimeException("도서 재고 수정 중 오류", e);
        }
    }

    // ── 도서 삭제 ─────────────────────────────────────────────────

    @When("해당 도서를 삭제한다")
    public void 해당_도서를_삭제한다() {
        lastResponse = execute(() -> restClient.delete()
                .uri("/api/books/{id}", savedBookId)
                .retrieve()
                .toEntity(String.class));
    }

    @When("ID {long}인 도서를 삭제하려 한다")
    public void ID_인_도서를_삭제하려_한다(long id) {
        lastResponse = execute(() -> restClient.delete()
                .uri("/api/books/{id}", id)
                .retrieve()
                .toEntity(String.class));
    }

    // ── Then: 검증 ───────────────────────────────────────────────

    @Then("응답 상태 코드는 {int}이다")
    public void 응답_상태_코드는_이다(int expectedStatusCode) {
        assertThat(lastResponse.getStatusCode().value())
                .as("HTTP 상태 코드")
                .isEqualTo(expectedStatusCode);
    }

    @Then("등록된 도서 제목은 {string}이다")
    public void 등록된_도서_제목은_이다(String expectedTitle) {
        assertThat(parseBody().path("data").path("title").asText()).isEqualTo(expectedTitle);
    }

    @Then("등록된 도서 재고는 {int}권이다")
    public void 등록된_도서_재고는_N권이다(int expectedStock) {
        assertThat(parseBody().path("data").path("stock").asInt()).isEqualTo(expectedStock);
    }

    @Then("오류 메시지에 {string} 필드 검증 오류가 포함된다")
    public void 오류_메시지에_필드_검증_오류가_포함된다(String fieldName) {
        assertThat(parseBody().path("data").has(fieldName))
                .as("응답 data에 '%s' 필드 오류가 있어야 합니다", fieldName)
                .isTrue();
    }

    @Then("도서 목록에 {int}권이 포함되어 있다")
    public void 도서_목록에_N권이_포함되어_있다(int expectedCount) {
        assertThat(parseBody().path("data").size()).isEqualTo(expectedCount);
    }

    @Then("조회된 도서 제목은 {string}이다")
    public void 조회된_도서_제목은_이다(String expectedTitle) {
        assertThat(parseBody().path("data").path("title").asText()).isEqualTo(expectedTitle);
    }

    @Then("수정된 도서 가격은 {double}이다")
    public void 수정된_도서_가격은_이다(double expectedPrice) {
        assertThat(parseBody().path("data").path("price").asDouble()).isEqualTo(expectedPrice);
    }

    @Then("수정된 도서 재고는 {int}권이다")
    public void 수정된_도서_재고는_N권이다(int expectedStock) {
        assertThat(parseBody().path("data").path("stock").asInt()).isEqualTo(expectedStock);
    }

    @Then("해당 도서는 더 이상 조회되지 않는다")
    public void 해당_도서는_더_이상_조회되지_않는다() {
        ResponseEntity<String> response = execute(() -> restClient.get()
                .uri("/api/books/{id}", savedBookId)
                .retrieve()
                .toEntity(String.class));
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
