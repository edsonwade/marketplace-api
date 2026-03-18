package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.OrderItemDto;
import code.vanilson.marketplace.model.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {
    /**
     * Returns the orderItem with the specified id.
     *
     * @param id ID of the orderItem to retrieve.
     * @return The requested orderItem if found.
     */
    Optional<OrderItemDto> findOrderItemById(Long id);

    /**
     * Returns all orderItems in the database.
     *
     * @return All orderItems in the database.
     */
    List<OrderItemDto> findAllOrderItems();

    /**
     * Saves the specified orderItem to the database.
     *
     * @param orderItemDto The orderItem to save to the database.
     * @return The saved orderItem.
     */
    OrderItemDto saveOrderItem(OrderItemDto orderItemDto);

    /**
     * Deletes the orderItem with the specified id.
     *
     * @param id The id of the orderItem to delete.
     * @return True if the operation was successful.
     */
    boolean deleteOrderItemById(long id);
}
