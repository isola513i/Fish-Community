package sit.meetroom.meetingroomapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sit.meetroom.meetingroomapi.dto.UserDto;
import sit.meetroom.meetingroomapi.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDto toUserDto(User user);

    List<UserDto> toUserDtoList(List<User> users);
}