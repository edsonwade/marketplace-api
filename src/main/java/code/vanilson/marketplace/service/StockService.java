package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.StockDto;
import java.util.List;
import java.util.Optional;

public interface StockService {
    List<StockDto> findAllStocks();
    Optional<StockDto> findByProductId(Long productId);
    StockDto save(StockDto stockDto);
    boolean updateStockQuantity(Long productId, Integer quantity);
    boolean deleteStock(Long id);
}
