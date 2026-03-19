package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.StockDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.StockMapper;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.model.Stock;
import code.vanilson.marketplace.repository.ProductRepository;
import code.vanilson.marketplace.repository.StockRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LogManager.getLogger(StockServiceImpl.class);

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    public StockServiceImpl(StockRepository stockRepository, ProductRepository productRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<StockDto> findAllStocks() {
        return StockMapper.toStockDtoList(stockRepository.findAll());
    }

    @Override
    public Optional<StockDto> findByProductId(Long productId) {
        return stockRepository.findByProductProductId(productId)
                .map(StockMapper::toStockDto);
    }

    @Override
    @Transactional
    public StockDto save(StockDto stockDto) {
        Product product = productRepository.findById(stockDto.getProductId())
                .orElseThrow(() -> new ObjectWithIdNotFound(
                        MessageFormat.format("Product with id {0} not found", stockDto.getProductId())));
        
        Stock stock = StockMapper.toStock(stockDto, product);
        stock.setVersion(1);
        Stock savedStock = stockRepository.save(stock);
        logger.info("Saved stock for product {}: quantity {}", stockDto.getProductId(), stockDto.getQuantity());
        return StockMapper.toStockDto(savedStock);
    }

    @Override
    @Transactional
    public boolean updateStockQuantity(Long productId, Integer quantity) {
        return stockRepository.findByProductProductId(productId)
                .map(stock -> {
                    stock.setQuantity(quantity);
                    stockRepository.save(stock);
                    logger.info("Updated stock quantity for product {}: {}", productId, quantity);
                    return true;
                }).orElse(false);
    }

    @Override
    @Transactional
    public boolean deleteStock(Long id) {
        if (!stockRepository.existsById(id)) {
            throw new ObjectWithIdNotFound(MessageFormat.format("Stock with id {0} not found", id));
        }
        stockRepository.deleteById(id);
        logger.info("Deleted stock with id: {}", id);
        return true;
    }
}
