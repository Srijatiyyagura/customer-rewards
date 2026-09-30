package com.retailer.rewards.service;

import com.retailer.rewards.dto.CustomerRequest;
import com.retailer.rewards.dto.CustomerResponse;
import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    @DisplayName("Should create customer when email is unique")
    void shouldCreateCustomer() {
        CustomerRequest request = new CustomerRequest("Alice", "alice@example.com");
        Customer saved = new Customer(1L, "Alice", "alice@example.com");

        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse response = customerService.createCustomer(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating customer with duplicate email")
    void shouldThrowException_WhenEmailAlreadyExists() {
        CustomerRequest request = new CustomerRequest("Alice", "alice@example.com");

        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should return customer by ID")
    void shouldGetCustomerById() {
        Customer customer = new Customer(1L, "Alice", "alice@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer ID not found")
    void shouldThrowCustomerNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("Should return all customers")
    void shouldGetAllCustomers() {
        when(customerRepository.findAll()).thenReturn(Arrays.asList(
                new Customer(1L, "Alice", "alice@example.com"),
                new Customer(2L, "Bob", "bob@example.com")
        ));

        List<CustomerResponse> customers = customerService.getAllCustomers();

        assertThat(customers).hasSize(2);
    }
}
