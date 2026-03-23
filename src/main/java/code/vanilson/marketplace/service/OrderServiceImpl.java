package code.vanilson.marketplace.service;

import code.vanilson.marketplace.exception.IllegalRequestException;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.mapper.CustomerMapper;
import code.vanilson.marketplace.mapper.OrderMapper;
import code.vanilson.marketplace.model.Order;
import code.vanilson.marketplace.model.OrderItem;
import code.vanilson.marketplace.repository.OrderRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    public static final String NOT_FOUND = " not found";
    public static final String THE_ORDER_OBJECT_MUST_NOT_BE_NULL = "The 'order' object must not be null.";
    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<OrderDto> findAllOrders() {
        logger.info(" All orders");
        List<Order> orders = orderRepository.findAllOrdersWithDetails();
        return OrderMapper.toOrderDtoList(orders);
    }

    @Override
    public Optional<OrderDto> findOrderById(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            throw new ObjectWithIdNotFound(String.format("order with id %d not found", id));
        }
        logger.info("Order with id:{} found", id);
        return order.map(OrderMapper::toOrderDto);
    }

    @Override
    @Transactional
    public OrderDto saveOrder(@NotNull OrderDto orderDto) {
        if (orderDto == null) {
            logger.error(THE_ORDER_OBJECT_MUST_NOT_BE_NULL);
            throw new IllegalRequestException(THE_ORDER_OBJECT_MUST_NOT_BE_NULL);
        }
        Order order = OrderMapper.toOrder(orderDto);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order saved with success:{}", savedOrder);
        return OrderMapper.toOrderDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto updateOrder(long id, OrderDto orderDto) {
        if (Objects.isNull(orderDto)) {
            logger.error(THE_ORDER_OBJECT_MUST_NOT_BE_NULL);
            throw new IllegalRequestException(THE_ORDER_OBJECT_MUST_NOT_BE_NULL);
        }
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            logger.error("Order with id {} not found.", id);
            throw new ObjectWithIdNotFound("Order with id " + id + " not found.");
        }

        var existingOrder = optionalOrder.get();

        if (orderDto.getLocalDateTime() == null || orderDto.getCustomer() == null ||
                orderDto.getOrderItems() == null) {
            logger.error("Updating to null values for 'localDateTime', 'customer', or 'orderItems' is not allowed.");
            throw new IllegalRequestException(
                    "Updating to null values for 'localDateTime', 'customer', or 'orderItems' is not allowed.");
        }

        // Update existing Order properties
        existingOrder.setLocalDateTime(orderDto.getLocalDateTime());
        existingOrder.setCustomer(CustomerMapper.toCustomer(orderDto.getCustomer()));

        // Clear the existing OrderItems
        existingOrder.getOrderItems().clear();

        // Update existing OrderItems
        Set<OrderItem> updatedOrderItems = new HashSet<>(orderDto.getOrderItems());
        for (OrderItem updatedItem : updatedOrderItems) {
            // Set the back reference to the existing Order
            updatedItem.setOrder(existingOrder);
            existingOrder.getOrderItems().add(updatedItem);
        }

        // Save the updated Order with updated OrderItems
        Order savedOrder = orderRepository.save(existingOrder);

        // Log the updated Order
        logger.info("Order updated with success: {}", savedOrder);

        return OrderMapper.toOrderDto(savedOrder);
    }

    @Override
    public List<OrderDto> findOrdersByCustomerId(Long customerId) {
        return OrderMapper.toOrderDtoList(orderRepository.findByCustomerIdWithDetails(customerId));
    }

    @Override
    public List<OrderDto> findUnpaidOrdersByCustomerId(Long customerId) {
        return OrderMapper.toOrderDtoList(orderRepository.findUnpaidByCustomerId(customerId));
    }

    @Override
    @Transactional
    public boolean deleteOrderById(long id) {
        Optional<Order> orders = orderRepository.findById(id);
        if (orders.isEmpty()) {
            throw new ObjectWithIdNotFound("order with id " + id + NOT_FOUND);
        }
        orders.ifPresent(orderRepository::delete);
        logger.info("Delete order with id: {}", id);
        return true;
    }
}
