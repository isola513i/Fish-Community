package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.entity.Booking;
import sit.meetroom.meetingroomapi.entity.BookingStatus;
import sit.meetroom.meetingroomapi.entity.Room;
import sit.meetroom.meetingroomapi.mapper.RoomMapper;
import sit.meetroom.meetingroomapi.repository.BookingRepository;
import sit.meetroom.meetingroomapi.repository.RoomRepository;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepo;
    private final RoomMapper roomMapper;
    private final BookingRepository bookingRepo;

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

    @Transactional
    public RoomDto update(Long id, RoomDto dto) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!room.getName().equalsIgnoreCase(dto.name()) &&
                roomRepo.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Room name already exists");
        }

        // Update fields
        room.setName(dto.name());
        room.setCapacity(dto.capacity());
        room.setLocation(dto.location());
        room.setEquipmentsJson(dto.equipmentsJson());

        if (dto.isActive() != null) {
            room.setIsActive(dto.isActive());
        }

        Room updatedRoom = roomRepo.save(room);
        return roomMapper.toRoomDto(updatedRoom);
    }

    @Transactional
    public void delete(Long id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id: " + id));

        // Check for future bookings
        List<Booking> futureBookings = bookingRepo.findFutureConfirmedBookingsByRoomId(
                id, Instant.now()
        );

        if (!futureBookings.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot delete room with " + futureBookings.size() +
                            " future booking(s). Please cancel all future bookings first."
            );
        }

        // Soft delete by setting isActive to false
        room.setIsActive(false);
        roomRepo.save(room);
    }

    @Transactional
    public void forceDelete(Long id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id: " + id));

        // Cancel all future bookings
        List<Booking> futureBookings = bookingRepo.findFutureConfirmedBookingsByRoomId(
                id, Instant.now()
        );

        Instant now = Instant.now();
        futureBookings.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(now);
        });
        bookingRepo.saveAll(futureBookings);

        // Soft delete
        room.setIsActive(false);
        roomRepo.save(room);
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
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id: " + id));
        return roomMapper.toRoomDto(room);
    }
}