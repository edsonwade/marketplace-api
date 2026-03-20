package code.vanilson.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"paymentMethodId", "customerId", "methodType", "provider", "isDefault", "isActive", "lastFourDigits", "expiryMonth", "expiryYear", "createdAt", "updatedAt", "version"})
public class PaymentMethodDto {

    @JsonProperty("id")
    private Long paymentMethodId;

    private Long customerId;

    private String methodType;

    private String provider;

    private Boolean isDefault;

    private Boolean isActive;

    private String lastFourDigits;

    private String expiryMonth;

    private String expiryYear;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    public PaymentMethodDto(Long paymentMethodId, Long customerId, String methodType, String provider, String lastFourDigits) {
        this.paymentMethodId = paymentMethodId;
        this.customerId = customerId;
        this.methodType = methodType;
        this.provider = provider;
        this.lastFourDigits = lastFourDigits;
        this.isDefault = false;
        this.isActive = true;
    }
}