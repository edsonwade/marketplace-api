package code.vanilson.marketplace.integration;


import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.dto.OrderDto;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({DBUnitExtension.class, SpringExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class OrderIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    public static final String EXPECTED_VALUE = "2023-09-19T00:00:00";


    @SuppressWarnings("unused")
    public ConnectionHolder getConnectionHolder() {
        return () -> dataSource.getConnection();
    }

    /**
     * Test to verify getting all orders.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("GET /api/orders -Success")
    @DataSet(value = "datasets/orders.yml")
    void testGetOrders() throws Exception {


        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].customer.customerId").value(1))
                .andExpect(jsonPath("$[0].customer.name").value("test"))
                .andExpect(jsonPath("$[0].customer.email").value("test@test.test"))
                .andExpect(jsonPath("$[0].customer.address").value("test 1"))
                .andExpect(jsonPath("$[0].localDateTime").value(EXPECTED_VALUE));


    }

    /**
     * Test to verify getting all orders when the list is empty.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("GET /api/orders -Success")
    @DataSet(value = "datasets/orders.yml")
    void testGetOrdersSuccessEmptyList() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test to verify getting an order by ID when the order exists.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("Get /api/orders/1 - Success")
    @DataSet(value = "datasets/orders.yml")
    void testGetOrderByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("orderId").value(1))
                .andExpect(jsonPath("customer.customerId").value(1))
                .andExpect(jsonPath("customer.name").value("test"))
                .andExpect(jsonPath("customer.email").value("test@test.test"))
                .andExpect(jsonPath("customer.address").value("test 1"))
                .andExpect(jsonPath("localDateTime").value(EXPECTED_VALUE));

    }

    /**
     * Test to verify getting an order by ID when the order does not exist.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("GET  /api/orders/99 - Not Found")
    @DataSet(value = "datasets/orders.yml")
    void testGetOrderByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 99))
                .andExpect(status().isNotFound());
    }

    /**
     * Test to verify creating a new order.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("POST /api/orders -Success")
    @DataSet(value = "datasets/orders.yml")
    void testCreateOrder() throws Exception {
        CustomerDto customer = new CustomerDto(1L, "test", "test@test.test", "test 1");
        OrderDto orderDto = OrderDto.builder()
                .customer(customer)
                .orderItems(java.util.Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").exists());

    }


    @Test
    @DisplayName("/api/orders/1 -Success")
    @DataSet(value = "datasets/orders.yml")
    void testDeleteOrderSuccess() throws Exception {
        Long orderId = 1L;
        // Performing a DELETE request to delete the order
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isOk());
    }

    /**
     * Test to verify deleting an order when the order is not found.
     *
     * @throws Exception If an error occurs during the test execution.
     */
    @Test
    @DisplayName("/api/orders/123 - Failed")
    @DataSet(value = "datasets/orders.yml")
    void testDeleteOrderIdNotFound() throws Exception {
        Long orderId = 123L;
        // Performing a DELETE request to delete the order
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }


    /**
     * Utility method to convert an object to JSON string
     */
    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}



