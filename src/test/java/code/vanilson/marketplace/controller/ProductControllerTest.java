package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.config.JwtAuthenticationFilter;
import code.vanilson.marketplace.config.JwtService;
import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.service.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    ProductServiceImpl productServiceMock;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    AuthenticationProvider authenticationProvider;
    @MockBean
    LogoutHandler logoutHandler;
    @MockBean
    JwtService jwtService;

    ProductDto product;

    @BeforeEach
    void setUp() {
        product = new ProductDto(1, "keyboard", 10, 1);
    }

    @Test
    @DisplayName("GET /api/products -Success")
    void testGetProductSuccess() throws Exception {

        when(productServiceMock.findAllProducts())
                .thenReturn(
                        List.of(
                                new ProductDto(1, "keyboard", 10, 1),
                                new ProductDto(2, "Mouse", 10, 1)
                        )
                );
        mockMvc.perform(get("/api/products"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].name").value("keyboard"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].version").value(1));

        verify(productServiceMock, times(1)).findAllProducts();

    }

    @Test
    @DisplayName("GET /api/products/{id} -Found")
    void testGetProductByIdSuccess() throws Exception {

        when(productServiceMock.findById(1)).thenReturn(Optional.of(product));
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))
                .andExpect(jsonPath("$.size()").value(4))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.version").value(1));

        verify(productServiceMock, times(1)).findById(1);

    }

    @Test
    @DisplayName("GET /api/products/{id} -Not Found")
    void testGetProductByIdNotFound() throws Exception {

        when(productServiceMock.findById(1)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("POST /api/products - Success")
    void testPostCreateNewProduct() throws Exception {
        ProductDto postProduct = new ProductDto("keyboard", 10);
        ProductDto mockProduct = new ProductDto(1, "keyboard", 10, 1);

        when(productServiceMock.save(any())).thenReturn(mockProduct);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(postProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                //validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))
                // return fields
                .andExpect(jsonPath("$.size()").value(4))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.version").value(1));

        verify(productServiceMock, times(1)).save(any());

    }

    @Test
    @DisplayName("PUT /api/products/{id} - Success")
    void testPuttUpdateProductSuccess() throws Exception {
        ProductDto putProduct = new ProductDto("keyboard", 10);
        ProductDto mockProduct = new ProductDto(1, "keyboard", 10, 1);

        when(productServiceMock.findById(1)).thenReturn(Optional.of(mockProduct));
        when(productServiceMock.update(any())).thenReturn(true);

        mockMvc.perform(put("/api/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(asJsonString(putProduct)))
                //validate the  response content type and code
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                //validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))
                // return fields
                .andExpect(jsonPath("$.size()").value(4))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.version").value(2));

        verify(productServiceMock, times(1)).update(mockProduct);

    }

    @Test
    @DisplayName("PUT /api/products/{id} -Not Found")
    void testPutProductNotFound() throws Exception {

        when(productServiceMock.findById(1)).thenReturn(Optional.empty());
        when(productServiceMock.update(any())).thenReturn(false);
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("PUT /api/products/{id} -Conflict")
    void testProductPutVersionMisMatch() throws Exception {

        ProductDto putProduct = new ProductDto("keyboard", 10);
        ProductDto mockProduct = new ProductDto(1, "keyboard", 10, 2);

        when(productServiceMock.findById(1)).thenReturn(Optional.of(mockProduct));
        when(productServiceMock.update(any())).thenReturn(false);

        mockMvc.perform(put("/api/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(asJsonString(putProduct)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} -Found")
    void testDeleteProductByIdSuccess() throws Exception {

        when(productServiceMock.findById(1)).thenReturn(Optional.of(product));
        when(productServiceMock.delete(1)).thenReturn(true);
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isOk());

        verify(productServiceMock, times(1)).delete(1);

    }

    @Test
    @DisplayName("DELETE /api/products/{id} -Not Found")
    void testDeleteProductByIdNotFound() throws Exception {

        when(productServiceMock.findById(1)).thenReturn(Optional.empty());
        when(productServiceMock.delete(any())).thenReturn(false);
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("DELETE /api/products/{id} -Failure")
    void testDeleteProductFailure() throws Exception {
        when(productServiceMock.findById(1)).thenReturn(Optional.of(product));
        when(productServiceMock.delete(1)).thenReturn(false);
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isInternalServerError());

    }

    /**
     * Method auxiliary to convert object in json
     *
     * @param obj obj
     * @return obj
     */
    protected String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}