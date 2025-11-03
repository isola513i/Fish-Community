package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record BookingUpdateDto(
        @NotBlank String title,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        String notes
) {}
