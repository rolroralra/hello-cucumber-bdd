package com.example.cucumber.common;

/**
 * 모든 REST API의 공통 응답 포맷.
 *
 * @param success 요청 성공 여부
 * @param data    응답 데이터 (실패 시 null)
 * @param message 메시지 (성공 시 null, 실패 시 오류 설명)
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
