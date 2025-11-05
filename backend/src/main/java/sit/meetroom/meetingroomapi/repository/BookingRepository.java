package sit.meetroom.meetingroomapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.meetroom.meetingroomapi.entity.Booking;
import sit.meetroom.meetingroomapi.entity.BookingStatus;
import sit.meetroom.meetingroomapi.entity.User;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
  SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
  FROM Booking b
  WHERE b.room.id = :roomId
    AND b.status = sit.meetroom.meetingroomapi.entity.BookingStatus.CONFIRMED
    AND b.startAt < :newEnd
    AND b.endAt   > :newStart
  """)
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("newStart") Instant newStart,
                          @Param("newEnd") Instant newEnd);

    @Query("""
  SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
  FROM Booking b
  WHERE b.room.id = :roomId
    AND b.id != :excludeBookingId
    AND b.status = sit.meetroom.meetingroomapi.entity.BookingStatus.CONFIRMED
    AND b.startAt < :newEnd
    AND b.endAt   > :newStart
  """)
    boolean existsOverlapExcludingSelf(
            @Param("roomId") Long roomId,
            @Param("excludeBookingId") Long excludeBookingId,
            @Param("newStart") Instant newStart,
            @Param("newEnd") Instant newEnd
    );

    @Query("""
  SELECT b FROM Booking b
  WHERE b.room.id = :roomId
    AND b.status = sit.meetroom.meetingroomapi.entity.BookingStatus.CONFIRMED
    AND b.startAt > :currentTime
  ORDER BY b.startAt ASC
  """)
    List<Booking> findFutureConfirmedBookingsByRoomId(
            @Param("roomId") Long roomId,
            @Param("currentTime") Instant currentTime
    );

    List<Booking> findAllByUserOrderByStartAtDesc(User user);

    List<Booking> findAllByUserAndStartAtAfter(User user, Instant time);

    Page<Booking> findAllByUserOrderByStartAtDesc(User user, Pageable pageable);
}