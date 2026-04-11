package com.example.cucumber.book;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 도서 생성/수정 요청 DTO.
 * Java Record를 사용하여 불변 객체로 선언.
 */
public record BookRequest(

        @NotBlank(message = "제목은 필수입니다")
        String title,

        @NotBlank(message = "저자는 필수입니다")
        String author,

        @NotBlank(message = "ISBN은 필수입니다")
        @Pattern(regexp = "^[0-9\\-]{10,17}$", message = "올바른 ISBN 형식이 아닙니다")
        String isbn,

        @NotNull(message = "가격은 필수입니다")
        @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
        BigDecimal price,

        @NotNull(message = "재고 수량은 필수입니다")
        @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다")
        Integer stock
) {
}
