package com.tracker.budget.controller;

import com.tracker.budget.dto.ApiResponse;
import com.tracker.budget.dto.AuthRequest;
import com.tracker.budget.dto.RegisterRequest;
import com.tracker.budget.dto.UserDto;
import com.tracker.budget.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserDto userDto = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. You can now log in.", userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody AuthRequest request,
                                                      HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        UserDto userDto = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", userDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        try {
            UserDto userDto = authService.getCurrentUserDto();
            return ResponseEntity.ok(ApiResponse.ok(userDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated."));
        }
    }
}
