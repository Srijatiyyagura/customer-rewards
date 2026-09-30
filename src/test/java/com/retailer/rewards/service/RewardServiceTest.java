package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.MonthlyRewardResponse;
import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RewardService Unit Tests")
class RewardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private RewardCalculationService rewardCalculationService;
    private RewardService rewardService;

    private Customer alice;

    @BeforeEach
    void setUp() {
        rewardCalculationService = new RewardCalculationService();
        rewardService = new RewardService(customerRepository, transactionRepository, rewardCalculationService);
        alice = new Customer(1L, "Alice Johnson", "alice@example.com");
    }

    @Test
    @DisplayName("Should aggregate points by month and calculate overall total for a customer")
    void shouldCalculateMonthlyBreakdownAndTotalPointsForCustomer() {
        LocalDate month1Date = LocalDate.of(2026, 7, 15);
        LocalDate month2Date = LocalDate.of(2026, 8, 20);
        LocalDate month3DateA = LocalDate.of(2026, 9, 5);
        LocalDate month3DateB = LocalDate.of(2026, 9, 25);

        List<Transaction> transactions = Arrays.asList(
                // Month 1 (July): $120 -> 90 points
                new Transaction(1L, alice, BigDecimal.valueOf(120.00), month1Date, "July purchase"),
                // Month 2 (August): $100 -> 50 points
                new Transaction(2L, alice, BigDecimal.valueOf(100.00), month2Date, "August purchase"),
                // Month 3 (September): $200 -> 250 points, $45 -> 0 points. Month 3 total = 250 points
                new Transaction(3L, alice, BigDecimal.valueOf(200.00), month3DateA, "Sept purchase 1"),
                new Transaction(4L, alice, BigDecimal.valueOf(45.00), month3DateB, "Sept purchase 2")
        );

        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 30);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(transactions);

        CustomerRewardResponse response = rewardService.getCustomerRewardsById(1L, startDate, endDate);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getCustomerName()).isEqualTo("Alice Johnson");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");

        // Total points across the 3 months: 90 + 50 + 250 = 390
        assertThat(response.getTotalPoints()).isEqualTo(390L);
        assertThat(response.getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(465.00));

        // Monthly breakdown should contain 3 entries
        List<MonthlyRewardResponse> monthly = response.getMonthlyRewards();
        assertThat(monthly).hasSize(3);

        // July
        assertThat(monthly.get(0).getMonth()).isEqualTo("JULY");
        assertThat(monthly.get(0).getMonthNumber()).isEqualTo(7);
        assertThat(monthly.get(0).getYear()).isEqualTo(2026);
        assertThat(monthly.get(0).getPoints()).isEqualTo(90L);
        assertThat(monthly.get(0).getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(120.00));
        assertThat(monthly.get(0).getTransactionCount()).isEqualTo(1);

        // August
        assertThat(monthly.get(1).getMonth()).isEqualTo("AUGUST");
        assertThat(monthly.get(1).getMonthNumber()).isEqualTo(8);
        assertThat(monthly.get(1).getPoints()).isEqualTo(50L);
        assertThat(monthly.get(1).getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(monthly.get(1).getTransactionCount()).isEqualTo(1);

        // September
        assertThat(monthly.get(2).getMonth()).isEqualTo("SEPTEMBER");
        assertThat(monthly.get(2).getMonthNumber()).isEqualTo(9);
        assertThat(monthly.get(2).getPoints()).isEqualTo(250L);
        assertThat(monthly.get(2).getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(245.00));
        assertThat(monthly.get(2).getTransactionCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return 0 points and empty monthly list when customer has no transactions")
    void shouldReturnEmptyMonthlyRewardsAndZeroTotal_WhenCustomerHasNoTransactions() {
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 30);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(eq(1L), eq(startDate), eq(endDate)))
                .thenReturn(Collections.emptyList());

        CustomerRewardResponse response = rewardService.getCustomerRewardsById(1L, startDate, endDate);

        assertThat(response.getTotalPoints()).isZero();
        assertThat(response.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getMonthlyRewards()).isEmpty();
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer ID does not exist")
    void shouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rewardService.getCustomerRewardsById(999L, null, null))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should throw InvalidDateRangeException when startDate is after endDate")
    void shouldThrowInvalidDateRangeException_WhenStartDateIsAfterEndDate() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 1);

        assertThatThrownBy(() -> rewardService.resolveDateRange(startDate, endDate))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("cannot be after end date");
    }

    @Test
    @DisplayName("Should calculate rewards for all customers")
    void shouldCalculateRewardsForAllCustomers() {
        Customer bob = new Customer(2L, "Bob Smith", "bob@example.com");
        when(customerRepository.findAll()).thenReturn(Arrays.asList(alice, bob));

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(eq(2L), any(), any()))
                .thenReturn(Collections.emptyList());

        List<CustomerRewardResponse> allRewards = rewardService.getAllCustomerRewards(null, null);

        assertThat(allRewards).hasSize(2);
        assertThat(allRewards.get(0).getCustomerName()).isEqualTo("Alice Johnson");
        assertThat(allRewards.get(1).getCustomerName()).isEqualTo("Bob Smith");
    }

    @Test
    @DisplayName("Should resolve standard 3-calendar-month default date range")
    void shouldResolveDefaultThreeMonthDateRange() {
        RewardService.DateRange dateRange = rewardService.resolveDateRange(null, null);

        assertThat(dateRange.startDate()).isNotNull();
        assertThat(dateRange.endDate()).isNotNull();
        assertThat(dateRange.startDate()).isBefore(dateRange.endDate());
    }
}
