package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.config.SecurityUtils;
import sit.meetroom.meetingroomapi.dto.ChangePasswordDto;
import sit.meetroom.meetingroomapi.dto.ProfileUpdateDto;
import sit.meetroom.meetingroomapi.dto.UserDto;
import sit.meetroom.meetingroomapi.entity.Booking;
import sit.meetroom.meetingroomapi.entity.BookingStatus;
import sit.meetroom.meetingroomapi.entity.User;
import sit.meetroom.meetingroomapi.repository.BookingRepository;
import sit.meetroom.meetingroomapi.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String email = SecurityUtils.currentEmail();
        if (email == null) {
            throw new UsernameNotFoundException("User not authenticated");
        }
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public UserDto getCurrentUserProfile() {
        User u = getCurrentUser();
        return new UserDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name()
        );
    }

    @Transactional
    public UserDto updateCurrentUserProfile(ProfileUpdateDto dto) {
        User u = getCurrentUser();
        u.setFullName(dto.fullName());
        u.setTimezone(dto.timezone());
        User savedUser = userRepo.save(u);

        return new UserDto(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    @Transactional
    public void changePassword(ChangePasswordDto dto) {
        User u = getCurrentUser();

        if (!passwordEncoder.matches(dto.oldPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("Old password does not match");
        }

        u.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        userRepo.save(u);
    }

    @Transactional
    public void deleteCurrentUserAccount() {
        User u = getCurrentUser();

        List<Booking> futureBookings = bookingRepo.findAllByUserAndStartAtAfter(u, Instant.now());
        futureBookings.forEach(b -> {
            b.setStatus(BookingStatus.CANCELLED);
            b.setCancelledAt(Instant.now());
        });
        bookingRepo.saveAll(futureBookings);
        u.setIsActive(false);
        userRepo.save(u);
    }
}