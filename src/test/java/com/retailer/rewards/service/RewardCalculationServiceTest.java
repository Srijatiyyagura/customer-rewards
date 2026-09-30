package com.retailer.rewards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RewardCalculationService Unit Tests")
class RewardCalculationServiceTest {

    private RewardCalculationService rewardCalculationService;

    @BeforeEach
    void setUp() {
        rewardCalculationService = new RewardCalculationService();
    }

    @Test
    @DisplayName("Should return 0 points when amount is null")
    void shouldReturnZeroPoints_WhenAmountIsNull() {
        long points = rewardCalculationService.calculatePoints(null);
        assertThat(points).isZero();
    }

    @Test
    @DisplayName("Should return 0 points when amount is zero")
    void shouldReturnZeroPoints_WhenAmountIsZero() {
        long points = rewardCalculationService.calculatePoints(BigDecimal.ZERO);
        assertThat(points).isZero();
    }

    @Test
    @DisplayName("Should return 0 points when amount is negative")
    void shouldReturnZeroPoints_WhenAmountIsNegative() {
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(-25.50));
        assertThat(points).isZero();
    }

    @Test
    @DisplayName("Should return 0 points when amount is strictly below $50")
    void shouldReturnZeroPoints_WhenAmountIsBelowFifty() {
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(10.00))).isZero();
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(45.00))).isZero();
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(49.99))).isZero();
    }

    @Test
    @DisplayName("Should return 0 points when amount is exactly $50.00 boundary")
    void shouldReturnZeroPoints_WhenAmountIsExactlyFiftyDollars() {
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(50.00));
        assertThat(points).isZero();
    }

    @Test
    @DisplayName("Should return 1 point when amount is $51.00 (1 dollar over $50)")
    void shouldReturnOnePoint_WhenAmountIsFiftyOneDollars() {
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(51.00));
        assertThat(points).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should calculate 1 point per dollar between $50 and $100")
    void shouldCalculateCorrectPoints_BetweenFiftyAndOneHundredDollars() {
        // $75 = $25 over $50 = 25 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(75.00))).isEqualTo(25L);
        // $90 = $40 over $50 = 40 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(90.00))).isEqualTo(40L);
        // $99 = $49 over $50 = 49 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(99.00))).isEqualTo(49L);
    }

    @Test
    @DisplayName("Should return 50 points when amount is exactly $100.00 upper tier boundary")
    void shouldReturnFiftyPoints_WhenAmountIsExactlyOneHundredDollars() {
        // 50 dollars between $50 and $100 * 1 pt = 50 pts; 0 dollars over $100
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(100.00));
        assertThat(points).isEqualTo(50L);
    }

    @Test
    @DisplayName("Should return 52 points when amount is $101.00 (50 pts from $50-$100 tier + 2 pts for $1 over $100)")
    void shouldReturnFiftyTwoPoints_WhenAmountIsOneHundredAndOneDollars() {
        // 50 * 1 + (101 - 100) * 2 = 50 + 2 = 52
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(101.00));
        assertThat(points).isEqualTo(52L);
    }

    @Test
    @DisplayName("Should return 90 points for $120.00 purchase (2x$20 + 1x$50 = 90)")
    void shouldCalculatePoints_ForOneHundredTwentyDollarsPurchase() {
        // Standard specification example: 2 x $20 + 1 x $50 = 90 points
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(120.00));
        assertThat(points).isEqualTo(90L);
    }

    @Test
    @DisplayName("Should calculate correct points for larger purchases")
    void shouldCalculatePoints_ForLargerPurchases() {
        // $200 = 50 * 1 + (200 - 100) * 2 = 50 + 200 = 250 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(200.00))).isEqualTo(250L);
        // $300 = 50 * 1 + (300 - 100) * 2 = 50 + 400 = 450 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(300.00))).isEqualTo(450L);
    }

    @Test
    @DisplayName("Should award points based on whole dollars spent (floor rounding)")
    void shouldHandleFractionalAmounts_ByFloorWholeDollars() {
        // $50.99 = 50 whole dollars -> 0 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(50.99))).isZero();
        // $51.75 = 51 whole dollars -> 1 point
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(51.75))).isEqualTo(1L);
        // $99.99 = 99 whole dollars -> 49 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(99.99))).isEqualTo(49L);
        // $100.99 = 100 whole dollars -> 50 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(100.99))).isEqualTo(50L);
        // $120.50 = 120 whole dollars -> 90 points
        assertThat(rewardCalculationService.calculatePoints(BigDecimal.valueOf(120.50))).isEqualTo(90L);
    }

    @ParameterizedTest(name = "Amount {0} should earn {1} points")
    @CsvSource({
            "0.00, 0",
            "49.99, 0",
            "50.00, 0",
            "51.00, 1",
            "70.00, 20",
            "99.00, 49",
            "100.00, 50",
            "101.00, 52",
            "120.00, 90",
            "150.00, 150",
            "200.00, 250"
    })
    @DisplayName("Parameterized test for various purchase amounts")
    void parameterizedTest_PointsCalculation(double amount, long expectedPoints) {
        long points = rewardCalculationService.calculatePoints(BigDecimal.valueOf(amount));
        assertThat(points).isEqualTo(expectedPoints);
    }
}
