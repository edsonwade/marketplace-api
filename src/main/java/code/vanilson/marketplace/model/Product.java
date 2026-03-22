package code.vanilson.marketplace.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "tb_products")
@JsonPropertyOrder({"id", "name", "quantity", "version"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    @JsonProperty("id")
    private Long productId;
    private String name;
    private Integer quantity;
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal price = java.math.BigDecimal.valueOf(9.99);
    @Version
    private Integer version;


    public Product() {
        // default constructor
    }

    public Product(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public Product(Long id, String name, Integer quantity, Integer version) {
        this.productId = id;
        this.name = name;
        this.quantity = quantity;
        this.version = version;
    }

    public Product(Long id, String name, Integer quantity) {
        this.productId = id;
        this.name = name;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;
        return Objects.equals(productId, product.productId) && Objects.equals(name, product.name) && Objects.equals(quantity, product.quantity) && Objects.equals(version, product.version);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(productId);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(quantity);
        result = 31 * result + Objects.hashCode(version);
        return result;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + productId +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", version=" + version +
                '}';
    }


}
