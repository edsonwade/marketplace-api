package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.StockDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.model.Stock;
import code.vanilson.marketplace.repository.ProductRepository;
import code.vanilson.marketplace.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockServiceImplTest {

    StockRepository stockRepositoryMock;
    ProductRepository productRepositoryMock;
    StockServiceImpl currentInstance;

    Product product;
    Stock stock;

    @BeforeEach
    void setUp() {
        stockRepositoryMock = mock(StockRepository.class);
        productRepositoryMock = mock(ProductRepository.class);
        currentInstance = new StockServiceImpl(stockRepositoryMock, productRepositoryMock);

        product = new Product(1L, "Keyboard", 10, 1);
        stock = new Stock(1L, product, 100, "Warehouse A", 1);
    }

    @Test
    @DisplayName("Find all stocks - Success")
    void testFindAllStocks() {
        when(stockRepositoryMock.findAll()).thenReturn(List.of(stock));
        List<StockDto> result = currentInstance.findAllStocks();
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getQuantity().intValue());
        verify(stockRepositoryMock, times(1)).findAll();
    }

    @Test
    @DisplayName("Find stock by product id - Success")
    void testFindByProductId() {
        when(stockRepositoryMock.findByProductProductId(1L)).thenReturn(Optional.of(stock));
        Optional<StockDto> result = currentInstance.findByProductId(1L);
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getQuantity().intValue());
        verify(stockRepositoryMock, times(1)).findByProductProductId(1L);
    }

    @Test
    @DisplayName("Save stock - Success")
    void testSaveStock() {
        StockDto dto = new StockDto(1L, 1L, 50, "Warehouse B");
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(product));
        when(stockRepositoryMock.save(any(Stock.class))).thenReturn(new Stock(2L, product, 50, "Warehouse B", 1));

        StockDto result = currentInstance.save(dto);
        assertNotNull(result.getStockId());
        assertEquals(50, result.getQuantity().intValue());
        verify(stockRepositoryMock, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Save stock - Product not found")
    void testSaveStockProductNotFound() {
        StockDto dto = new StockDto(99L, 99L, 50, "Warehouse B");
        when(productRepositoryMock.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjectWithIdNotFound.class, () -> currentInstance.save(dto));
        verify(stockRepositoryMock, never()).save(any(Stock.class));
    }

    @Test
    @DisplayName("Update stock quantity - Success")
    void testUpdateStockQuantity() {
        when(stockRepositoryMock.findByProductProductId(1L)).thenReturn(Optional.of(stock));
        boolean result = currentInstance.updateStockQuantity(1L, 200);
        assertTrue(result);
        assertEquals(200, stock.getQuantity().intValue());
        verify(stockRepositoryMock, times(1)).save(stock);
    }

    @Test
    @DisplayName("Delete stock - Success")
    void testDeleteStock() {
        when(stockRepositoryMock.existsById(1L)).thenReturn(true);
        boolean result = currentInstance.deleteStock(1L);
        assertTrue(result);
        verify(stockRepositoryMock, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete stock - Not found")
    void testDeleteStockNotFound() {
        when(stockRepositoryMock.existsById(99L)).thenReturn(false);
        assertThrows(ObjectWithIdNotFound.class, () -> currentInstance.deleteStock(99L));
        verify(stockRepositoryMock, never()).deleteById(99L);
    }
}
