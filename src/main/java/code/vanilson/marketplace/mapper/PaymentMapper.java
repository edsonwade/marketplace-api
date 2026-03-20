package code.vanilson.marketplace.mapper;

import code.vanilson.marketplace.dto.PaymentDto;
import code.vanilson.marketplace.dto.PaymentMethodDto;
import code.vanilson.marketplace.model.Payment;
import code.vanilson.marketplace.model.PaymentMethod;

import java.util.List;
import java.util.stream.Collectors;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentDto(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionId(),
                payment.getCreatedAt(),
                payment.getUpdatedAt(),
                payment.getVersion()
        );
    }

    public static List<PaymentDto> toPaymentDtoList(List<Payment> payments) {
        return payments.stream()
                .map(PaymentMapper::toPaymentDto)
                .collect(Collectors.toList());
    }

    public static Payment toPayment(PaymentDto paymentDto) {
        if (paymentDto == null) {
            return null;
        }

        Payment payment = new Payment();
        payment.setPaymentId(paymentDto.getPaymentId());
        payment.setOrderId(paymentDto.getOrderId());
        payment.setCustomerId(paymentDto.getCustomerId());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setPaymentStatus(paymentDto.getPaymentStatus() != null ? paymentDto.getPaymentStatus() : "PENDING");
        payment.setAmount(paymentDto.getAmount());
        payment.setCurrency(paymentDto.getCurrency() != null ? paymentDto.getCurrency() : "USD");
        payment.setTransactionId(paymentDto.getTransactionId());
        payment.setVersion(paymentDto.getVersion());

        return payment;
    }

    public static PaymentMethodDto toPaymentMethodDto(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }

        PaymentMethodDto dto = new PaymentMethodDto(
                paymentMethod.getPaymentMethodId(),
                paymentMethod.getCustomerId(),
                paymentMethod.getMethodType(),
                paymentMethod.getProvider(),
                paymentMethod.getLastFourDigits()
        );
        dto.setIsDefault(paymentMethod.getIsDefault());
        dto.setIsActive(paymentMethod.getIsActive());
        dto.setExpiryMonth(paymentMethod.getExpiryMonth());
        dto.setExpiryYear(paymentMethod.getExpiryYear());
        dto.setCreatedAt(paymentMethod.getCreatedAt());
        dto.setUpdatedAt(paymentMethod.getUpdatedAt());
        dto.setVersion(paymentMethod.getVersion());
        return dto;
    }

    public static List<PaymentMethodDto> toPaymentMethodDtoList(List<PaymentMethod> paymentMethods) {
        return paymentMethods.stream()
                .map(PaymentMapper::toPaymentMethodDto)
                .collect(Collectors.toList());
    }

    public static PaymentMethod toPaymentMethod(PaymentMethodDto dto) {
        if (dto == null) {
            return null;
        }

        PaymentMethod method = new PaymentMethod();
        method.setPaymentMethodId(dto.getPaymentMethodId());
        method.setCustomerId(dto.getCustomerId());
        method.setMethodType(dto.getMethodType());
        method.setProvider(dto.getProvider());
        method.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        method.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        method.setLastFourDigits(dto.getLastFourDigits());
        method.setExpiryMonth(dto.getExpiryMonth());
        method.setExpiryYear(dto.getExpiryYear());
        method.setVersion(dto.getVersion());

        return method;
    }
}