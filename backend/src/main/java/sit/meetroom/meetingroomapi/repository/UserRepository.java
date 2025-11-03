package sit.meetroom.meetingroomapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.meetroom.meetingroomapi.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
