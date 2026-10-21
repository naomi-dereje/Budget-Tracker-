package com.tracker.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.budget.dto.AuthRequest;
import com.tracker.budget.dto.RegisterRequest;
import com.tracker.budget.dto.UserDto;
import com.tracker.budget.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register should create user and return 201 Created")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john.doe@university.edu");
        request.setPassword("Password123!");
        request.setUniversity("Global University");
        request.setCurrencySymbol("$");
        request.setMonthlyBudgetBaseline(new BigDecimal("750.00"));

        UserDto userDto = new UserDto(1L, "john.doe@university.edu", "John Doe", "Global University", "$", new BigDecimal("750.00"));

        when(authService.register(any(RegisterRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@university.edu"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("POST /api/auth/register should reject invalid email with 400 Bad Request")
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("not-an-email"); // Invalid email format
        request.setPassword("Short");     // Too short (min 6)

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 on valid credentials")
    void login_Success() throws Exception {
        AuthRequest request = new AuthRequest("john.doe@university.edu", "Password123!");
        UserDto userDto = new UserDto(1L, "john.doe@university.edu", "John Doe", "Global University", "$", new BigDecimal("750.00"));

        when(authService.login(any(AuthRequest.class), any(), any())).thenReturn(userDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@university.edu"));
    }
}
