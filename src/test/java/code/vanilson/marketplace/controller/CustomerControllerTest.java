package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.config.JwtAuthenticationFilter;
import code.vanilson.marketplace.config.JwtService;
import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.service.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerServiceImpl customerService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    AuthenticationProvider authenticationProvider;
    @MockBean
    LogoutHandler logoutHandler;
    @MockBean
    JwtService jwtService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations
                .openMocks(this);
    }

    @Test
    @DisplayName("Get All Customers")
    void testGetCustomers() throws Exception {
        when(customerService.findAllCustomers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Customer by ID")
    void testGetCustomerById() throws Exception {
        Long customerId = 1L;
        CustomerDto customer = new CustomerDto(customerId, "John Doe", "john@example.com", "Address 1");

        // Mock the service to return a customer by ID
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.address").value("Address 1"));
    }

    @Test
    @DisplayName("Create Customer")
    void testCreateCustomer() throws Exception {
        CustomerDto newCustomer = new CustomerDto(1L, "test", "test@example.com", "test 1");

        // Mock the service to return the created customer
        when(customerService.saveCustomer(any(CustomerDto.class))).thenReturn(newCustomer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Customer\",\"email\":\"new@example.com\",\"address\":\"test 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.address").value("test 1"));
    }

    @Test
    @DisplayName("Update Customer")
    void testUpdateCustomer() throws Exception {
        Long customerId = 1L;
        CustomerDto updatedCustomer = new CustomerDto(customerId, "Updated Name", "updated@example.com", "Updated Address");

        // Mock the service to return the updated customer
        when(customerService.updateCustomer(eq(customerId), any(CustomerDto.class))).thenReturn(updatedCustomer);

        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\":\"Updated Name\",\"email\":\"updated@example.com\",\"address\":\"Updated Address\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Delete Customer")
    void testDeleteCustomer() throws Exception {
        Long customerId = 1L;
        when(customerService.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerDto(customerId, "John Doe", "john@example.com", "Address 1")));
        when(customerService.deleteCustomer(customerId)).thenReturn(true);

        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete Customer - Customer Not Found")
    void testDeleteCustomerCustomerNotFound() throws Exception {
        Long customerId = 1L;
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Customer - Internal Server Error")
    void testDeleteCustomerInternalServerError() throws Exception {
        Long customerId = 1L;

        when(customerService.findCustomerById(customerId))
                .thenReturn(Optional.of(new CustomerDto(customerId, "John Doe", "john@example.com", "Address 1")));
        when(customerService.deleteCustomer(customerId)).thenReturn(false);

        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isInternalServerError());
    }

}
