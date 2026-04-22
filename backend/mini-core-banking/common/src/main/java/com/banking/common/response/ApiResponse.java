package com.banking.common.response;

import java.time.Instant;

/**
 * 모든 API 응답의 공통 래퍼.
 * 성공: success=true, data 채움, error=null
 * 실패: success=false, data=null, error 채움
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message), Instant.now());
    }

    public record ApiError(String code, String message) {
    }
}
