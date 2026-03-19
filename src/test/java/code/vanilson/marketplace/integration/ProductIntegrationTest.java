package code.vanilson.marketplace.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            try { stmt.execute("DELETE FROM tb_products"); } catch (Exception ignored) {}

            String[] inserts = {
                "INSERT INTO tb_products (product_id, name, quantity, version) VALUES (1, 'Computer', 10, 1)",
                "INSERT INTO tb_products (product_id, name, quantity, version) VALUES (2, 'Mouse', 5, 1)",
                "INSERT INTO tb_products (product_id, name, quantity, version) VALUES (3, 'Keyboard', 3, 1)"
            };
            for (String insert : inserts) {
                stmt.execute(insert);
            }
        }
    }

    @Test
    @DisplayName("GET /api/products - Success")
    void testGetProductSuccess() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].name").value("Computer"));
    }

    @Test
    @DisplayName("GET /api/products/1 - Found")
    void testGetProductByIdFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.name").value("Computer"));
    }

    @Test
    @DisplayName("GET /api/products/99 - Not Found")
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/products/1 - Success")
    void testProductPutSuccess() throws Exception {
        String json = "{\"name\":\"TV Plasma\",\"quantity\":10}";
        mockMvc.perform(put("/api/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TV Plasma"))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    @DisplayName("PUT /api/products/1 - Version Mismatch")
    void testProductPutVersionMismatch() throws Exception {
        String json = "{\"name\":\"TV Plasma\",\"quantity\":10}";
        mockMvc.perform(put("/api/products/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 7)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/products/99 - Not Found")
    void testProductPutNotFound() throws Exception {
        String json = "{\"name\":\"TV Plasma\",\"quantity\":10}";
        mockMvc.perform(put("/api/products/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.IF_MATCH, 1)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/1 - Success")
    void testProductDeleteSuccess() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/products/99 - Not Found")
    void testProductDeleteNotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
