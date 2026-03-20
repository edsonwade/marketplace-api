package code.vanilson.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"cartId", "customerId", "status", "items", "totalAmount", "createdAt", "updatedAt", "version"})
public class CartDto {

    @JsonProperty("id")
    private Long cartId;

    @JsonProperty("customerId")
    private Long customerId;

    private String status;

    @JsonProperty("items")
    private List<CartItemDto> items;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    public CartDto(Long cartId, Long customerId, String status) {
        this.cartId = cartId;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = BigDecimal.ZERO;
    }
}