package com.tracker.budget.service;

import com.tracker.budget.dto.AuthRequest;
import com.tracker.budget.dto.RegisterRequest;
import com.tracker.budget.dto.UserDto;
import com.tracker.budget.entity.User;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.repository.UserRepository;
import com.tracker.budget.security.SecurityUtils;
import com.tracker.budget.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtils securityUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with email " + request.getEmail() + " already exists.");
        }

        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setUniversity(request.getUniversity() != null ? request.getUniversity().trim() : null);
        user.setCurrencySymbol(request.getCurrencySymbol() != null && !request.getCurrencySymbol().isBlank()
                ? request.getCurrencySymbol().trim() : "$");
        user.setMonthlyBudgetBaseline(request.getMonthlyBudgetBaseline() != null
                ? request.getMonthlyBudgetBaseline() : BigDecimal.ZERO);

        User saved = userRepository.save(user);

        return mapToDto(saved);
    }

    public UserDto login(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        org.springframework.security.web.context.HttpSessionSecurityContextRepository repo =
                new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
        repo.saveContext(securityContext, httpRequest, httpResponse);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User record not found"));

        return mapToDto(user);
    }

    public UserDto getCurrentUserDto() {
        Long userId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Current user not found"));
        return mapToDto(user);
    }

    public User getCurrentUserEntity() {
        Long userId = securityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Current user not found"));
    }

    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
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
