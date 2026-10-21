package com.tracker.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.budget.dto.TransactionRequest;
import com.tracker.budget.dto.TransactionResponse;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    @DisplayName("GET /api/transactions should return 401 for unauthenticated user")
    void getTransactions_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alex.student@campus.edu", roles = {"USER"})
    @DisplayName("POST /api/transactions should create transaction for authenticated user")
    void createTransaction_Authenticated_Success() throws Exception {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                1L,
                new BigDecimal("24.50"),
                "Campus bookstore supplies",
                LocalDate.of(2026, 10, 10)
        );

        TransactionResponse response = new TransactionResponse();
        response.setId(10L);
        response.setType(TransactionType.EXPENSE);
        response.setAmount(new BigDecimal("24.50"));
        response.setDescription("Campus bookstore supplies");
        response.setTransactionDate(LocalDate.of(2026, 10, 10));

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.amount").value(24.50));
    }

    @Test
    @WithMockUser(username = "alex.student@campus.edu", roles = {"USER"})
    @DisplayName("POST /api/transactions should reject invalid zero or negative amount with 400 Bad Request")
    void createTransaction_NegativeAmount_ReturnsBadRequest() throws Exception {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                1L,
                new BigDecimal("-5.00"),
                "Invalid negative amount",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
