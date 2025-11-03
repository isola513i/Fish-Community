package sit.meetroom.meetingroomapi.dto;

public record UserDto(
        Long id,
        String fullName,
        String email,
        String role
) {}