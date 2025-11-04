package sit.meetroom.meetingroomapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sit.meetroom.meetingroomapi.dto.ChangePasswordDto;
import sit.meetroom.meetingroomapi.dto.ProfileUpdateDto;
import sit.meetroom.meetingroomapi.dto.UserDto;
import sit.meetroom.meetingroomapi.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDto getCurrentUserProfile() {
        return userService.getCurrentUserProfile();
    }

    @PutMapping("/me")
    public UserDto updateCurrentUserProfile(@Valid @RequestBody ProfileUpdateDto dto) {
        return userService.updateCurrentUserProfile(dto);
    }

    @PutMapping("/me/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        userService.changePassword(dto);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrentUserAccount() {
        userService.deleteCurrentUserAccount();
    }
}