package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByCustomerId(Long customerId);
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, String paymentStatus);
    List<Payment> findByPaymentStatus(String paymentStatus);
}