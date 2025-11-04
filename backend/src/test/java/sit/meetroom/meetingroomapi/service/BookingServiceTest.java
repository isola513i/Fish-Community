package sit.meetroom.meetingroomapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sit.meetroom.meetingroomapi.dto.BookingCreateDto;
import sit.meetroom.meetingroomapi.dto.BookingResponseDto;
import sit.meetroom.meetingroomapi.entity.*;
import sit.meetroom.meetingroomapi.exception.BookingConflictException;
import sit.meetroom.meetingroomapi.mapper.BookingMapper;
import sit.meetroom.meetingroomapi.repository.*;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepo;

    @Mock
    private RoomRepository roomRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingService bookingService;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .role(UserRole.MEMBER)
                .isActive(true)
                .build();

        testRoom = Room.builder()
                .id(1L)
                .name("Conference Room A")
                .capacity(10)
                .isActive(true)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateBooking_Success() {
        // Arrange
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(7200);

        BookingCreateDto dto = new BookingCreateDto(
                1L, "Team Meeting", start, end, "Important meeting"
        );

        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepo.existsOverlap(anyLong(), any(), any())).thenReturn(false);

        when(bookingRepo.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        when(bookingMapper.toBookingResponseDto(any())).thenReturn(
                new BookingResponseDto(1L, "Team Meeting", "Important meeting",
                        BookingStatus.CONFIRMED, start, end, null, null, null)
        );

        // Act
        BookingResponseDto result = bookingService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Team Meeting", result.title());
        assertEquals(BookingStatus.CONFIRMED, result.status());
        verify(bookingRepo, times(1)).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_Conflict() {
        // Arrange
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(7200);

        BookingCreateDto dto = new BookingCreateDto(
                1L, "Team Meeting", start, end, null
        );

        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));
        when(bookingRepo.existsOverlap(anyLong(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(BookingConflictException.class, () -> bookingService.create(dto));

        verify(bookingRepo, never()).save(any());
    }

    @Test
    void testCreateBooking_InvalidTimeRange() {
        // Arrange
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.minusSeconds(1800); // end before start

        BookingCreateDto dto = new BookingCreateDto(
                1L, "Team Meeting", start, end, null
        );

        when(roomRepo.findById(1L)).thenReturn(Optional.of(testRoom));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.create(dto));

        verify(bookingRepo, never()).save(any());
    }

    @Test
    void testCreateBooking_RoomNotFound() {
        // Arrange
        Instant start = Instant.now().plusSeconds(3600);
        Instant end = start.plusSeconds(7200);

        BookingCreateDto dto = new BookingCreateDto(
                999L, "Team Meeting", start, end, null
        );

        when(roomRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> bookingService.create(dto));

        verify(bookingRepo, never()).save(any());
    }
}