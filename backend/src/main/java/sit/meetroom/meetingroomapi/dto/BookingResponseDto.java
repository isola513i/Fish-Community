package sit.meetroom.meetingroomapi.dto;

import sit.meetroom.meetingroomapi.entity.BookingStatus;
import java.time.Instant;

public record BookingResponseDto(
        Long id,
        String title,
        String notes,
        BookingStatus status,
        Instant startAt,
        Instant endAt,
        Instant cancelledAt,
        RoomDto room,
        UserDto user
) {}