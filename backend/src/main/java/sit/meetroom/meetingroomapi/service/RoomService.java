package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.dto.AdminRoomDto;
import sit.meetroom.meetingroomapi.dto.RoomDto;
import sit.meetroom.meetingroomapi.entity.Booking;
import sit.meetroom.meetingroomapi.entity.BookingStatus;
import sit.meetroom.meetingroomapi.entity.Room;
import sit.meetroom.meetingroomapi.mapper.BookingMapper;
import sit.meetroom.meetingroomapi.mapper.RoomMapper;
import sit.meetroom.meetingroomapi.repository.BookingRepository;
import sit.meetroom.meetingroomapi.repository.RoomRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepo;
    private final RoomMapper roomMapper;
    private final BookingRepository bookingRepo;
    private final BookingMapper bookingMapper;

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

        room.setIsActive(false);
        roomRepo.save(room);
    }

    @Transactional
    public void forceDelete(Long id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id: " + id));

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

    public List<RoomDto> findAvailableRooms(Instant startAt, Instant endAt) {
        Set<Long> bookedRoomIds = bookingRepo.findBookedRoomIdsBetween(startAt, endAt);

        List<Room> allActiveRooms = roomRepo.findAllByIsActiveTrue(Pageable.unpaged()).getContent();

        List<Room> availableRooms = allActiveRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());

        return roomMapper.toRoomDtoList(availableRooms);
    }

    public RoomDto get(Long id) {
        Room room = roomRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Room not found with id: " + id));
        return roomMapper.toRoomDto(room);
    }

    @Transactional(readOnly = true)
    public List<AdminRoomDto> listRoomsWithBookingStatus() {
        // 1. ดึงห้องทั้งหมด (ไม่สน page)
        List<Room> allRooms = roomRepo.findAll();

        // 2. ดึงการจอง "ทั้งหมด" ที่ยัง Active และยังไม่สิ้นสุด
        List<Booking> futureBookings = bookingRepo.findAllCurrentAndFutureBookings(Instant.now());

        // 3. (สำคัญ) Group การจองด้วย Room ID
        // และหาการจองที่ "ใกล้ที่สุด" (startAt น้อยสุด) ของแต่ละห้อง
        Map<Long, Booking> nextBookingByRoomId = futureBookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getRoom().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(Booking::getStartAt)),
                                optionalBooking -> optionalBooking.orElse(null)
                        )
                ));

        // 4. ประกอบร่าง DTO
        return allRooms.stream()
                .map(room -> new AdminRoomDto(
                        roomMapper.toRoomDto(room),
                        bookingMapper.toBookingResponseDto(
                                nextBookingByRoomId.get(room.getId()) // อาจจะเป็น null
                        )
                ))
                .sorted(Comparator.comparing(dto -> dto.room().name())) // เรียงตามชื่อห้อง
                .collect(Collectors.toList());
    }
}