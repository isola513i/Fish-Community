package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.entity.Room;
import sit.meetroom.meetingroomapi.mapper.RoomMapper;
import sit.meetroom.meetingroomapi.repository.RoomRepository;


@Service @RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepo;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomDto create(RoomDto dto) {
        if (roomRepo.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Room name already exists");
        }
        Room r = roomMapper.toRoom(dto);
        r.setIsActive(dto.isActive() == null || dto.isActive());

        Room savedRoom = roomRepo.save(r);
        return roomMapper.toRoomDto(savedRoom);
    }

    public Page<RoomDto> listPaginated(Pageable pageable) {
        Page<Room> roomPage = roomRepo.findAll(pageable);
        return roomPage.map(roomMapper::toRoomDto);
    }

    public Page<RoomDto> listActivePaginated(Pageable pageable) {
        Page<Room> roomPage = roomRepo.findAllByIsActiveTrue(pageable);
        return roomPage.map(roomMapper::toRoomDto);
    }

    public RoomDto get(Long id) {
        Room room = roomRepo.findById(id).orElseThrow();
        return roomMapper.toRoomDto(room);
    }
}
