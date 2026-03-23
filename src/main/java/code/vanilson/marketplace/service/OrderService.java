package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    /**
     * Returns the order with the specified id.
     *
     * @param id ID of the order to retrieve.
     * @return The requested order if found.
     */
    Optional<OrderDto> findOrderById(Long id);

    /**
     * Returns all orders in the database.
     *
     * @return All orders in the database.
     */
    List<OrderDto> findAllOrders();

    /**
     * Updates the specified order, identified by its id.
     *
     * @param id    The id of the order to update.
     * @param orderDto The order to update.
     * @return The updated order.
     */
    OrderDto updateOrder(long id, OrderDto orderDto);

    /**
     * Saves the specified order to the database.
     *
     * @param orderDto The order to save to the database.
     * @return The saved order.
     */
    OrderDto saveOrder(OrderDto orderDto);

    /**
     * Deletes the order with the specified id.
     *
     * @param id The id of the order to delete.
     * @return True if the operation was successful.
     */
    boolean deleteOrderById(long id);

    List<OrderDto> findOrdersByCustomerId(Long customerId);

    List<OrderDto> findUnpaidOrdersByCustomerId(Long customerId);
}
