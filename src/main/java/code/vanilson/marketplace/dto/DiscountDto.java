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
@JsonPropertyOrder({"discountId", "name", "description", "discountType", "discountValue", "minPurchaseAmount", "maxDiscountAmount", "startDate", "endDate", "isActive", "createdAt", "updatedAt", "version"})
public class DiscountDto {

    @JsonProperty("id")
    private Long discountId;

    private String name;

    private String description;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal minPurchaseAmount;

    private BigDecimal maxDiscountAmount;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    public DiscountDto(Long discountId, String name, String description, String discountType, BigDecimal discountValue, LocalDateTime startDate, LocalDateTime endDate) {
        this.discountId = discountId;
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
    }
}