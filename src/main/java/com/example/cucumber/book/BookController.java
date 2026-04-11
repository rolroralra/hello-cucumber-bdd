package com.example.cucumber.book;

import com.example.cucumber.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 전체 도서 목록 조회
     * GET /api/books
     */
    @GetMapping
    public ApiResponse<List<BookResponse>> getAllBooks() {
        return ApiResponse.success(bookService.findAll());
    }

    /**
     * 도서 단건 조회
     * GET /api/books/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBook(@PathVariable Long id) {
        return ApiResponse.success(bookService.findById(id));
    }

    /**
     * 도서 등록
     * POST /api/books
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ApiResponse.success(bookService.create(request));
    }

    /**
     * 도서 수정
     * PUT /api/books/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        return ApiResponse.success(bookService.update(id, request));
    }

    /**
     * 도서 삭제
     * DELETE /api/books/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.delete(id);
    }
}
