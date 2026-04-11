package com.example.cucumber.book;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 도서 응답 DTO.
 * 엔티티를 직접 노출하지 않고 필요한 필드만 반환.
 */
public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPrice(),
                book.getStock(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
