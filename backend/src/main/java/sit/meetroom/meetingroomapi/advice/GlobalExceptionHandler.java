package sit.meetroom.meetingroomapi.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sit.meetroom.meetingroomapi.dto.ErrorResponseDto;
import sit.meetroom.meetingroomapi.exception.BookingConflictException;
import sit.meetroom.meetingroomapi.exception.ForbiddenException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper สำหรับสร้าง Error Response
    private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    // 400 - Validation Errors (จาก @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation Failed",
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 400 - Bad Request (เช่น รหัสผ่านเก่าไม่ถูก, เวลาไม่ถูกต้อง)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 403 - Forbidden (ไม่มีสิทธิ์)
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // 404 - Not Found (เช่น หา User ไม่เจอ)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 404 - Not Found (เช่น หา Booking/Room ไม่เจอ จาก .orElseThrow())
    // (java.util.NoSuchElementException คือตัวที่ .orElseThrow() โยนออกมา)
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDto> handleNoSuchElementException(java.util.NoSuchElementException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "The requested resource was not found", request);
    }

    // 409 - Conflict (จองชน)
    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleBookingConflictException(BookingConflictException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 500 - Internal Server Error (Error ที่เราไม่ได้ดักจับไว้)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        // Log error นี้ไว้ดูใน Production
        ex.printStackTrace();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), request);
    }
}