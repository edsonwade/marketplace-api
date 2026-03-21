package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.service.OrderService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    public static final String ORDER = "/orders/";
    private static final Logger logger = LogManager.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Returns all orders in the database.
     *
     * @return All orders in the database.
     */
    @GetMapping
    public ResponseEntity<Iterable<OrderDto>> getAllOrders() {
        return ResponseEntity.ok().body(orderService.findAllOrders());
    }

    /**
     * Returns the order with the specified ID.
     *
     * @param id The ID of the order to retrieve.
     * @return The order with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<OrderDto>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(orderService.findOrderById(id));
    }

    /**
     * Creates a new order.
     *
     * @param orderDto The order to create.
     * @return The created order.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderDto> createOrder(@RequestBody @Valid OrderDto orderDto) {

        // Create a new order
        OrderDto newOrder = orderService.saveOrder(orderDto);

        try {
            // Build a created response
            return ResponseEntity
                    .created(new URI(ORDER + newOrder.getOrderId()))
                    .body(newOrder);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the fields in the specified order with the specified ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@Valid @RequestBody OrderDto orderDto,
                                             @PathVariable Long id) {
        OrderDto updatedOrder = orderService.updateOrder(id, orderDto);
        return ResponseEntity.ok().body(updatedOrder);
    }

    /**
     * Deletes the order with the specified ID.
     *
     * @param id The ID of the order to delete.
     * @return A ResponseEntity with one of the following status codes:
     * 200 OK if to delete was successful
     * 404 Not Found if a order with the specified ID is not found
     * 500 Internal Service Error if an error occurs during deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {

        logger.info("Deleting order with ID {}", id);

        // Get the existing order
        Optional<OrderDto> existingOrder = orderService.findOrderById(id);

        return existingOrder.map(p -> {
            if (orderService.deleteOrderById(p.getOrderId())) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

}
