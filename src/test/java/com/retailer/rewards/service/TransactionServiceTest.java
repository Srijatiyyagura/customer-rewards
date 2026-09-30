package com.retailer.rewards.service;

import com.retailer.rewards.dto.TransactionRequest;
import com.retailer.rewards.dto.TransactionResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    private RewardCalculationService rewardCalculationService;
    private TransactionService transactionService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        rewardCalculationService = new RewardCalculationService();
        transactionService = new TransactionService(transactionRepository, customerRepository, rewardCalculationService);
        customer = new Customer(1L, "Alice Johnson", "alice@example.com");
    }

    @Test
    @DisplayName("Should successfully record transaction and return earned points")
    void shouldRecordTransactionAndCalculatePoints() {
        TransactionRequest request = new TransactionRequest(
                1L, BigDecimal.valueOf(120.00), LocalDate.of(2026, 9, 15), "Electronics");

        Transaction savedTransaction = new Transaction(
                10L, customer, BigDecimal.valueOf(120.00), LocalDate.of(2026, 9, 15), "Electronics");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.recordTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getCustomerName()).isEqualTo("Alice Johnson");
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(120.00));
        // $120 = 90 points
        assertThat(response.getPointsEarned()).isEqualTo(90L);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when recording transaction for nonexistent customer")
    void shouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        TransactionRequest request = new TransactionRequest(
                99L, BigDecimal.valueOf(100.00), LocalDate.now(), "Purchase");

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.recordTransaction(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should retrieve transactions for a specific customer")
    void shouldGetTransactions_ByCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findByCustomerId(1L)).thenReturn(Collections.emptyList());

        List<TransactionResponse> result = transactionService.getTransactions(1L, null, null);

        assertThat(result).isNotNull();
        verify(transactionRepository).findByCustomerId(1L);
    }

    @Test
    @DisplayName("Should retrieve transactions filtered by customer and date range")
    void shouldGetTransactions_ByCustomerAndDateRange() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(1L, start, end))
                .thenReturn(Collections.emptyList());

        List<TransactionResponse> result = transactionService.getTransactions(1L, start, end);

        assertThat(result).isNotNull();
        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(1L, start, end);
    }

    @Test
    @DisplayName("Should throw InvalidDateRangeException when startDate is after endDate")
    void shouldThrowInvalidDateRangeException_WhenStartDateIsAfterEndDate() {
        LocalDate start = LocalDate.of(2026, 9, 30);
        LocalDate end = LocalDate.of(2026, 7, 1);

        assertThatThrownBy(() -> transactionService.getTransactions(1L, start, end))
                .isInstanceOf(InvalidDateRangeException.class);
    }
}
