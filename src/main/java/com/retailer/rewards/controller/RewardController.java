package com.retailer.rewards.controller;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for customer reward calculations.
 * Supports both /api/rewards and /rewards endpoint paths.
 */
@RestController
@RequestMapping({"/api/rewards", "/rewards"})
@Tag(name = "Rewards", description = "Endpoints for retrieving customer reward points and monthly breakdowns")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    /**
     * Retrieves reward points earned by all customers, aggregated per month and overall total.
     * Defaults to the standard 3-calendar-month window if dates are not provided.
     */
    @GetMapping
    @Operation(
            summary = "Get rewards for all customers",
            description = "Calculates monthly reward breakdowns and total points for all customers. Defaults to the last 3 calendar months if date parameters are omitted."
    )
    public ResponseEntity<List<CustomerRewardResponse>> getAllCustomerRewards(
            @Parameter(description = "Optional start date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Optional end date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CustomerRewardResponse> rewards = rewardService.getAllCustomerRewards(startDate, endDate);
        return ResponseEntity.ok(rewards);
    }

    /**
     * Retrieves reward points for a single customer by ID, aggregated per month and overall total.
     * Defaults to the standard 3-calendar-month window if dates are not provided.
     */
    @GetMapping("/{customerId}")
    @Operation(
            summary = "Get rewards for a specific customer by ID",
            description = "Calculates monthly reward breakdowns and total points for a single customer by customer ID."
    )
    public ResponseEntity<CustomerRewardResponse> getCustomerRewardsById(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable Long customerId,
            @Parameter(description = "Optional start date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Optional end date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        CustomerRewardResponse reward = rewardService.getCustomerRewardsById(customerId, startDate, endDate);
        return ResponseEntity.ok(reward);
    }
}
