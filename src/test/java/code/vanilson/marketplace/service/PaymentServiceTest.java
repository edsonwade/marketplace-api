package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.PaymentDto;
import code.vanilson.marketplace.dto.PaymentMethodDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.PaymentMapper;
import code.vanilson.marketplace.model.Payment;
import code.vanilson.marketplace.model.PaymentMethod;
import code.vanilson.marketplace.repository.PaymentMethodRepository;
import code.vanilson.marketplace.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private PaymentMethod paymentMethod;
    private PaymentMethodDto paymentMethodDto;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testFindAllPaymentsReturnsList() {
        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        List<PaymentDto> result = paymentService.findAllPayments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void testFindPaymentByIdReturnsPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<PaymentDto> result = paymentService.findPaymentById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getPaymentId()).isEqualTo(1L);
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void testFindPaymentByIdReturnsEmpty() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<PaymentDto> result = paymentService.findPaymentById(999L);

        assertThat(result).isEmpty();
        verify(paymentRepository, times(1)).findById(999L);
    }

    @Test
    void testFindPaymentsByOrderIdReturnsList() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(payment));

        List<PaymentDto> result = paymentService.findPaymentsByOrderId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(1L);
        verify(paymentRepository, times(1)).findByOrderId(1L);
    }

    @Test
    void testFindPaymentsByCustomerIdReturnsList() {
        when(paymentRepository.findByCustomerId(1L)).thenReturn(List.of(payment));

        List<PaymentDto> result = paymentService.findPaymentsByCustomerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
        verify(paymentRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void testCreatePaymentReturnsCreated() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.createPayment(paymentDto);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo("COMPLETED");
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testProcessPaymentReturnsCompleted() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDto result = paymentService.processPayment(1L, 1L, "CREDIT_CARD", "token123");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo("COMPLETED");
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatusReturnsUpdated() {
        PaymentDto updatedDto = new PaymentDto();
        updatedDto.setPaymentId(1L);
        updatedDto.setPaymentStatus("REFUNDED");
        
        Payment updatedPayment = new Payment(1L, 1L, "CREDIT_CARD", BigDecimal.valueOf(100));
        updatedPayment.setPaymentId(1L);
        updatedPayment.setPaymentStatus("REFUNDED");
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        PaymentDto result = paymentService.updatePaymentStatus(1L, "REFUNDED");

        assertThat(result).isNotNull();
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatusThrowsWhenNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updatePaymentStatus(999L, "REFUNDED"))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment not found");

        verify(paymentRepository, times(1)).findById(999L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testDeletePaymentReturnsTrue() {
        when(paymentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(1L);

        boolean result = paymentService.deletePayment(1L);

        assertThat(result).isTrue();
        verify(paymentRepository, times(1)).existsById(1L);
        verify(paymentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletePaymentThrowsWhenNotFound() {
        when(paymentRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.deletePayment(999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment not found");

        verify(paymentRepository, times(1)).existsById(999L);
        verify(paymentRepository, never()).deleteById(any());
    }

    @Test
    void testFindPaymentMethodsByCustomerIdReturnsList() {
        when(paymentMethodRepository.findByCustomerIdAndIsActiveTrue(1L)).thenReturn(List.of(paymentMethod));

        List<PaymentMethodDto> result = paymentService.findPaymentMethodsByCustomerId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
        verify(paymentMethodRepository, times(1)).findByCustomerIdAndIsActiveTrue(1L);
    }

    @Test
    void testFindPaymentMethodByIdReturnsPaymentMethod() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));

        Optional<PaymentMethodDto> result = paymentService.findPaymentMethodById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getPaymentMethodId()).isEqualTo(1L);
        verify(paymentMethodRepository, times(1)).findById(1L);
    }

    @Test
    void testAddPaymentMethodReturnsAdded() {
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(paymentMethod);

        PaymentMethodDto result = paymentService.addPaymentMethod(paymentMethodDto);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository, times(1)).save(any(PaymentMethod.class));
    }

    @Test
    void testAddPaymentMethodSetsNewAsDefaultWhenNoOtherDefault() {
        when(paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(1L)).thenReturn(Optional.empty());
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(paymentMethod);

        paymentMethodDto.setIsDefault(true);
        PaymentMethodDto result = paymentService.addPaymentMethod(paymentMethodDto);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository, times(1)).findByCustomerIdAndIsDefaultTrue(1L);
        verify(paymentMethodRepository, times(1)).save(any(PaymentMethod.class));
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
        PaymentMethodDto result = paymentService.addPaymentMethod(paymentMethodDto);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository, times(2)).save(any(PaymentMethod.class));
    }

    @Test
    void testSetDefaultPaymentMethodReturnsUpdated() {
        PaymentMethod updatedMethod = new PaymentMethod();
        updatedMethod.setPaymentMethodId(1L);
        updatedMethod.setCustomerId(1L);
        updatedMethod.setIsDefault(true);
        
        when(paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(1L)).thenReturn(Optional.empty());
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(updatedMethod);

        PaymentMethodDto result = paymentService.setDefaultPaymentMethod(1L, 1L);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository, times(1)).findById(1L);
        verify(paymentMethodRepository, times(1)).save(any(PaymentMethod.class));
    }

    @Test
    void testSetDefaultPaymentMethodThrowsWhenNotFound() {
        when(paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(1L)).thenReturn(Optional.empty());
        when(paymentMethodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.setDefaultPaymentMethod(1L, 999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment method not found");

        verify(paymentMethodRepository, times(1)).findById(999L);
        verify(paymentMethodRepository, never()).save(any());
    }

    @Test
    void testDeletePaymentMethodReturnsTrue() {
        when(paymentMethodRepository.existsById(1L)).thenReturn(true);
        doNothing().when(paymentMethodRepository).deleteById(1L);

        boolean result = paymentService.deletePaymentMethod(1L);

        assertThat(result).isTrue();
        verify(paymentMethodRepository, times(1)).existsById(1L);
        verify(paymentMethodRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletePaymentMethodThrowsWhenNotFound() {
        when(paymentMethodRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> paymentService.deletePaymentMethod(999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Payment method not found");

        verify(paymentMethodRepository, times(1)).existsById(999L);
        verify(paymentMethodRepository, never()).deleteById(any());
    }
}
