package com.retailer.rewards.dto;

import java.math.BigDecimal;

/**
 * DTO representing earned reward points and summary for a single month.
 * Designed as a structured object to facilitate easy UI rendering.
 */
public class MonthlyRewardResponse {

    private int year;
    private String month;
    private int monthNumber;
    private long points;
    private BigDecimal totalSpent;
    private int transactionCount;

    public MonthlyRewardResponse() {
    }

    public MonthlyRewardResponse(int year, String month, int monthNumber, long points, BigDecimal totalSpent, int transactionCount) {
        this.year = year;
        this.month = month;
        this.monthNumber = monthNumber;
        this.points = points;
        this.totalSpent = totalSpent;
        this.transactionCount = transactionCount;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    public void setMonthNumber(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}
