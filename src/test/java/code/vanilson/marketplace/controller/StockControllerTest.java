package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.StockDto;
import code.vanilson.marketplace.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    private MockMvc mockMvc;
    @Mock
    private StockService stockService;
    @InjectMocks
    private StockController stockController;
    private ObjectMapper objectMapper;
    private StockDto stockDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stockController).build();
        objectMapper = new ObjectMapper();
        stockDto = new StockDto(1L, 1L, Integer.valueOf(100), "Warehouse A");
    }

    @Test
    @DisplayName("GET /api/stocks - Success")
    void testGetAllStocks() throws Exception {
        when(stockService.findAllStocks()).thenReturn(List.of(stockDto));
        mockMvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].stockId").value(1))
                .andExpect(jsonPath("$[0].quantity").value(100));
    }

    @Test
    @DisplayName("GET /api/stocks/product/{productId} - Success")
    void testGetStockByProductId() throws Exception {
        when(stockService.findByProductId(1L)).thenReturn(Optional.of(stockDto));
        mockMvc.perform(get("/api/stocks/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    @DisplayName("POST /api/stocks - Success")
    void testCreateStock() throws Exception {
        when(stockService.save(any(StockDto.class))).thenReturn(stockDto);
        mockMvc.perform(post("/api/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockId").value(1));
    }

    @Test
    @DisplayName("PATCH /api/stocks/product/{productId} - Success")
    void testUpdateStockQuantity() throws Exception {
        when(stockService.updateStockQuantity(1L, 200)).thenReturn(true);
        mockMvc.perform(patch("/api/stocks/product/1")
                        .param("quantity", "200"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/stocks/{id} - Success")
    void testDeleteStock() throws Exception {
        when(stockService.deleteStock(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/stocks/1"))
                .andExpect(status().isOk());
    }
}
