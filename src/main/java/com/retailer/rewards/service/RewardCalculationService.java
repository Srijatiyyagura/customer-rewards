package com.retailer.rewards.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service dedicated to calculating reward points for transaction amounts
 * according to retailer reward program rules.
 * <p>
 * Business Rules:
 * - 2 points for every dollar spent over $100 in each transaction
 * - 1 point for every dollar spent between $50 and $100 in each transaction
 * - Points are awarded on each recorded purchase transaction based on whole dollars spent.
 */
@Service
public class RewardCalculationService {

    public static final BigDecimal LOWER_THRESHOLD = BigDecimal.valueOf(50);
    public static final BigDecimal UPPER_THRESHOLD = BigDecimal.valueOf(100);
    public static final int POINTS_BETWEEN_50_AND_100_TIER = 50; // (100 - 50) * 1 point

    /**
     * Calculates reward points for a single transaction amount.
     * <p>
     * Examples:
     * - $45.00  -> 0 points
     * - $50.00  -> 0 points
     * - $51.00  -> 1 point (1 dollar between $50 and $100)
     * - $75.00  -> 25 points (25 dollars between $50 and $100)
     * - $100.00 -> 50 points (50 dollars between $50 and $100, 0 dollars over $100)
     * - $101.00 -> 52 points (50 * 1 + 1 * 2)
     * - $120.00 -> 90 points (50 * 1 + 20 * 2)
     *
     * @param amount Transaction amount
     * @return Calculated reward points (>= 0)
     */
    public long calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(LOWER_THRESHOLD) <= 0) {
            return 0L;
        }

        // Points are calculated on whole dollars spent
        long wholeDollars = amount.setScale(0, RoundingMode.FLOOR).longValue();
        long points = 0L;

        if (wholeDollars > UPPER_THRESHOLD.longValue()) {
            // 2 points for every dollar spent over $100
            long dollarsOver100 = wholeDollars - UPPER_THRESHOLD.longValue();
            points += (dollarsOver100 * 2L);
            // plus 1 point for every dollar spent between $50 and $100 (50 dollars * 1 pt = 50 pts)
            points += POINTS_BETWEEN_50_AND_100_TIER;
        } else if (wholeDollars > LOWER_THRESHOLD.longValue()) {
            // 1 point for every dollar spent between $50 and $100
            long dollarsBetween50And100 = wholeDollars - LOWER_THRESHOLD.longValue();
            points += dollarsBetween50And100;
        }

        return points;
    }
}
