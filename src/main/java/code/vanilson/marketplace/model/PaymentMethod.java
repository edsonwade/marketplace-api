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
@Table(name = "tb_payment_methods")
@JsonPropertyOrder({"paymentMethodId", "customerId", "methodType", "provider", "isDefault", "isActive", "lastFourDigits", "expiryMonth", "expiryYear", "createdAt", "updatedAt", "version"})
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id", nullable = false)
    @JsonProperty("id")
    private Long paymentMethodId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "method_type", nullable = false, length = 50)
    private String methodType;

    @Column(length = 50)
    private String provider;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(length = 255)
    private String token;

    @Column(name = "last_four_digits", length = 4)
    private String lastFourDigits;

    @Column(name = "expiry_month", length = 2)
    private String expiryMonth;

    @Column(name = "expiry_year", length = 4)
    private String expiryYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public PaymentMethod() {
    }

    public PaymentMethod(Long customerId, String methodType, String provider, String token, String lastFourDigits) {
        this.customerId = customerId;
        this.methodType = methodType;
        this.provider = provider;
        this.token = token;
        this.lastFourDigits = lastFourDigits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(paymentMethodId, that.paymentMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentMethodId);
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "paymentMethodId=" + paymentMethodId +
                ", customerId=" + customerId +
                ", methodType='" + methodType + '\'' +
                ", provider='" + provider + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}