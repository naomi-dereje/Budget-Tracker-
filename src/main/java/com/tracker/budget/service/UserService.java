package com.tracker.budget.service;

import com.tracker.budget.dto.ChangePasswordRequest;
import com.tracker.budget.dto.UserDto;
import com.tracker.budget.entity.User;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.repository.UserRepository;
import com.tracker.budget.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public UserDto getUserProfile() {
        Long userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User profile not found."));
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateProfile(UserDto dto) {
        Long userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User profile not found."));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName().trim());
        }
        if (dto.getUniversity() != null) {
            user.setUniversity(dto.getUniversity().trim());
        }
        if (dto.getCurrencySymbol() != null && !dto.getCurrencySymbol().isBlank()) {
            user.setCurrencySymbol(dto.getCurrencySymbol().trim());
        }
        if (dto.getMonthlyBudgetBaseline() != null && dto.getMonthlyBudgetBaseline().compareTo(BigDecimal.ZERO) >= 0) {
            user.setMonthlyBudgetBaseline(dto.getMonthlyBudgetBaseline());
        }

        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User profile not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException("New password must be at least 6 characters long.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getUniversity(),
                user.getCurrencySymbol(),
                user.getMonthlyBudgetBaseline()
        );
    }
}
