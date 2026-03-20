package code.vanilson.marketplace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "tb_coupons")
@JsonPropertyOrder({"couponId", "code", "discountId", "discount", "usageLimit", "usageCount", "isActive", "createdAt", "updatedAt", "version"})
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id", nullable = false)
    @JsonProperty("id")
    private Long couponId;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL)
    private List<CouponUsage> usages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        if (!isActive || !discount.isValid()) {
            return false;
        }
        if (usageLimit != null && usageCount >= usageLimit) {
            return false;
        }
        return true;
    }

    public boolean canUse() {
        return isValid() && (usageLimit == null || usageCount < usageLimit);
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public Coupon() {
    }

    public Coupon(String code, Discount discount) {
        this.code = code;
        this.discount = discount;
    }

    public Coupon(String code, Discount discount, Integer usageLimit) {
        this.code = code;
        this.discount = discount;
        this.usageLimit = usageLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(couponId, coupon.couponId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(couponId);
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "couponId=" + couponId +
                ", code='" + code + '\'' +
                ", usageLimit=" + usageLimit +
                ", usageCount=" + usageCount +
                ", isActive=" + isActive +
                '}';
    }
}