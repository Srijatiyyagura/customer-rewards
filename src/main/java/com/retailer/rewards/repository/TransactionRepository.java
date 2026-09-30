package com.retailer.rewards.repository;

import com.retailer.rewards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Transaction persistence and query operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t JOIN FETCH t.customer WHERE t.customer.id = :customerId ORDER BY t.transactionDate DESC")
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.customer WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate ASC")
    List<Transaction> findByTransactionDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.customer WHERE t.customer.id = :customerId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate ASC")
    List<Transaction> findByCustomerIdAndTransactionDateBetween(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.customer ORDER BY t.transactionDate DESC")
    List<Transaction> findAllWithCustomer();
}
