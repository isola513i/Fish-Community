package sit.meetroom.meetingroomapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sit.meetroom.meetingroomapi.dto.AdminRoomDto;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.service.RoomService;

import java.time.Instant;
import java.util.List;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomDto create(@Valid @RequestBody RoomDto dto) {
        return roomService.create(dto);
    }

    @PutMapping("/{id}")
    public RoomDto update(@PathVariable Long id, @Valid @RequestBody RoomDto dto) {
        return roomService.update(id, dto);
    }

    @GetMapping("/available")
    public List<RoomDto> listAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endAt
    ) {
        return roomService.findAvailableRooms(startAt, endAt);
    }

    @GetMapping("/admin-list")
    public List<AdminRoomDto> listForAdmin() {
        return roomService.listRoomsWithBookingStatus();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }

    @DeleteMapping("/{id}/force")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forceDelete(@PathVariable Long id) {
        roomService.forceDelete(id);
    }
}