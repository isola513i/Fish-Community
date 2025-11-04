package sit.meetroom.meetingroomapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sit.meetroom.meetingroomapi.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByNameIgnoreCase(String name);
    Page<Room> findAllByIsActiveTrue(Pageable pageable);
}
