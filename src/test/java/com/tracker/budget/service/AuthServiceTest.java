package com.tracker.budget.service;

import com.tracker.budget.dto.RegisterRequest;
import com.tracker.budget.dto.UserDto;
import com.tracker.budget.entity.User;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.repository.UserRepository;
import com.tracker.budget.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("student@university.edu");
        registerRequest.setPassword("Secret123!");
        registerRequest.setFullName("Jane Student");
        registerRequest.setUniversity("State Tech");
        registerRequest.setCurrencySymbol("$");
        registerRequest.setMonthlyBudgetBaseline(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("Should successfully register a new user with hashed password")
    void register_Success() {
        when(userRepository.existsByEmail("student@university.edu")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("$2a$10$hashedPasswordHere");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setEmail("student@university.edu");
        savedUser.setPassword("$2a$10$hashedPasswordHere");
        savedUser.setFullName("Jane Student");
        savedUser.setUniversity("State Tech");
        savedUser.setCurrencySymbol("$");
        savedUser.setMonthlyBudgetBaseline(new BigDecimal("800.00"));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("student@university.edu", result.getEmail());
        assertEquals("Jane Student", result.getFullName());
        assertEquals("$", result.getCurrencySymbol());
        assertEquals(new BigDecimal("800.00"), result.getMonthlyBudgetBaseline());

        verify(passwordEncoder, times(1)).encode("Secret123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email already exists")
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            authService.register(registerRequest);
        });

        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should retrieve current authenticated user details")
    void getCurrentUserDto_Success() {
        when(securityUtils.getCurrentUserId()).thenReturn(10L);

        User user = new User();
        user.setId(10L);
        user.setEmail("student@university.edu");
        user.setFullName("Jane Student");
        user.setCurrencySymbol("$");
        user.setMonthlyBudgetBaseline(new BigDecimal("800.00"));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        UserDto dto = authService.getCurrentUserDto();

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("student@university.edu", dto.getEmail());
    }
}
