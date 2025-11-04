package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.config.SecurityUtils;
import sit.meetroom.meetingroomapi.dto.*;
import sit.meetroom.meetingroomapi.entity.*;
import sit.meetroom.meetingroomapi.exception.BookingConflictException;
import sit.meetroom.meetingroomapi.exception.ForbiddenException;
import sit.meetroom.meetingroomapi.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;

    private User getCurrentUser() {
        String email = SecurityUtils.currentEmail(); //
        if (email == null) {
            throw new UsernameNotFoundException("User not authenticated");
        }
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
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
                .user(getCurrentUser())
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
    public BookingResponseDto update(Long id, BookingUpdateDto dto) {
        Booking b = bookingRepo.findById(id).orElseThrow();

        checkBookingPermission(b, getCurrentUser());

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

        checkBookingPermission(b, getCurrentUser());

        b.setStatus(BookingStatus.CANCELLED);
        b.setCancelledAt(Instant.now());
    }

    private void checkBookingPermission(Booking booking, User currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (booking.getUser().getId().equals(currentUser.getId())) {
            return;
        }
        throw new ForbiddenException("You do not have permission to access this booking");
    }

    // --- (NEW) Feature: My Bookings (Feature 4, 5) ---
    public List<BookingResponseDto> listMyBookings() {
        User currentUser = getCurrentUser();
        List<Booking> bookings = bookingRepo.findAllByUserOrderByStartAtDesc(currentUser);

        // แปลงเป็น DTO เพื่อแก้ปัญหา Lazy Loading และส่งข้อมูลที่จำเป็น
        return bookings.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
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
