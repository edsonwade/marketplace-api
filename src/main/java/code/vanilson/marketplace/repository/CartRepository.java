package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerIdAndStatus(Long customerId, String status);
    Optional<Cart> findByCustomerId(Long customerId);
    boolean existsByCustomerIdAndStatus(Long customerId, String status);
}