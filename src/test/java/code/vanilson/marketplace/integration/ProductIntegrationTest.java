package code.vanilson.marketplace.integration;

import code.vanilson.marketplace.dto.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({DBUnitExtension.class, SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    public ConnectionHolder getConnectionHolder() {
        return () -> dataSource.getConnection();
    }

    @Test
    @DisplayName("GET /api/products -Success")
    @DataSet(value = "datasets/products.yml")
    void testGetProductSuccess() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].name").value("Computer"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[0].version").value(1));

    }

    @Test
    @DisplayName("GET /api/product/1 - Found")
    @DataSet("datasets/products.yml")
    void testGetProductByIdFound() throws Exception {
        // Execute the GET request
        mockMvc.perform(get("/api/products/{id}", 1))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/1"))

                // Validate the returned fields
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.name", is("Computer")))
                .andExpect(jsonPath("$.quantity", is(10)))
                .andExpect(jsonPath("$.version", is(1)));
    }

    @Test
    @DisplayName("GET /api/products/99 - Not Found")
    @DataSet("datasets/products.yml")
    void testGetProductByIdNotFound() throws Exception {
        // Execute the GET request
        mockMvc.perform(get("/api/products/{id}", 99))

                // Validate that we get a 404 Not Found response
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/products/2 - Success")
    @DataSet("datasets/products.yml")
    void testProductPutSuccess() throws Exception {
        // Setup product to update
        ProductDto putProduct = new ProductDto("TV Plasma", 10);

        mockMvc.perform(put("/api/products/update/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 2)
                        .content(asJsonString(putProduct)))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate the headers
                .andExpect(header().string(HttpHeaders.ETAG, "\"3\""))
                .andExpect(header().string(HttpHeaders.LOCATION, "/products/2"))

                // Validate the returned fields
                .andExpect(jsonPath("$.productId", is(2)))
                .andExpect(jsonPath("$.name", is("TV Plasma")))
                .andExpect(jsonPath("$.quantity", is(10)))
                .andExpect(jsonPath("$.version", is(3)));
    }

    @Test
    @DisplayName("PUT /api/products/1 - Version Mismatch")
    @DataSet("datasets/products.yml")
    void testProductPutVersionMismatch() throws Exception {
        // Setup product to update
        ProductDto putProduct = new ProductDto("TV Plasma", 10);

        mockMvc.perform(put("/api/products/update/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 7)
                        .content(asJsonString(putProduct)))

                // Validate the response code and content type
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/products/99 - Not Found")
    @DataSet("datasets/products.yml")
    void testProductPutNotFound() throws Exception {
        // Setup product to update
        ProductDto putProduct = new ProductDto("TV Plasma", 10);

        mockMvc.perform(put("/api/products/update/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(asJsonString(putProduct)))

                // Validate the response code and content type
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/1 - Success")
    @DataSet("datasets/products.yml")
    void testProductDeleteSuccess() throws Exception {
        // Execute our DELETE request
        mockMvc.perform(delete("/api/products/delete/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/products/99 - Not Found")
    @DataSet("datasets/products.yml")
    void testProductDeleteNotFound() throws Exception {
        // Execute our DELETE request
        mockMvc.perform(delete("/api/products/delete/{id}", 99))
                .andExpect(status().isNotFound());
    }

    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
