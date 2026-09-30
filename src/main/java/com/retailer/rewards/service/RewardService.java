package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.MonthlyRewardResponse;
import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service responsible for aggregating customer reward points across monthly intervals and overall total.
 */
@Service
@Transactional(readOnly = true)
public class RewardService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final RewardCalculationService rewardCalculationService;

    public RewardService(CustomerRepository customerRepository,
                         TransactionRepository transactionRepository,
                         RewardCalculationService rewardCalculationService) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.rewardCalculationService = rewardCalculationService;
    }

    /**
     * Calculates reward points for all customers over the specified or default 3-month date range.
     *
     * @param startDate Optional start date (inclusive)
     * @param endDate   Optional end date (inclusive)
     * @return List of reward responses for each customer
     */
    public List<CustomerRewardResponse> getAllCustomerRewards(LocalDate startDate, LocalDate endDate) {
        DateRange dateRange = resolveDateRange(startDate, endDate);
        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(customer -> calculateCustomerRewards(customer, dateRange.startDate(), dateRange.endDate()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates reward points for a single customer by customer ID over the specified or default 3-month date range.
     *
     * @param customerId ID of the customer
     * @param startDate  Optional start date (inclusive)
     * @param endDate    Optional end date (inclusive)
     * @return Reward response for the customer
     */
    public CustomerRewardResponse getCustomerRewardsById(Long customerId, LocalDate startDate, LocalDate endDate) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        DateRange dateRange = resolveDateRange(startDate, endDate);
        return calculateCustomerRewards(customer, dateRange.startDate(), dateRange.endDate());
    }

    /**
     * Aggregates transactions for a customer within a date range into monthly breakdowns and overall totals.
     */
    private CustomerRewardResponse calculateCustomerRewards(Customer customer, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findByCustomerIdAndTransactionDateBetween(
                customer.getId(), startDate, endDate);

        // Group transactions by YearMonth using a sorted map to preserve chronological order
        Map<YearMonth, List<Transaction>> transactionsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTransactionDate()),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<MonthlyRewardResponse> monthlyRewards = new ArrayList<>();
        long totalPoints = 0L;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Map.Entry<YearMonth, List<Transaction>> entry : transactionsByMonth.entrySet()) {
            YearMonth yearMonth = entry.getKey();
            List<Transaction> monthTransactions = entry.getValue();

            long monthPoints = 0L;
            BigDecimal monthSpent = BigDecimal.ZERO;

            for (Transaction transaction : monthTransactions) {
                long points = rewardCalculationService.calculatePoints(transaction.getAmount());
                monthPoints += points;
                monthSpent = monthSpent.add(transaction.getAmount());
            }

            monthlyRewards.add(new MonthlyRewardResponse(
                    yearMonth.getYear(),
                    yearMonth.getMonth().name(),
                    yearMonth.getMonthValue(),
                    monthPoints,
                    monthSpent,
                    monthTransactions.size()
            ));

            totalPoints += monthPoints;
            totalSpent = totalSpent.add(monthSpent);
        }

        return new CustomerRewardResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                monthlyRewards,
                totalPoints,
                totalSpent
        );
    }

    /**
     * Resolves the effective date range. Defaults to a standard 3-calendar-month window
     * (the first day of the month 2 months prior up to today).
     */
    public DateRange resolveDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new InvalidDateRangeException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
            }
            return new DateRange(startDate, endDate);
        }

        if (startDate != null) {
            LocalDate defaultEnd = startDate.plusMonths(3).minusDays(1);
            return new DateRange(startDate, defaultEnd);
        }

        if (endDate != null) {
            LocalDate defaultStart = YearMonth.from(endDate).minusMonths(2).atDay(1);
            return new DateRange(defaultStart, endDate);
        }

        // Default 3-month window: First day of 2 months ago through the end of the current month
        LocalDate today = LocalDate.now();
        LocalDate defaultStart = YearMonth.from(today).minusMonths(2).atDay(1);
        LocalDate defaultEnd = YearMonth.from(today).atEndOfMonth();
        return new DateRange(defaultStart, defaultEnd);
    }

    public record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
