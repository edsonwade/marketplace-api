package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    List<Discount> findByIsActiveTrue();
    
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findAllActiveDiscounts(LocalDateTime now);
    
    Optional<Discount> findByName(String name);
}