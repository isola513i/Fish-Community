package sit.meetroom.meetingroomapi.mapper;

import org.mapstruct.Mapper;
import sit.meetroom.meetingroomapi.dto.BookingResponseDto;
import sit.meetroom.meetingroomapi.entity.Booking;

import java.util.List;

@Mapper(componentModel = "spring", uses = {RoomMapper.class, UserMapper.class})
public interface BookingMapper {
    BookingResponseDto toBookingResponseDto(Booking booking);
    List<BookingResponseDto> toBookingResponseDtoList(List<Booking> bookings);
}