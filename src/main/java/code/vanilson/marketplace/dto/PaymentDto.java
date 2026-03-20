package code.vanilson.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"paymentId", "orderId", "customerId", "paymentMethod", "paymentStatus", "amount", "currency", "transactionId", "createdAt", "updatedAt", "version"})
public class PaymentDto {

    @JsonProperty("id")
    private Long paymentId;

    private Long orderId;

    private Long customerId;

    private String paymentMethod;

    private String paymentStatus;

    private BigDecimal amount;

    private String currency;

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    public PaymentDto(Long paymentId, Long orderId, Long customerId, String paymentMethod, BigDecimal amount) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = "PENDING";
        this.currency = "USD";
    }
}