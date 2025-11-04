package sit.meetroom.meetingroomapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.service.RoomService;


@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping
    public Page<RoomDto> list(Pageable pageable) {
        return roomService.listPaginated(pageable);
    }

    @GetMapping("/{id}")
    public RoomDto get(@PathVariable Long id) {
        return roomService.get(id);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public RoomDto create(@Valid @RequestBody RoomDto dto) {
        return roomService.create(dto);
    }
}
