package com.retailer.rewards.controller;

import com.retailer.rewards.dto.TransactionRequest;
import com.retailer.rewards.dto.TransactionResponse;
import com.retailer.rewards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for customer purchase transactions.
 * Supports both /api/transactions and /transactions endpoint paths.
 */
@RestController
@RequestMapping({"/api/transactions", "/transactions"})
@Tag(name = "Transactions", description = "Endpoints for viewing and recording customer purchase transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Retrieves transactions with optional customer and date range filters.
     */
    @GetMapping
    @Operation(
            summary = "Get transactions",
            description = "Retrieves recorded purchase transactions. Can be filtered by customerId and/or date range."
    )
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @Parameter(description = "Optional filter by customer ID")
            @RequestParam(required = false) Long customerId,
            @Parameter(description = "Optional start date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Optional end date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionResponse> transactions = transactionService.getTransactions(customerId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Records a new transaction and returns details with earned points.
     */
    @PostMapping
    @Operation(
            summary = "Record a new purchase transaction",
            description = "Creates a new transaction for a customer and returns the calculated reward points earned."
    )
    public ResponseEntity<TransactionResponse> recordTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.recordTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
