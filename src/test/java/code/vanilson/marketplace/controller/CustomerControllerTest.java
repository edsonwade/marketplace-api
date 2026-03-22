package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.repository.CustomerRepository;
import code.vanilson.marketplace.service.CustomerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerServiceImpl customerService;

    @Mock
    private CustomerRepository customerRepository;

    // Build manually — constructor now requires both CustomerService and CustomerRepository
    private CustomerController customerController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        customerController = new CustomerController(customerService, customerRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                // Required so @AuthenticationPrincipal resolves from SecurityContextHolder
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        // Always clear security context after each test
        SecurityContextHolder.clearContext();
    }

    /** Helper: puts a UserDetails into the Spring Security context so that
     *  @AuthenticationPrincipal resolves correctly in standalone MockMvc. */
    private void authenticateAs(String email) {
        User principal = new User(
                email, "irrelevant",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    // ── existing tests ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Get All Customers")
    void testGetCustomers() throws Exception {
        when(customerService.findAllCustomers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Customer by ID")
    void testGetCustomerById() throws Exception {
        Long customerId = 1L;
        CustomerDto customer = new CustomerDto(customerId, "John Doe", "john@example.com", "Address 1");
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
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
        when(customerService.saveCustomer(any(CustomerDto.class))).thenReturn(newCustomer);
        mockMvc.perform(post("/api/v1/customers")
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
        when(customerService.updateCustomer(eq(customerId), any(CustomerDto.class))).thenReturn(updatedCustomer);
        mockMvc.perform(put("/api/v1/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"email\":\"updated@example.com\",\"address\":\"Updated Address\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Delete Customer")
    void testDeleteCustomer() throws Exception {
        Long customerId = 1L;
        CustomerDto customer = new CustomerDto(customerId, "John Doe", "john@example.com", "Address 1");
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        when(customerService.deleteCustomer(customerId)).thenReturn(true);
        mockMvc.perform(delete("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete Customer - Not Found")
    void testDeleteCustomerNotFound() throws Exception {
        Long customerId = 1L;
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/v1/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Customer - Internal Server Error")
    void testDeleteCustomerInternalServerError() throws Exception {
        Long customerId = 1L;
        CustomerDto customer = new CustomerDto(customerId, "John Doe", "john@example.com", "Address 1");
        when(customerService.findCustomerById(customerId)).thenReturn(Optional.of(customer));
        when(customerService.deleteCustomer(customerId)).thenReturn(false);
        mockMvc.perform(delete("/api/v1/customers/{id}", customerId))
                .andExpect(status().isInternalServerError());
    }

    // ── new tests for GET /me ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /me - returns 401 when not authenticated")
    void testGetCurrentCustomerReturns401WhenNotAuthenticated() throws Exception {
        // SecurityContext is empty → principal is null → controller returns 401
        mockMvc.perform(get("/api/v1/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /me - returns customer for authenticated user")
    void testGetCurrentCustomerReturnsCustomer() throws Exception {
        // Populate SecurityContext so @AuthenticationPrincipal resolves to 'john@example.com'
        authenticateAs("john@example.com");

        Customer customer = new Customer(1L, "John Doe", "john@example.com", "Address 1");
        when(customerRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/v1/customers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /me - auto-creates customer when email has no record")
    void testGetCurrentCustomerAutoCreates() throws Exception {
        authenticateAs("newuser@example.com");

        Customer created = new Customer(99L, "newuser", "newuser@example.com", "");

        // First call returns empty (no customer yet), save returns the new one
        when(customerRepository.findByEmail("newuser@example.com"))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(created);

        mockMvc.perform(get("/api/v1/customers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(99))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        verify(customerRepository).save(any(Customer.class));
    }
}
