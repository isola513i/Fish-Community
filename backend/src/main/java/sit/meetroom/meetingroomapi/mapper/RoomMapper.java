package sit.meetroom.meetingroomapi.mapper;

import org.mapstruct.Mapper;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.entity.Room;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    RoomDto toRoomDto(Room room);
    List<RoomDto> toRoomDtoList(List<Room> rooms);
    Room toRoom(RoomDto roomDto);
}