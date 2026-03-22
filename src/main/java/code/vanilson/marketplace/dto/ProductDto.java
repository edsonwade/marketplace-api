package code.vanilson.marketplace.dto;

import code.vanilson.marketplace.model.Product;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO for {@link Product}
 */
@Data
@Getter
@Setter
public class ProductDto implements Serializable {
    Long productId;
    @NotNull
    @Size(max = 45)
    String name;
    @NotNull
    @Size(max = 1000)
    Integer quantity;
    java.math.BigDecimal price;
    Integer version;

    public ProductDto() {
        // Default constructor
    }

    public ProductDto(Long productId, String name, Integer quantity, Integer version) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.version = version;
    }

    public ProductDto(String name, Integer quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}