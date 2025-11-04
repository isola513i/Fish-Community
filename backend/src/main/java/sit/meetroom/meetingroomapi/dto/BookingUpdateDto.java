package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.*;
import sit.meetroom.meetingroomapi.validation.ValidTimeRange;
import java.time.Instant;

@ValidTimeRange(minDurationMinutes = 15, maxDurationHours = 8)
public record BookingUpdateDto(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title cannot exceed 255 characters")
        String title,

        @NotNull(message = "Start time is required")
        Instant startAt,

        @NotNull(message = "End time is required")
        Instant endAt,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {}