package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    List<CouponUsage> findByCouponCouponId(Long couponId);
    List<CouponUsage> findByCustomerId(Long customerId);
    boolean existsByCouponCouponIdAndCustomerId(Long couponId, Long customerId);
}