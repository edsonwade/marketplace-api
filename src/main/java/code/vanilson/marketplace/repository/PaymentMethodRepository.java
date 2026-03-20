package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByCustomerId(Long customerId);
    List<PaymentMethod> findByCustomerIdAndIsActiveTrue(Long customerId);
    Optional<PaymentMethod> findByCustomerIdAndIsDefaultTrue(Long customerId);
    boolean existsByCustomerIdAndIsDefaultTrue(Long customerId);
}