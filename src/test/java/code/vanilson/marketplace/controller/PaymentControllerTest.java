package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.PaymentDto;
import code.vanilson.marketplace.dto.PaymentMethodDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;
    private PaymentDto paymentDto;
    private PaymentMethodDto paymentMethodDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();

        paymentDto = new PaymentDto(1L, 1L, 1L, "CREDIT_CARD", BigDecimal.valueOf(100));
        paymentDto.setPaymentStatus("COMPLETED");

        paymentMethodDto = new PaymentMethodDto(1L, 1L, "CREDIT_CARD", "Visa", "1234");
    }

    @Test
    void testGetAllPaymentsReturnsOk() throws Exception {
        when(paymentService.findAllPayments()).thenReturn(List.of(paymentDto));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(paymentService, times(1)).findAllPayments();
    }

    @Test
    void testGetPaymentByIdReturnsPayment() throws Exception {
        when(paymentService.findPaymentById(1L)).thenReturn(Optional.of(paymentDto));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).findPaymentById(1L);
    }

    @Test
    void testGetPaymentByIdReturnsNotFound() throws Exception {
        when(paymentService.findPaymentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).findPaymentById(999L);
    }

    @Test
    void testGetPaymentsByOrderIdReturnsOk() throws Exception {
        when(paymentService.findPaymentsByOrderId(1L)).thenReturn(List.of(paymentDto));

        mockMvc.perform(get("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(paymentService, times(1)).findPaymentsByOrderId(1L);
    }

    @Test
    void testGetPaymentsByCustomerIdReturnsOk() throws Exception {
        when(paymentService.findPaymentsByCustomerId(1L)).thenReturn(List.of(paymentDto));

        mockMvc.perform(get("/api/payments/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(paymentService, times(1)).findPaymentsByCustomerId(1L);
    }

    @Test
    void testCreatePaymentReturnsCreated() throws Exception {
        when(paymentService.createPayment(any(PaymentDto.class))).thenReturn(paymentDto);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).createPayment(any(PaymentDto.class));
    }

    @Test
    void testProcessPaymentReturnsOk() throws Exception {
        when(paymentService.processPayment(anyLong(), anyLong(), any(), any())).thenReturn(paymentDto);

        mockMvc.perform(post("/api/payments/process")
                        .param("orderId", "1")
                        .param("customerId", "1")
                        .param("paymentMethod", "CREDIT_CARD")
                        .param("token", "token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).processPayment(1L, 1L, "CREDIT_CARD", "token123");
    }

    @Test
    void testUpdatePaymentStatusReturnsOk() throws Exception {
        when(paymentService.updatePaymentStatus(1L, "REFUNDED")).thenReturn(paymentDto);

        mockMvc.perform(patch("/api/payments/1/status")
                        .param("status", "REFUNDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).updatePaymentStatus(1L, "REFUNDED");
    }

    @Test
    void testDeletePaymentReturnsOk() throws Exception {
        when(paymentService.deletePayment(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/payments/1"))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).deletePayment(1L);
    }

    @Test
    void testDeletePaymentReturnsNotFound() throws Exception {
        when(paymentService.deletePayment(999L)).thenThrow(new ObjectWithIdNotFound("Payment not found"));

        mockMvc.perform(delete("/api/payments/999"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).deletePayment(999L);
    }

    @Test
    void testGetPaymentMethodsByCustomerIdReturnsOk() throws Exception {
        when(paymentService.findPaymentMethodsByCustomerId(1L)).thenReturn(List.of(paymentMethodDto));

        mockMvc.perform(get("/api/payments/methods/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(paymentService, times(1)).findPaymentMethodsByCustomerId(1L);
    }

    @Test
    void testGetPaymentMethodByIdReturnsPaymentMethod() throws Exception {
        when(paymentService.findPaymentMethodById(1L)).thenReturn(Optional.of(paymentMethodDto));

        mockMvc.perform(get("/api/payments/methods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).findPaymentMethodById(1L);
    }

    @Test
    void testGetPaymentMethodByIdReturnsNotFound() throws Exception {
        when(paymentService.findPaymentMethodById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/methods/999"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).findPaymentMethodById(999L);
    }

    @Test
    void testAddPaymentMethodReturnsCreated() throws Exception {
        when(paymentService.addPaymentMethod(any(PaymentMethodDto.class))).thenReturn(paymentMethodDto);

        mockMvc.perform(post("/api/payments/methods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentMethodDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).addPaymentMethod(any(PaymentMethodDto.class));
    }

    @Test
    void testSetDefaultPaymentMethodReturnsOk() throws Exception {
        when(paymentService.setDefaultPaymentMethod(1L, 1L)).thenReturn(paymentMethodDto);

        mockMvc.perform(put("/api/payments/methods/1/default")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(paymentService, times(1)).setDefaultPaymentMethod(1L, 1L);
    }

    @Test
    void testDeletePaymentMethodReturnsOk() throws Exception {
        when(paymentService.deletePaymentMethod(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/payments/methods/1"))
                .andExpect(status().isOk());

        verify(paymentService, times(1)).deletePaymentMethod(1L);
    }

    @Test
    void testDeletePaymentMethodReturnsNotFound() throws Exception {
        when(paymentService.deletePaymentMethod(999L)).thenThrow(new ObjectWithIdNotFound("Payment method not found"));

        mockMvc.perform(delete("/api/payments/methods/999"))
                .andExpect(status().isNotFound());

        verify(paymentService, times(1)).deletePaymentMethod(999L);
    }
}
