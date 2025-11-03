package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.entity.Room;
import sit.meetroom.meetingroomapi.repository.RoomRepository;

import java.util.List;

@Service @RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepo;

    @Transactional
    public Room create(RoomDto dto) {
        if (roomRepo.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Room name already exists");
        }
        Room r = Room.builder()
                .name(dto.name())
                .capacity(dto.capacity())
                .location(dto.location())
                .equipmentsJson(dto.equipmentsJson())
                .isActive(dto.isActive() == null || dto.isActive())
                .build();
        return roomRepo.save(r);
    }

    public List<Room> list(){ return roomRepo.findAll(); }
    public Room get(Long id){ return roomRepo.findById(id).orElseThrow(); }
}
