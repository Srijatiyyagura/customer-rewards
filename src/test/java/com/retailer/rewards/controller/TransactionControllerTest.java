package com.retailer.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.rewards.dto.TransactionRequest;
import com.retailer.rewards.dto.TransactionResponse;
import com.retailer.rewards.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController MockMvc Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    @DisplayName("GET /api/transactions - should return list of transactions")
    void shouldReturnTransactions() throws Exception {
        TransactionResponse tx = new TransactionResponse(
                1L, 1L, "Alice Johnson", BigDecimal.valueOf(120.00),
                LocalDate.of(2026, 9, 15), "Electronics", 90L);

        when(transactionService.getTransactions(any(), any(), any()))
                .thenReturn(Collections.singletonList(tx));

        mockMvc.perform(get("/api/transactions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].amount", is(120.0)))
                .andExpect(jsonPath("$[0].pointsEarned", is(90)));
    }

    @Test
    @DisplayName("GET /transactions - should also work without /api prefix")
    void shouldWorkWithoutApiPrefix() throws Exception {
        when(transactionService.getTransactions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/transactions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/transactions - should create transaction and return 201 Created")
    void shouldCreateTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, BigDecimal.valueOf(120.00), LocalDate.of(2026, 9, 15), "Electronics purchase");

        TransactionResponse response = new TransactionResponse(
                10L, 1L, "Alice Johnson", BigDecimal.valueOf(120.00),
                LocalDate.of(2026, 9, 15), "Electronics purchase", 90L);

        when(transactionService.recordTransaction(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.pointsEarned", is(90)))
                .andExpect(jsonPath("$.amount", is(120.0)));
    }

    @Test
    @DisplayName("POST /api/transactions - should return 400 Bad Request when amount is missing or invalid")
    void shouldReturn400_WhenAmountIsInvalid() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, BigDecimal.valueOf(-10.00), LocalDate.of(2026, 9, 15), "Invalid amount");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.amount").exists());
    }

    @Test
    @DisplayName("POST /api/transactions - should return 400 Bad Request when customerId is missing")
    void shouldReturn400_WhenCustomerIdIsMissing() throws Exception {
        TransactionRequest request = new TransactionRequest(
                null, BigDecimal.valueOf(50.00), LocalDate.of(2026, 9, 15), "Missing customer");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.customerId").exists());
    }
}
