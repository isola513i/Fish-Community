package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateDto(
        @NotBlank
        @Size(max = 255)
        String fullName,

        @NotBlank
        @Size(max = 64)
        String timezone
) {}