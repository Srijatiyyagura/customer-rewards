package com.retailer.rewards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO representing transaction details and the reward points earned for it.
 */
public class TransactionResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private long pointsEarned;

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, Long customerId, String customerName, BigDecimal amount,
                               LocalDate transactionDate, String description, long pointsEarned) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.pointsEarned = pointsEarned;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(long pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
}
