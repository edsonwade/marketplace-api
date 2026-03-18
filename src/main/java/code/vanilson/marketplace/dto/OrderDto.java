package code.vanilson.marketplace.dto;

import code.vanilson.marketplace.model.Order;
import code.vanilson.marketplace.model.OrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link Order}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto implements Serializable {
    Long orderId;
    LocalDateTime localDateTime;
    @NotNull
    CustomerDto customer;
    @NotNull
    List<OrderItem> orderItems;
}