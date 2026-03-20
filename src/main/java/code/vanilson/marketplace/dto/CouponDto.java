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
@JsonPropertyOrder({"couponId", "code", "discountId", "discount", "usageLimit", "usageCount", "isActive", "createdAt", "updatedAt", "version"})
public class CouponDto {

    @JsonProperty("id")
    private Long couponId;

    private String code;

    @JsonProperty("discountId")
    private Long discountId;

    private DiscountDto discount;

    private Integer usageLimit;

    private Integer usageCount;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    public CouponDto(Long couponId, String code, Long discountId, Integer usageLimit, Integer usageCount) {
        this.couponId = couponId;
        this.code = code;
        this.discountId = discountId;
        this.usageLimit = usageLimit;
        this.usageCount = usageCount;
        this.isActive = true;
    }
}