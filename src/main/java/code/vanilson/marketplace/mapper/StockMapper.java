package code.vanilson.marketplace.mapper;

import code.vanilson.marketplace.dto.StockDto;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.model.Stock;

import java.util.List;
import java.util.stream.Collectors;

public class StockMapper {

    private StockMapper() {
        // default constructor
    }

    public static StockDto toStockDto(Stock stock) {
        if (stock == null) {
            return null;
        }
        return new StockDto(
                stock.getStockId(),
                stock.getProduct() != null ? stock.getProduct().getProductId() : null,
                stock.getQuantity(),
                stock.getLocation()
        );
    }

    public static Stock toStock(StockDto stockDto, Product product) {
        if (stockDto == null) {
            return null;
        }
        Stock stock = new Stock();
        stock.setStockId(stockDto.getStockId());
        stock.setProduct(product);
        stock.setQuantity(stockDto.getQuantity());
        stock.setLocation(stockDto.getLocation());
        return stock;
    }

    public static List<StockDto> toStockDtoList(List<Stock> stocks) {
        return stocks.stream()
                .map(StockMapper::toStockDto)
                .collect(Collectors.toList());
    }
}
