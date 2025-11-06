package sit.meetroom.meetingroomapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminRoomDto(
        RoomDto room,
        BookingResponseDto nextBooking
) {}