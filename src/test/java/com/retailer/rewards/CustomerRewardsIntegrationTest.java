package com.retailer.rewards;

import com.retailer.rewards.dto.CustomerRewardResponse;
import com.retailer.rewards.dto.TransactionRequest;
import com.retailer.rewards.dto.TransactionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Customer Rewards API End-to-End Integration Tests")
class CustomerRewardsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET /api/rewards - should calculate 3-month rewards for all seeded customers")
    void shouldRetrieveRewardsForAllCustomers() {
        ResponseEntity<List<CustomerRewardResponse>> response = restTemplate.exchange(
                "/api/rewards",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<CustomerRewardResponse> rewards = response.getBody();
        assertThat(rewards).isNotNull().isNotEmpty();

        // Find Alice Johnson
        CustomerRewardResponse alice = rewards.stream()
                .filter(r -> "Alice Johnson".equals(r.getCustomerName()))
                .findFirst()
                .orElse(null);

        assertThat(alice).isNotNull();
        // Alice seeded:
        // Month 2: $120 -> 90 pts
        // Month 1: $100 -> 50 pts, $50 -> 0 pts
        // Month 0: $200 -> 250 pts, $45 -> 0 pts
        // Total points across 3 months = 90 + 50 + 250 = 390
        assertThat(alice.getTotalPoints()).isEqualTo(390L);
        assertThat(alice.getMonthlyRewards()).hasSize(3);

        // Verify Diana Prince: Historical $300 transaction (4 months ago) must NOT be included in default 3-month reward
        CustomerRewardResponse diana = rewards.stream()
                .filter(r -> "Diana Prince".equals(r.getCustomerName()))
                .findFirst()
                .orElse(null);

        assertThat(diana).isNotNull();
        // Diana only has 1 transaction in the 3-month period ($110 -> 70 pts)
        // Her 4-month-old transaction ($300 -> 450 pts) should be excluded!
        assertThat(diana.getTotalPoints()).isEqualTo(70L);
        assertThat(diana.getMonthlyRewards()).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/rewards/{id} - should return 3-month rewards for specific customer")
    void shouldRetrieveRewardsForSingleCustomer() {
        ResponseEntity<CustomerRewardResponse> response = restTemplate.getForEntity(
                "/api/rewards/1",
                CustomerRewardResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CustomerRewardResponse alice = response.getBody();
        assertThat(alice).isNotNull();
        assertThat(alice.getCustomerId()).isEqualTo(1L);
        assertThat(alice.getTotalPoints()).isEqualTo(390L);
    }

    @Test
    @DisplayName("POST /api/transactions - should create transaction and update customer rewards")
    void shouldCreateTransactionAndReflectInRewards() {
        // Record a new purchase of $120.00 for Bob (id = 2) in current month
        TransactionRequest request = new TransactionRequest(
                2L,
                BigDecimal.valueOf(120.00),
                LocalDate.now(),
                "Integration Test Purchase"
        );

        ResponseEntity<TransactionResponse> txResponse = restTemplate.postForEntity(
                "/api/transactions",
                request,
                TransactionResponse.class
        );

        assertThat(txResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TransactionResponse tx = txResponse.getBody();
        assertThat(tx).isNotNull();
        // $120 purchase earns 90 points (2x$20 + 1x$50)
        assertThat(tx.getPointsEarned()).isEqualTo(90L);
        assertThat(tx.getCustomerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("GET /api/rewards/999 - should return 404 for unknown customer")
    void shouldReturn404ForUnknownCustomer() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/rewards/999",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Customer with ID 999 was not found.");
    }
}
