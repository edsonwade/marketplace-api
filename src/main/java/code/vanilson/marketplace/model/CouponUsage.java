package code.vanilson.marketplace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "tb_coupon_usages")
@JsonPropertyOrder({"usageId", "couponId", "customerId", "orderId", "usedAt"})
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id", nullable = false)
    @JsonProperty("id")
    private Long usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }

    public CouponUsage() {
    }

    public CouponUsage(Coupon coupon, Long customerId) {
        this.coupon = coupon;
        this.customerId = customerId;
    }

    public CouponUsage(Coupon coupon, Long customerId, Long orderId) {
        this.coupon = coupon;
        this.customerId = customerId;
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponUsage that = (CouponUsage) o;
        return Objects.equals(usageId, that.usageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usageId);
    }

    @Override
    public String toString() {
        return "CouponUsage{" +
                "usageId=" + usageId +
                ", couponId=" + (coupon != null ? coupon.getCouponId() : null) +
                ", customerId=" + customerId +
                ", orderId=" + orderId +
                ", usedAt=" + usedAt +
                '}';
    }
}