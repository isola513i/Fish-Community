package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank
        String oldPassword,

        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {}