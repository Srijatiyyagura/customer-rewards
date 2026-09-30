package com.retailer.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.rewards.dto.CustomerRequest;
import com.retailer.rewards.dto.CustomerResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.service.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController MockMvc Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("GET /api/customers - should return list of customers")
    void shouldReturnAllCustomers() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(Arrays.asList(
                new CustomerResponse(1L, "Alice Johnson", "alice@example.com"),
                new CustomerResponse(2L, "Bob Smith", "bob@example.com")
        ));

        mockMvc.perform(get("/api/customers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alice Johnson")))
                .andExpect(jsonPath("$[1].name", is("Bob Smith")));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - should return customer by ID")
    void shouldReturnCustomerById() throws Exception {
        when(customerService.getCustomerById(1L))
                .thenReturn(new CustomerResponse(1L, "Alice Johnson", "alice@example.com"));

        mockMvc.perform(get("/api/customers/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alice Johnson")));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - should return 404 when not found")
    void shouldReturn404_WhenCustomerNotFound() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/api/customers/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("POST /api/customers - should create customer")
    void shouldCreateCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest("Charlie Brown", "charlie@example.com");
        CustomerResponse response = new CustomerResponse(3L, "Charlie Brown", "charlie@example.com");

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Charlie Brown")));
    }
}
