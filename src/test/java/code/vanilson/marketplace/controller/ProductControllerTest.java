package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.service.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;
    @Mock
    private ProductServiceImpl productService;
    @InjectMocks
    private ProductController productController;
    private ObjectMapper objectMapper;
    private ProductDto product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
        product = new ProductDto(1L, "keyboard", 10, 1);
    }

    @Test
    @DisplayName("GET /api/products - Success")
    void testGetProductSuccess() throws Exception {
        when(productService.findAllProducts())
                .thenReturn(List.of(
                        new ProductDto(1L, "keyboard", 10, 1),
                        new ProductDto(2L, "Mouse", 10, 1)
                ));
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].name").value("keyboard"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].version").value(1));
        verify(productService, times(1)).findAllProducts();
    }

    @Test
    @DisplayName("GET /api/products/{id} - Found")
    void testGetProductByIdSuccess() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.of(product));
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.version").value(1));
        verify(productService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id} - Not Found")
    void testGetProductByIdNotFound() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products - Success")
    void testPostCreateNewProduct() throws Exception {
        ProductDto postProduct = new ProductDto("keyboard", 10);
        ProductDto mockProduct = new ProductDto(1L, "keyboard", 10, 1);
        when(productService.save(any())).thenReturn(mockProduct);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.version").value(1));
        verify(productService, times(1)).save(any());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Success")
    void testPuttUpdateProductSuccess() throws Exception {
        ProductDto putProduct = new ProductDto("keyboard", 10);
        ProductDto mockProduct = new ProductDto(1L, "keyboard", 10, 1);
        when(productService.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productService.update(any())).thenReturn(true);
        mockMvc.perform(put("/api/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(objectMapper.writeValueAsString(putProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.quantity").value(10));
        verify(productService, times(1)).update(any());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Not Found")
    void testPutProductNotFound() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/products/{id} -Conflict")
    void testProductPutVersionMisMatch() throws Exception {
        ProductDto putProduct = new ProductDto("keyboard", 10);
        ProductDto mockProduct = new ProductDto(1L, "keyboard", 10, 2);
        when(productService.findById(1L)).thenReturn(Optional.of(mockProduct));
        mockMvc.perform(put("/api/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(objectMapper.writeValueAsString(putProduct)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Found")
    void testDeleteProductByIdSuccess() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.of(product));
        when(productService.delete(1L)).thenReturn(true);
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isOk());
        verify(productService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Not Found")
    void testDeleteProductByIdNotFound() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Failure")
    void testDeleteProductFailure() throws Exception {
        when(productService.findById(1L)).thenReturn(Optional.of(product));
        when(productService.delete(1L)).thenReturn(false);
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isInternalServerError());
    }
}
