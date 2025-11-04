package sit.meetroom.meetingroomapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> validationErrors
) {
    // Constructor สำหรับ Error ทั่วไป
    public ErrorResponseDto(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null);
    }

    // Constructor สำหรับ Validation Error
    public ErrorResponseDto(int status, String error, String message, String path, List<String> validationErrors) {
        this(Instant.now(), status, error, message, path, validationErrors);
    }
}