package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.MonthlyRewardResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.service.RewardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
@DisplayName("RewardController MockMvc Tests")
class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardService rewardService;

    @Test
    @DisplayName("GET /api/rewards - should return list of customer rewards")
    void shouldReturnAllCustomerRewards() throws Exception {
        MonthlyRewardResponse month1 = new MonthlyRewardResponse(2026, "JULY", 7, 90L, BigDecimal.valueOf(120.00), 1);
        MonthlyRewardResponse month2 = new MonthlyRewardResponse(2026, "AUGUST", 8, 50L, BigDecimal.valueOf(100.00), 1);

        CustomerRewardResponse aliceRewards = new CustomerRewardResponse(
                1L, "Alice Johnson", "alice@example.com",
                Arrays.asList(month1, month2),
                140L, BigDecimal.valueOf(220.00)
        );

        when(rewardService.getAllCustomerRewards(any(), any())).thenReturn(Collections.singletonList(aliceRewards));

        mockMvc.perform(get("/api/rewards").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId", is(1)))
                .andExpect(jsonPath("$[0].customerName", is("Alice Johnson")))
                .andExpect(jsonPath("$[0].totalPoints", is(140)))
                .andExpect(jsonPath("$[0].monthlyRewards", hasSize(2)))
                .andExpect(jsonPath("$[0].monthlyRewards[0].month", is("JULY")))
                .andExpect(jsonPath("$[0].monthlyRewards[0].points", is(90)))
                .andExpect(jsonPath("$[0].monthlyRewards[1].month", is("AUGUST")))
                .andExpect(jsonPath("$[0].monthlyRewards[1].points", is(50)));
    }

    @Test
    @DisplayName("GET /rewards - should also route successfully without /api prefix")
    void shouldReturnRewards_WhenCallingPathWithoutApiPrefix() throws Exception {
        when(rewardService.getAllCustomerRewards(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rewards").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/rewards/{customerId} - should return rewards for specified customer")
    void shouldReturnRewardsForSpecificCustomer() throws Exception {
        MonthlyRewardResponse month = new MonthlyRewardResponse(2026, "SEPTEMBER", 9, 90L, BigDecimal.valueOf(120.00), 1);
        CustomerRewardResponse response = new CustomerRewardResponse(
                1L, "Alice Johnson", "alice@example.com",
                Collections.singletonList(month),
                90L, BigDecimal.valueOf(120.00)
        );

        when(rewardService.getCustomerRewardsById(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/rewards/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(1)))
                .andExpect(jsonPath("$.customerName", is("Alice Johnson")))
                .andExpect(jsonPath("$.totalPoints", is(90)))
                .andExpect(jsonPath("$.monthlyRewards[0].points", is(90)));
    }

    @Test
    @DisplayName("GET /api/rewards/{customerId} - should return 404 when customer not found")
    void shouldReturn404_WhenCustomerNotFound() throws Exception {
        when(rewardService.getCustomerRewardsById(eq(999L), any(), any()))
                .thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(get("/api/rewards/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Customer with ID 999 was not found.")));
    }

    @Test
    @DisplayName("GET /api/rewards - should return 400 when start date is after end date")
    void shouldReturn400_WhenStartDateAfterEndDate() throws Exception {
        when(rewardService.getAllCustomerRewards(any(), any()))
                .thenThrow(new InvalidDateRangeException("Start date cannot be after end date"));

        mockMvc.perform(get("/api/rewards?startDate=2026-10-01&endDate=2026-09-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Start date cannot be after end date")));
    }
}
