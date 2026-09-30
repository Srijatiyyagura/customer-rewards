package com.retailer.rewards.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO representing the rewards calculation for a customer across months and overall total.
 */
public class CustomerRewardResponse {

    private Long customerId;
    private String customerName;
    private String email;
    private List<MonthlyRewardResponse> monthlyRewards = new ArrayList<>();
    private long totalPoints;
    private BigDecimal totalSpent;

    public CustomerRewardResponse() {
    }

    public CustomerRewardResponse(Long customerId, String customerName, String email,
                                  List<MonthlyRewardResponse> monthlyRewards,
                                  long totalPoints, BigDecimal totalSpent) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.email = email;
        this.monthlyRewards = monthlyRewards != null ? monthlyRewards : new ArrayList<>();
        this.totalPoints = totalPoints;
        this.totalSpent = totalSpent;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<MonthlyRewardResponse> getMonthlyRewards() {
        return monthlyRewards;
    }

    public void setMonthlyRewards(List<MonthlyRewardResponse> monthlyRewards) {
        this.monthlyRewards = monthlyRewards != null ? monthlyRewards : new ArrayList<>();
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
}
