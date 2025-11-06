package sit.meetroom.meetingroomapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.meetroom.meetingroomapi.entity.Booking;
import sit.meetroom.meetingroomapi.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    
    @Override
    @Query(value = "SELECT b FROM Booking b LEFT JOIN FETCH b.user u LEFT JOIN FETCH b.room r",
            countQuery = "SELECT COUNT(b) FROM Booking b")
    Page<Booking> findAll(Pageable pageable);


    List<Booking> findAllByUserOrderByStartAtDesc(User user);

    List<Booking> findAllByUserAndStartAtAfter(User user, Instant time);

    @Query(value = """
            SELECT b FROM Booking b
            LEFT JOIN FETCH b.user u
            LEFT JOIN FETCH b.room r
            WHERE b.user = :user
            """,
            countQuery = """
            SELECT COUNT(b) FROM Booking b
            WHERE b.user = :user
            """)
    Page<Booking> findAllByUserOrderByStartAtDesc(@Param("user") User user, Pageable pageable);

    @Query("""
    SELECT DISTINCT b.room.id
    FROM Booking b
    WHERE b.status = sit.meetroom.meetingroomapi.entity.BookingStatus.CONFIRMED
      AND b.startAt < :newEnd
      AND b.endAt   > :newStart
    """)
    Set<Long> findBookedRoomIdsBetween(
            @Param("newStart") Instant newStart,
            @Param("newEnd") Instant newEnd
    );

    @Query("""
    SELECT b FROM Booking b
    WHERE b.status = sit.meetroom.meetingroomapi.entity.BookingStatus.CONFIRMED
      AND b.endAt > :currentTime
    ORDER BY b.startAt ASC
    """)
    List<Booking> findAllCurrentAndFutureBookings(@Param("currentTime") Instant currentTime);
}