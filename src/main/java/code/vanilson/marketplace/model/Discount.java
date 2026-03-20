package code.vanilson.marketplace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "tb_discounts")
@JsonPropertyOrder({"discountId", "name", "description", "discountType", "discountValue", "minPurchaseAmount", "maxDiscountAmount", "startDate", "endDate", "isActive", "createdAt", "updatedAt", "version"})
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id", nullable = false)
    @JsonProperty("id")
    private Long discountId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL)
    private List<Coupon> coupons = new ArrayList<>();

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
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(startDate) && now.isBefore(endDate);
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isValid() || amount.compareTo(minPurchaseAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if ("PERCENTAGE".equals(discountType)) {
            discount = amount.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discount = discountValue;
        }

        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        return discount.compareTo(amount) > 0 ? amount : discount;
    }

    public Discount() {
    }

    public Discount(String name, String description, String discountType, BigDecimal discountValue) {
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount discount = (Discount) o;
        return Objects.equals(discountId, discount.discountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discountId);
    }

    @Override
    public String toString() {
        return "Discount{" +
                "discountId=" + discountId +
                ", name='" + name + '\'' +
                ", discountType='" + discountType + '\'' +
                ", discountValue=" + discountValue +
                ", isActive=" + isActive +
                '}';
    }
}