package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.dto.*;
import sit.meetroom.meetingroomapi.entity.*;
import sit.meetroom.meetingroomapi.exception.BookingConflictException;
import sit.meetroom.meetingroomapi.repository.*;

import java.time.Instant;
import java.util.List;

@Service @RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    // TODO: เปลี่ยน userId เป็นค่าจาก JWT ภายหลัง
    private User getMockUser() {
        return userRepo.findById(1L).orElseThrow(); // dev ชั่วคราว
    }

    @Transactional
    public BookingResponseDto create(BookingCreateDto dto) {
        if (!dto.startAt().isBefore(dto.endAt()))
            throw new IllegalArgumentException("endAt must be greater than startAt");

        Room room = roomRepo.findById(dto.roomId()).orElseThrow();

        boolean overlap = bookingRepo.existsOverlap(room.getId(), dto.startAt(), dto.endAt());
        if (overlap) throw new BookingConflictException("Time slot already booked");

        Booking b = Booking.builder()
                .room(room)
                .user(getMockUser())
                .title(dto.title())
                .startAt(dto.startAt())
                .endAt(dto.endAt())
                .notes(dto.notes())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepo.save(b);
        return toResponseDto(savedBooking);
    }

    @Transactional
    public BookingResponseDto update(Long id, BookingUpdateDto dto) { //
        Booking b = bookingRepo.findById(id).orElseThrow();
        if (!dto.startAt().isBefore(dto.endAt()))
            throw new IllegalArgumentException("endAt must be greater than startAt");

        boolean overlap = bookingRepo.existsOverlapExcludingSelf(
                b.getRoom().getId(), id, dto.startAt(), dto.endAt()
        );
        if (overlap) throw new BookingConflictException("Time slot already booked");

        b.setTitle(dto.title());
        b.setStartAt(dto.startAt());
        b.setEndAt(dto.endAt());
        b.setNotes(dto.notes());

        return toResponseDto(b);
    }

    @Transactional
    public void cancel(Long id) {
        Booking b = bookingRepo.findById(id).orElseThrow();
        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelledAt(Instant.now());
    }

    public List<Booking> calendar(Long roomId, Instant from, Instant to) {
        // TODO: เมธอดนี้ก็จะเจอปัญหา Lazy เหมือนกัน
        // ควรเปลี่ยน List<Booking> เป็น List<BookingResponseDto> ในอนาคต
        return bookingRepo.findForCalendar(roomId, from, to);
    }

    private BookingResponseDto toResponseDto(Booking b) {
        Room r = b.getRoom();
        RoomDto roomDto = new RoomDto(
                r.getId(),
                r.getName(),
                r.getCapacity(),
                r.getLocation(),
                r.getEquipmentsJson(),
                r.getIsActive()
        );

        User u = b.getUser();
        UserDto userDto = new UserDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name()
        );

        return new BookingResponseDto(
                b.getId(),
                b.getTitle(),
                b.getNotes(),
                b.getStatus(),
                b.getStartAt(),
                b.getEndAt(),
                b.getCancelledAt(),
                roomDto,
                userDto
        );
    }
}
