package sit.meetroom.meetingroomapi.dto;

import jakarta.validation.constraints.NotNull;
import sit.meetroom.meetingroomapi.entity.UserRole;

public record AdminUserUpdateDto(
        @NotNull
        UserRole role,

        @NotNull
        Boolean isActive
) {}