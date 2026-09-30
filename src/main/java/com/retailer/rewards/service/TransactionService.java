package com.retailer.rewards.service;

import com.retailer.rewards.dto.TransactionRequest;
import com.retailer.rewards.dto.TransactionResponse;
import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing customer purchase transactions.
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final RewardCalculationService rewardCalculationService;

    public TransactionService(TransactionRepository transactionRepository,
                              CustomerRepository customerRepository,
                              RewardCalculationService rewardCalculationService) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.rewardCalculationService = rewardCalculationService;
    }

    /**
     * Retrieves transactions with optional customer and date range filtering.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date (" + startDate + ") cannot be after end date (" + endDate + ")");
        }

        List<Transaction> transactions;

        if (customerId != null && startDate != null && endDate != null) {
            if (!customerRepository.existsById(customerId)) {
                throw new CustomerNotFoundException(customerId);
            }
            transactions = transactionRepository.findByCustomerIdAndTransactionDateBetween(customerId, startDate, endDate);
        } else if (customerId != null) {
            if (!customerRepository.existsById(customerId)) {
                throw new CustomerNotFoundException(customerId);
            }
            transactions = transactionRepository.findByCustomerId(customerId);
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByTransactionDateBetween(startDate, endDate);
        } else {
            transactions = transactionRepository.findAllWithCustomer();
        }

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Records a new transaction and calculates reward points.
     */
    public TransactionResponse recordTransaction(TransactionRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        Transaction transaction = new Transaction(
                customer,
                request.getAmount(),
                request.getTransactionDate(),
                request.getDescription()
        );

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        long points = rewardCalculationService.calculatePoints(transaction.getAmount());
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCustomer().getId(),
                transaction.getCustomer().getName(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                points
        );
    }
}
