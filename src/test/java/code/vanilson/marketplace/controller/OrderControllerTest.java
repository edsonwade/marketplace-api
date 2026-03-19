package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.service.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;
    @Mock
    private OrderServiceImpl orderService;
    @InjectMocks
    private OrderController orderController;
    private ObjectMapper objectMapper;
    private Customer customer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        customer = new Customer(2L, "test", "test@test.test", "test 1");
    }

    @Test
    @DisplayName("Get All Orders")
    void testGetOrders() throws Exception {
        LocalDateTime dateTime = LocalDateTime.parse("2023-11-02T23:59:59.999");
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(1L);
        orderDto.setLocalDateTime(dateTime);
        when(orderService.findAllOrders()).thenReturn(List.of(orderDto));
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get All Orders - Empty List")
    void testGetOrdersSuccessEmptyList() throws Exception {
        when(orderService.findAllOrders()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Order by ID - Success")
    void testGetOrderByIdSuccess() throws Exception {
        Long orderId = 1L;
        OrderDto order = new OrderDto();
        order.setOrderId(orderId);
        order.setLocalDateTime(LocalDateTime.now());
        when(orderService.findOrderById(orderId)).thenReturn(Optional.of(order));
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Get Order by ID - Not Found")
    void testGetOrderByIdNotFound() throws Exception {
        Long orderId = 1L;
        when(orderService.findOrderById(orderId))
                .thenThrow(new ObjectWithIdNotFound(String.format("order with id %d not found", orderId)));
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create a new Order")
    void testCreateOrder() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setLocalDateTime(LocalDateTime.now());
        orderDto.setCustomer(new CustomerDto(1L, "Test", "test@test.com", "Address"));
        orderDto.setOrderItems(new ArrayList<>());
        when(orderService.saveOrder(any(OrderDto.class))).thenAnswer(invocation -> {
            OrderDto savedOrder = invocation.getArgument(0);
            savedOrder.setOrderId(1L);
            return savedOrder;
        });
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Delete Order - Order Not Found")
    void testDeleteOrderNotFound() throws Exception {
        Long orderId = 1L;
        when(orderService.findOrderById(orderId)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Order - Internal Server Error")
    void testDeleteOrderInternalServerError() throws Exception {
        Long orderId = 123L;
        OrderDto order = new OrderDto();
        order.setOrderId(orderId);
        when(orderService.findOrderById(orderId)).thenReturn(Optional.of(order));
        when(orderService.deleteOrderById(orderId)).thenReturn(false);
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isInternalServerError());
    }
}
