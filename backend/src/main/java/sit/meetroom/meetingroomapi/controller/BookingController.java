package sit.meetroom.meetingroomapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sit.meetroom.meetingroomapi.dto.BookingCreateDto;
import sit.meetroom.meetingroomapi.dto.BookingResponseDto;
import sit.meetroom.meetingroomapi.dto.BookingUpdateDto;
import sit.meetroom.meetingroomapi.service.BookingService;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/my-bookings")
    public List<BookingResponseDto> getMyBookings() {
        return bookingService.listMyBookings();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponseDto create(@Valid @RequestBody BookingCreateDto dto) {
        return bookingService.create(dto);
    }

    @PutMapping("/{id}")
    public BookingResponseDto update(@PathVariable Long id, @Valid @RequestBody BookingUpdateDto dto) {
        return bookingService.update(id, dto);
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id){ bookingService.cancel(id); }
}
