package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.config.SecurityUtils;
import sit.meetroom.meetingroomapi.dto.*;
import sit.meetroom.meetingroomapi.entity.*;
import sit.meetroom.meetingroomapi.exception.BookingConflictException;
import sit.meetroom.meetingroomapi.exception.ForbiddenException;
import sit.meetroom.meetingroomapi.mapper.BookingMapper;
import sit.meetroom.meetingroomapi.repository.*;

import java.time.Instant;

@Service @RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    private final BookingMapper bookingMapper;

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
        return bookingMapper.toBookingResponseDto(savedBooking);
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

        return bookingMapper.toBookingResponseDto(b);
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

    public Page<BookingResponseDto> listMyBookingsPaginated(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Booking> bookingPage = bookingRepo.findAllByUserOrderByStartAtDesc(currentUser, pageable);
        return bookingPage.map(bookingMapper::toBookingResponseDto);
    }

    public Page<BookingResponseDto> listAllBookingsPaginated(Pageable pageable) {
        Page<Booking> bookingPage = bookingRepo.findAll(pageable);
        return bookingPage.map(bookingMapper::toBookingResponseDto);
    }
}
