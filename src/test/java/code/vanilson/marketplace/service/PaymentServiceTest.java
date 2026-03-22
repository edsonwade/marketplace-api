package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.PaymentDto;
import code.vanilson.marketplace.dto.PaymentMethodDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.*;
import code.vanilson.marketplace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository       paymentRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private OrderRepository         orderRepository;
    @Mock private StockRepository         stockRepository;
    @Mock private ProductRepository       productRepository;
    @Mock private CustomerRepository      customerRepository;
    @Mock private EmailService            emailService;

    private PaymentService paymentService;

    private Payment       payment;
    private PaymentDto    paymentDto;
    private PaymentMethod paymentMethod;
    private PaymentMethodDto paymentMethodDto;
    private Order         order;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, paymentMethodRepository, orderRepository,
                stockRepository, productRepository, customerRepository, emailService);

        payment = new Payment(1L, 1L, "CREDIT_CARD", BigDecimal.valueOf(100));
        payment.setPaymentId(1L);
        payment.setPaymentStatus("COMPLETED");
        payment.setTransactionId("txn-123");
        payment.setCurrency("USD");

        paymentDto = new PaymentDto(1L, 1L, 1L, "CREDIT_CARD", BigDecimal.valueOf(100));

        paymentMethod = new PaymentMethod(1L, "CREDIT_CARD", "Visa", null, "1234");
        paymentMethod.setPaymentMethodId(1L);
        paymentMethod.setIsDefault(false);
        paymentMethod.setIsActive(true);

        paymentMethodDto = new PaymentMethodDto(1L, 1L, "CREDIT_CARD", "Visa", "1234");

        // Build a minimal Order with one OrderItem so processPayment can calculate amount
        Product product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(9.99));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(1L);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);

        order = new Order();
        order.setOrderId(1L);
        Set<OrderItem> items = new HashSet<>();
        items.add(orderItem);
        order.setOrderItems(items);
    }

    // ── findAll / findById ────────────────────────────────────────────────────

    @Test
    void testFindAllPaymentsReturnsList() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        List<PaymentDto> result = paymentService.findAllPayments();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
    }

    @Test
    void testFindPaymentByIdReturnsPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        Optional<PaymentDto> result = paymentService.findPaymentById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentId()).isEqualTo(1L);
    }

    @Test
    void testFindPaymentByIdReturnsEmpty() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThat(paymentService.findPaymentById(999L)).isEmpty();
    }

    @Test
    void testFindPaymentsByOrderIdReturnsList() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(payment));
        List<PaymentDto> result = paymentService.findPaymentsByOrderId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void testFindPaymentsByCustomerIdReturnsList() {
        when(paymentRepository.findByCustomerId(1L)).thenReturn(List.of(payment));
        List<PaymentDto> result = paymentService.findPaymentsByCustomerId(1L);
        assertThat(result).hasSize(1);
    }

    // ── createPayment ─────────────────────────────────────────────────────────

    @Test
    void testCreatePaymentReturnsCompleted() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        PaymentDto result = paymentService.createPayment(paymentDto);
        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo("COMPLETED");
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    // ── processPayment ────────────────────────────────────────────────────────

    @Test
    void testProcessPaymentReturnsCompleted() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        when(stockRepository.findByProductProductId(any())).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        PaymentDto result = paymentService.processPayment(1L, 1L, "CREDIT_CARD", null);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo("COMPLETED");
        verify(orderRepository).findById(1L);
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testProcessPaymentReducesStockOnCompletion() {
        // Product with 10 in stock, order has 2 items
        Product p = new Product();
        p.setProductId(1L);
        p.setQuantity(10);
        p.setPrice(BigDecimal.valueOf(9.99));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(productRepository.save(any(Product.class))).thenReturn(p);
        when(stockRepository.findByProductProductId(1L)).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        paymentService.processPayment(1L, 1L, "CREDIT_CARD", null);

        // Product quantity should be reduced from 10 by 2 = 8
        verify(productRepository).save(argThat(prod -> prod.getQuantity() == 8));
    }

    @Test
    void testProcessPaymentSendsEmailOnCompletion() {
        Customer customer = new Customer(1L, "John", "john@example.com", "123 St");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        when(stockRepository.findByProductProductId(any())).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        paymentService.processPayment(1L, 1L, "CREDIT_CARD", null);

        verify(emailService).sendPaymentConfirmation(
                eq("john@example.com"), any(), any());
    }

    @Test
    void testProcessPaymentThrowsWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(999L, 1L, "CREDIT_CARD", null))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void testProcessPaymentCalculatesAmountFromOrderItems() {
        // 2 items × 9.99 = 19.98
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            if (p.getAmount() != null) {
                assertThat(p.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(19.98));
            }
            p.setPaymentId(1L);
            p.setPaymentStatus("COMPLETED");
            return p;
        });

        PaymentDto result = paymentService.processPayment(1L, 1L, "DEBIT_CARD", null);
        assertThat(result.getPaymentStatus()).isEqualTo("COMPLETED");
    }

    // ── updatePaymentStatus ───────────────────────────────────────────────────

    @Test
    void testUpdatePaymentStatusReturnsUpdated() {
        Payment updated = new Payment(1L, 1L, "CREDIT_CARD", BigDecimal.valueOf(100));
        updated.setPaymentId(1L);
        updated.setPaymentStatus("REFUNDED");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updated);

        PaymentDto result = paymentService.updatePaymentStatus(1L, "REFUNDED");
        assertThat(result).isNotNull();
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatusThrowsWhenNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentService.updatePaymentStatus(999L, "REFUNDED"))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment not found");
        verify(paymentRepository, never()).save(any());
    }

    // ── deletePayment ─────────────────────────────────────────────────────────

    @Test
    void testDeletePaymentReturnsTrue() {
        when(paymentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(1L);
        assertThat(paymentService.deletePayment(1L)).isTrue();
    }

    @Test
    void testDeletePaymentThrowsWhenNotFound() {
        when(paymentRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> paymentService.deletePayment(999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment not found");
        verify(paymentRepository, never()).deleteById(any());
    }

    // ── paymentMethods ────────────────────────────────────────────────────────

    @Test
    void testFindPaymentMethodsByCustomerIdReturnsList() {
        when(paymentMethodRepository.findByCustomerIdAndIsActiveTrue(1L)).thenReturn(List.of(paymentMethod));
        List<PaymentMethodDto> result = paymentService.findPaymentMethodsByCustomerId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void testFindPaymentMethodByIdReturnsMethod() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        Optional<PaymentMethodDto> result = paymentService.findPaymentMethodById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentMethodId()).isEqualTo(1L);
    }

    @Test
    void testAddPaymentMethodReturnsAdded() {
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(paymentMethod);
        assertThat(paymentService.addPaymentMethod(paymentMethodDto)).isNotNull();
    }

    @Test
    void testAddPaymentMethodUnsetsExistingDefault() {
        PaymentMethod existingDefault = new PaymentMethod();
        existingDefault.setPaymentMethodId(99L);
        existingDefault.setCustomerId(1L);
        existingDefault.setIsDefault(true);

        when(paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(1L)).thenReturn(Optional.of(existingDefault));
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(paymentMethod);

        paymentMethodDto.setIsDefault(true);
        assertThat(paymentService.addPaymentMethod(paymentMethodDto)).isNotNull();
        verify(paymentMethodRepository, times(2)).save(any(PaymentMethod.class));
    }

    @Test
    void testSetDefaultPaymentMethodReturnsUpdated() {
        PaymentMethod updated = new PaymentMethod();
        updated.setPaymentMethodId(1L);
        updated.setCustomerId(1L);
        updated.setIsDefault(true);

        when(paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(1L)).thenReturn(Optional.empty());
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(updated);

        PaymentMethodDto result = paymentService.setDefaultPaymentMethod(1L, 1L);
        assertThat(result).isNotNull();
    }

    @Test
    void testSetDefaultPaymentMethodThrowsWhenNotFound() {
        when(paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(1L)).thenReturn(Optional.empty());
        when(paymentMethodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.setDefaultPaymentMethod(1L, 999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment method not found");
        verify(paymentMethodRepository, never()).save(any());
    }

    @Test
    void testDeletePaymentMethodReturnsTrue() {
        when(paymentMethodRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentMethodRepository).deleteById(1L);
        assertThat(paymentService.deletePaymentMethod(1L)).isTrue();
    }

    @Test
    void testDeletePaymentMethodThrowsWhenNotFound() {
        when(paymentMethodRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> paymentService.deletePaymentMethod(999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment method not found");
        verify(paymentMethodRepository, never()).deleteById(any());
    }
}
