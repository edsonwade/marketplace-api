package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartCartIdAndProductId(Long cartId, Long productId);
    boolean existsByCartCartIdAndProductId(Long cartId, Long productId);
}