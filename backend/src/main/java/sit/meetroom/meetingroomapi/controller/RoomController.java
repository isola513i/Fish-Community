package sit.meetroom.meetingroomapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.entity.Room;
import sit.meetroom.meetingroomapi.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping public List<Room> list(){ return roomService.list(); }

    @GetMapping("/{id}") public Room get(@PathVariable Long id){ return roomService.get(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Room create(@Valid @RequestBody RoomDto dto){ return roomService.create(dto); }
}
