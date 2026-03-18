package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.OrderItemDto;
import code.vanilson.marketplace.exception.IllegalRequestException;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.OrderMapper;
import code.vanilson.marketplace.model.OrderItem;
import code.vanilson.marketplace.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    public static final String NOT_FOUND = " not found";
    public static final String THE_ORDER_ITEM_OBJECT_MUST_NOT_BE_NULL = "The 'orderItem' object must not be null.";
    private static final Logger logger = LogManager.getLogger(OrderItemServiceImpl.class);
    private final OrderItemRepository orderItemRepository;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<OrderItemDto> findAllOrderItems() {
        logger.info("All orderItems");
        return orderItemRepository.findAll().stream()
                .map(OrderMapper::toOrderItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderItemDto> findOrderItemById(Long id) {
        Optional<OrderItem> orderItem = orderItemRepository.findById(id);
        if (orderItem.isEmpty()) {
            throw new ObjectWithIdNotFound(String.format("orderItem with id %d not found", id));
        }
        logger.info("OrderItem with id:{} found", id);
        return orderItem.map(OrderMapper::toOrderItemDto);
    }

    @Override
    @Transactional
    public OrderItemDto saveOrderItem(OrderItemDto orderItemDto) {
        if (Objects.isNull(orderItemDto)) {
            logger.error(THE_ORDER_ITEM_OBJECT_MUST_NOT_BE_NULL);
            throw new IllegalRequestException(THE_ORDER_ITEM_OBJECT_MUST_NOT_BE_NULL);
        }
        OrderItem orderItem = OrderMapper.swallowOrderItem(orderItemDto);
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        logger.info("OrderItem saved with success:{}", savedOrderItem);
        return OrderMapper.toOrderItemDto(savedOrderItem);
    }

    @Override
    @Transactional
    public boolean deleteOrderItemById(long id) {
        Optional<OrderItem> orderItem = orderItemRepository.findById(id);
        if (orderItem.isEmpty()) {
            throw new ObjectWithIdNotFound("orderItem with id " + id + NOT_FOUND);
        }
        orderItem.ifPresent(orderItemRepository::delete);
        logger.info("Delete orderItem with id: {}", id);
        return true;
    }
}
