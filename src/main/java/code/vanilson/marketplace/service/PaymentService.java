package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.PaymentDto;
import code.vanilson.marketplace.dto.PaymentMethodDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.PaymentMapper;
import code.vanilson.marketplace.model.Payment;
import code.vanilson.marketplace.model.PaymentMethod;
import code.vanilson.marketplace.repository.PaymentMethodRepository;
import code.vanilson.marketplace.repository.PaymentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentService(PaymentRepository paymentRepository, PaymentMethodRepository paymentMethodRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public List<PaymentDto> findAllPayments() {
        return PaymentMapper.toPaymentDtoList(paymentRepository.findAll());
    }

    public Optional<PaymentDto> findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentMapper::toPaymentDto);
    }

    public List<PaymentDto> findPaymentsByOrderId(Long orderId) {
        return PaymentMapper.toPaymentDtoList(paymentRepository.findByOrderId(orderId));
    }

    public List<PaymentDto> findPaymentsByCustomerId(Long customerId) {
        return PaymentMapper.toPaymentDtoList(paymentRepository.findByCustomerId(customerId));
    }

    @Transactional
    public PaymentDto createPayment(PaymentDto paymentDto) {
        logger.info("Creating payment for order: {}", paymentDto.getOrderId());

        Payment payment = PaymentMapper.toPayment(paymentDto);
        
        payment.setTransactionId(UUID.randomUUID().toString());
        
        Payment saved = paymentRepository.save(payment);
        
        payment.setPaymentStatus("COMPLETED");
        saved = paymentRepository.save(payment);
        
        logger.info("Created payment with id: {}", saved.getPaymentId());
        return PaymentMapper.toPaymentDto(saved);
    }

    @Transactional
    public PaymentDto processPayment(Long orderId, Long customerId, String paymentMethod, String token) {
        logger.info("Processing payment for order: {}, customer: {}, method: {}", orderId, customerId, paymentMethod);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setCustomerId(customerId);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus("PROCESSING");
        payment.setTransactionId(UUID.randomUUID().toString());
        
        Payment saved = paymentRepository.save(payment);
        
        boolean success = processWithGateway(payment);
        
        if (success) {
            saved.setPaymentStatus("COMPLETED");
            logger.info("Payment completed successfully: {}", saved.getPaymentId());
        } else {
            saved.setPaymentStatus("FAILED");
            logger.warn("Payment failed: {}", saved.getPaymentId());
        }

        Payment completed = paymentRepository.save(saved);
        return PaymentMapper.toPaymentDto(completed);
    }

    private boolean processWithGateway(Payment payment) {
        return true;
    }

    @Transactional
    public PaymentDto updatePaymentStatus(Long paymentId, String status) {
        logger.info("Updating payment status: {} to {}", paymentId, status);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ObjectWithIdNotFound("Payment not found with id: " + paymentId));
        
        payment.setPaymentStatus(status);
        Payment saved = paymentRepository.save(payment);
        
        logger.info("Payment status updated to: {}", status);
        return PaymentMapper.toPaymentDto(saved);
    }

    @Transactional
    public boolean deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ObjectWithIdNotFound("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
        logger.info("Deleted payment with id: {}", id);
        return true;
    }

    public List<PaymentMethodDto> findPaymentMethodsByCustomerId(Long customerId) {
        return PaymentMapper.toPaymentMethodDtoList(paymentMethodRepository.findByCustomerIdAndIsActiveTrue(customerId));
    }

    public Optional<PaymentMethodDto> findPaymentMethodById(Long id) {
        return paymentMethodRepository.findById(id)
                .map(PaymentMapper::toPaymentMethodDto);
    }

    @Transactional
    public PaymentMethodDto addPaymentMethod(PaymentMethodDto paymentMethodDto) {
        logger.info("Adding payment method for customer: {}", paymentMethodDto.getCustomerId());

        if (paymentMethodDto.getIsDefault() != null && paymentMethodDto.getIsDefault()) {
            paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(paymentMethodDto.getCustomerId())
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        paymentMethodRepository.save(existing);
                    });
        }

        PaymentMethod paymentMethod = PaymentMapper.toPaymentMethod(paymentMethodDto);
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);

        logger.info("Added payment method with id: {}", saved.getPaymentMethodId());
        return PaymentMapper.toPaymentMethodDto(saved);
    }

    @Transactional
    public PaymentMethodDto setDefaultPaymentMethod(Long customerId, Long paymentMethodId) {
        logger.info("Setting default payment method: {} for customer: {}", paymentMethodId, customerId);

        paymentMethodRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    paymentMethodRepository.save(existing);
                });

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ObjectWithIdNotFound("Payment method not found with id: " + paymentMethodId));

        paymentMethod.setIsDefault(true);
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);

        logger.info("Default payment method set to: {}", paymentMethodId);
        return PaymentMapper.toPaymentMethodDto(saved);
    }

    @Transactional
    public boolean deletePaymentMethod(Long id) {
        if (!paymentMethodRepository.existsById(id)) {
            throw new ObjectWithIdNotFound("Payment method not found with id: " + id);
        }
        paymentMethodRepository.deleteById(id);
        logger.info("Deleted payment method with id: {}", id);
        return true;
    }
}