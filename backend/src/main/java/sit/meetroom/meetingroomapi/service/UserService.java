package sit.meetroom.meetingroomapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.meetroom.meetingroomapi.config.SecurityUtils;
import sit.meetroom.meetingroomapi.dto.AdminUserUpdateDto;
import sit.meetroom.meetingroomapi.dto.ChangePasswordDto;
import sit.meetroom.meetingroomapi.dto.ProfileUpdateDto;
import sit.meetroom.meetingroomapi.dto.UserDto;
import sit.meetroom.meetingroomapi.entity.Booking;
import sit.meetroom.meetingroomapi.entity.BookingStatus;
import sit.meetroom.meetingroomapi.entity.User;
import sit.meetroom.meetingroomapi.mapper.UserMapper;
import sit.meetroom.meetingroomapi.repository.BookingRepository;
import sit.meetroom.meetingroomapi.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // --- User ---
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
        return userMapper.toUserDto(u);
    }

    @Transactional
    public UserDto updateCurrentUserProfile(ProfileUpdateDto dto) {
        User u = getCurrentUser();
        u.setFullName(dto.fullName());
        u.setTimezone(dto.timezone());
        User savedUser = userRepo.save(u);

        return userMapper.toUserDto(savedUser);
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

    // --- Admin ---
    public List<UserDto> listAllUsers() {
        List<User> users = userRepo.findAll();
        return userMapper.toUserDtoList(users);
    }

    public UserDto getUserById(Long id) {
        User user = userRepo.findById(id).orElseThrow();
        return userMapper.toUserDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, AdminUserUpdateDto dto) {
        User user = userRepo.findById(id).orElseThrow();
        user.setRole(dto.role());
        user.setIsActive(dto.isActive());
        User savedUser = userRepo.save(user);
        return userMapper.toUserDto(savedUser);
    }
}