package code.vanilson.marketplace.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            String[] cleanups = {
                "DELETE FROM tb_order_items",
                "DELETE FROM tb_orders",
                "DELETE FROM tb_customers"
            };
            for (String cleanup : cleanups) {
                try { stmt.execute(cleanup); } catch (Exception ignored) {}
            }
            
            String[] inserts = {
                "INSERT INTO tb_customers (customer_id, name, email, address) VALUES (1, 'test', 'test@test.test', 'test 1')",
                "INSERT INTO tb_customers (customer_id, name, email, address) VALUES (2, 'test1', 'test1@test.test', 'test 2')"
            };
            for (String insert : inserts) {
                stmt.execute(insert);
            }
        }
    }

    @Test
    @DisplayName("GET /api/customers - Success")
    void testGetCustomers() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/customers/99 - Not Found")
    void testGetCustomerByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/customers/1 - Found")
    void testGetCustomerById() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.test"))
                .andExpect(jsonPath("$.address").value("test 1"));
    }

    @Test
    @DisplayName("POST /api/customers - Success")
    void testCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Customer\",\"email\":\"new@example.com\",\"address\":\"test 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Customer"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.address").value("test 1"));
    }

    @Test
    @DisplayName("PUT /api/customers/1 - Success")
    void testUpdateCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"email\":\"updated@example.com\",\"address\":\"Updated Address\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.address").value("Updated Address"));
    }

    @Test
    @DisplayName("PUT /api/customers/99 - Not Found")
    void testUpdateCustomerNotFound() throws Exception {
        mockMvc.perform(put("/api/customers/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"email\":\"updated@example.com\",\"address\":\"Updated Address\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/customers/1 - Success")
    void testDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/customers/99 - Not Found")
    void testDeleteCustomerNotFound() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
