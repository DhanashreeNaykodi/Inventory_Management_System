package com.example.inventory_factory_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private PaginationInfo pagination;
    private String timestamp;

    public BaseResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // Success methods
    public static <T> BaseResponseDTO<T> success(T data) {
        return new BaseResponseDTO<>(true, "Success", data);
    }

    public static <T> BaseResponseDTO<T> success(String message, T data) {
        return new BaseResponseDTO<>(true, message, data);
    }

    public static <T> BaseResponseDTO<T> success(String message) {
        return new BaseResponseDTO<>(true, message, null);
    }

    // Error methods
    public static <T> BaseResponseDTO<T> error(String message) {
        return new BaseResponseDTO<>(false, message, null);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}