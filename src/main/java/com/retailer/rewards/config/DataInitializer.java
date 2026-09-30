package com.retailer.rewards.config;

import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes a sample dataset demonstrating varied customer scenarios,
 * boundary test cases, edge cases, and out-of-range historical data.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public DataInitializer(CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing sample dataset for Customer Rewards Program...");

        // 1. Create Customers
        Customer alice = new Customer("Alice Johnson", "alice.johnson@example.com");
        Customer bob = new Customer("Bob Smith", "bob.smith@example.com");
        Customer charlie = new Customer("Charlie Brown", "charlie.brown@example.com");
        Customer diana = new Customer("Diana Prince", "diana.prince@example.com");

        customerRepository.saveAll(Arrays.asList(alice, bob, charlie, diana));

        // 2. Establish dynamic dates relative to current date to always guarantee
        // a valid 3-month window whenever the application is launched.
        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);
        YearMonth oneMonthAgo = currentYearMonth.minusMonths(1);
        YearMonth twoMonthsAgo = currentYearMonth.minusMonths(2);
        YearMonth fourMonthsAgo = currentYearMonth.minusMonths(4); // Outside the 3-month window

        LocalDate dateMonth0A = currentYearMonth.atDay(Math.min(5, currentYearMonth.lengthOfMonth()));
        LocalDate dateMonth0B = currentYearMonth.atDay(Math.min(18, currentYearMonth.lengthOfMonth()));

        LocalDate dateMonth1A = oneMonthAgo.atDay(10);
        LocalDate dateMonth1B = oneMonthAgo.atDay(22);

        LocalDate dateMonth2A = twoMonthsAgo.atDay(12);
        LocalDate dateMonth2B = twoMonthsAgo.atDay(25);

        LocalDate dateOld = fourMonthsAgo.atDay(15); // >3 months ago

        // 3. Transactions for Alice: Active across 3 months ($120 example, $100 boundary, $50 boundary)
        List<Transaction> transactions = Arrays.asList(
                // Month 2 (2 months ago)
                new Transaction(alice, BigDecimal.valueOf(120.00), dateMonth2A, "Electronics purchase ($120 = 90 pts)"),
                // Month 1 (1 month ago)
                new Transaction(alice, BigDecimal.valueOf(100.00), dateMonth1A, "Home goods ($100 = 50 pts)"),
                new Transaction(alice, BigDecimal.valueOf(50.00), dateMonth1B, "Book store ($50 = 0 pts)"),
                // Month 0 (Current month)
                new Transaction(alice, BigDecimal.valueOf(200.00), dateMonth0A, "Department store ($200 = 250 pts)"),
                new Transaction(alice, BigDecimal.valueOf(45.00), dateMonth0B, "Coffee shop ($45 = 0 pts)"),

                // Bob: Boundary conditions ($51, $75, $99, $101, $150)
                new Transaction(bob, BigDecimal.valueOf(51.00), dateMonth2A, "Grocery ($51 = 1 pt)"),
                new Transaction(bob, BigDecimal.valueOf(75.00), dateMonth2B, "Hardware store ($75 = 25 pts)"),
                new Transaction(bob, BigDecimal.valueOf(99.00), dateMonth1A, "Clothing ($99 = 49 pts)"),
                new Transaction(bob, BigDecimal.valueOf(101.00), dateMonth1B, "Apparel ($101 = 52 pts)"),
                new Transaction(bob, BigDecimal.valueOf(150.00), dateMonth0A, "Sporting goods ($150 = 150 pts)"),

                // Charlie: Single month activity
                new Transaction(charlie, BigDecimal.valueOf(80.00), dateMonth1A, "Garden supplies ($80 = 30 pts)"),

                // Diana: Has historical transaction older than 3 months plus a current transaction.
                // Proves that transactions older than 3 months are excluded from the 3-month reward calculation.
                new Transaction(diana, BigDecimal.valueOf(300.00), dateOld, "Historical purchase older than 3 months ($300 - should be excluded)"),
                new Transaction(diana, BigDecimal.valueOf(110.00), dateMonth0A, "Recent purchase in current month ($110 = 70 pts)")
        );

        transactionRepository.saveAll(transactions);
        log.info("Sample dataset loaded successfully with {} customers and {} transactions.",
                customerRepository.count(), transactionRepository.count());
    }
}
