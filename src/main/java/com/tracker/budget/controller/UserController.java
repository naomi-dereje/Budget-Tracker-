package com.tracker.budget.controller;

import com.tracker.budget.dto.ApiResponse;
import com.tracker.budget.dto.ChangePasswordRequest;
import com.tracker.budget.dto.UserDto;
import com.tracker.budget.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile() {
        UserDto profile = userService.getUserProfile();
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@RequestBody UserDto dto) {
        UserDto updated = userService.updateProfile(dto);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", updated));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully.", null));
    }
}
