package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.StockDto;
import code.vanilson.marketplace.service.StockService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private static final Logger logger = LogManager.getLogger(StockController.class);
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<Iterable<StockDto>> getAllStocks() {
        return ResponseEntity.ok().body(stockService.findAllStocks());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<StockDto> getStockByProductId(@PathVariable Long productId) {
        return stockService.findByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StockDto> createStock(@RequestBody StockDto stockDto) {
        logger.info("Creating stock for product id: {}", stockDto.getProductId());
        StockDto savedStock = stockService.save(stockDto);
        try {
            return ResponseEntity
                    .created(new URI("/api/stocks/" + savedStock.getStockId()))
                    .body(savedStock);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/product/{productId}")
    public ResponseEntity<?> updateStockQuantity(@PathVariable Long productId, @RequestParam Integer quantity) {
        logger.info("Updating stock quantity for product id: {} to {}", productId, quantity);
        if (stockService.updateStockQuantity(productId, quantity)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStock(@PathVariable Long id) {
        logger.info("Deleting stock with id: {}", id);
        if (stockService.deleteStock(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
