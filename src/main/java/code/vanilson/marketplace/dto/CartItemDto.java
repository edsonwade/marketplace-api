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
@JsonPropertyOrder({"cartItemId", "cartId", "productId", "productName", "quantity", "price", "subtotal", "createdAt", "updatedAt", "version"})
public class CartItemDto {

    @JsonProperty("id")
    private Long cartItemId;

    private Long cartId;

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("productName")
    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer version;

    public CartItemDto(Long cartItemId, Long cartId, Long productId, String productName, Integer quantity, BigDecimal price) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }

    public CartItemDto(Long productId, String productName, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }
}