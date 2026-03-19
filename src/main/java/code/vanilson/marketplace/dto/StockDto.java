package code.vanilson.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDto {
    private long stockId;
    private Long productId;
    private Integer quantity;
    private String location;
}
