package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RoomDto(
        Long id,
        @NotBlank String name,
        @Min(1) Integer capacity,
        String location,
        String equipmentsJson,
        Boolean isActive
) {}
