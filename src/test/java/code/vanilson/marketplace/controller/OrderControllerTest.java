package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.config.JwtAuthenticationFilter;
import code.vanilson.marketplace.config.JwtService;
import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.CustomerMapper;
import code.vanilson.marketplace.mapper.OrderMapper;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.model.Order;
import code.vanilson.marketplace.service.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceImpl orderService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    AuthenticationProvider authenticationProvider;
    @MockBean
    LogoutHandler logoutHandler;
    @MockBean
    JwtService jwtService;
    /**
     * LocalDateTime for testing purposes.
     */
    static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse("2023-11-02T23:59:59.999");
    /**
     * Customer object used for testing.
     */
    Customer customer;

    @BeforeEach
    void setUp() {

        MockitoAnnotations
                .openMocks(this);
        customer = new Customer(2L, "test", "test@test.test", "test 1");
    }

    /**
     * Test to verify getting all orders.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Get All Orders")
    void testGetOrders() throws Exception {
        OrderDto orderDto = OrderMapper.toOrderDto(new Order(1L, LOCAL_DATE_TIME, customer, new HashSet<>()));
        when(orderService.findAllOrders()).thenReturn(List.of(orderDto));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].customer.customerId").value(2))
                .andExpect(jsonPath("$[0].customer.name").value("test"))
                .andExpect(jsonPath("$[0].customer.email").value("test@test.test"))
                .andExpect(jsonPath("$[0].customer.address").value("test 1"))
                .andExpect(jsonPath("$[0].localDateTime").value(LOCAL_DATE_TIME.toString()));

        verify(orderService, times(1)).findAllOrders();
    }

    /**
     * Test to verify getting all orders when the list is empty.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Get All Orders")
    void testGetOrdersSuccessEmptyList() throws Exception {
        when(orderService.findAllOrders()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test to verify getting an order by ID when the order exists.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Get Order by ID - Success")
    void testGetOrderByIdSuccess() throws Exception {
        Long orderId = 1L;
        OrderDto order = OrderMapper.toOrderDto(new Order(1L, LOCAL_DATE_TIME, customer, new HashSet<>()));
        // Mocking the behavior of orderService.findOrderById() to return the order
        when(orderService.findOrderById(orderId)).thenReturn(Optional.of(order));
        // Performing a GET request to retrieve the order by ID
        mockMvc.perform(get("/api/orders/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("orderId").value(1))
                .andExpect(jsonPath("customer.customerId").value(2))
                .andExpect(jsonPath("customer.name").value("test"))
                .andExpect(jsonPath("customer.email").value("test@test.test"))
                .andExpect(jsonPath("customer.address").value("test 1"))
                .andExpect(jsonPath("localDateTime").value(LOCAL_DATE_TIME.toString()));
        // Verifying that orderService.findOrderById() is called with the correct ID
        verify(orderService, times(1)).findOrderById(orderId);

    }

    /**
     * Test to verify getting an order by ID when the order does not exist.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Get Order by ID - Not Found")
    void testGetOrderByIdNotFound() throws Exception {
        Long orderId = 1L;
        when(orderService.findOrderById(orderId))
                .thenThrow(new ObjectWithIdNotFound(String.format("order with id %d not found", orderId)));
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to verify creating a new order.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Create a new Order")
    void testCreateOrder() throws Exception {
        // Create a sample Order object
        OrderDto orderDto = OrderMapper.toOrderDto(new Order(LocalDateTime.now(), customer, new HashSet<>()));

        // Mock the service response when saving the order
        when(orderService.saveOrder(any(OrderDto.class))).thenAnswer(invocation -> {
            OrderDto savedOrder = invocation.getArgument(0);
            savedOrder.setOrderId(1L); // Simulate saving and setting the orderId
            return savedOrder;
        });

        // Perform the POST request
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(orderDto)))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // Expect JSON response
                .andExpect(jsonPath("$.orderId").exists()); // Expect orderId field in the response JSON
    }


    /**
     * Test to verify deleting an order when the order is not found.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Delete Order - Order Not Found")
    void testDeleteOrderNotFound() throws Exception {
        Long orderId = 1L;
        // Mocking the behavior of orderService.findOrderById() to return an empty Optional
        when(orderService.findOrderById(orderId)).thenReturn(Optional.empty());
        // Performing a DELETE request to delete the order
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to verify deleting an order when an internal server error occurs.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Delete Order - Internal Server Error")
    void testDeleteOrderInternalServerError() throws Exception {
        OrderDto order = OrderDto.builder().orderId(123L).build();
        Long orderId = 123L;
        when(orderService.findOrderById(orderId)).thenReturn(Optional.of(order));
        when(orderService.deleteOrderById(orderId)).thenReturn(false);

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isInternalServerError()); // Expect 500 Internal Server Error
    }

    /**
     * Utility method to convert an object to JSON string
     */
    static String asJsonString(final Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}



