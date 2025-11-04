package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant; // ⬅️ (NEW) Import เพิ่ม

public record RoomDto(
        Long id,
        @NotBlank String name,
        @Min(1) Integer capacity,
        String location,
        String equipmentsJson,
        Boolean isActive,

        Instant createdAt,
        Instant updatedAt
) {}