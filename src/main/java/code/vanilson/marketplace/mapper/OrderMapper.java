package code.vanilson.marketplace.mapper;

import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.dto.OrderItemDto;
import code.vanilson.marketplace.model.Order;
import code.vanilson.marketplace.model.OrderItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderDto toOrderDto(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderDto(
                order.getOrderId(),
                order.getLocalDateTime(),
                CustomerMapper.toCustomerDto(order.getCustomer()),
                order.getOrderItems() != null ? new ArrayList<>(order.getOrderItems()) : new ArrayList<>(),
                null  // totalAmount — not stored on Order, only populated during checkout
        );
    }

    public static Order toOrder(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }
        Order order = new Order();
        order.setOrderId(orderDto.getOrderId());
        order.setLocalDateTime(orderDto.getLocalDateTime());
        order.setCustomer(CustomerMapper.toCustomer(orderDto.getCustomer()));
        if (orderDto.getOrderItems() != null) {
            order.setOrderItems(new HashSet<>(orderDto.getOrderItems()));
        }
        return order;
    }

    public static List<OrderDto> toOrderDtoList(List<Order> orders) {
        return orders.stream().map(OrderMapper::toOrderDto).collect(Collectors.toList());
    }

    public static OrderItemDto toOrderItemDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        return new OrderItemDto(
                orderItem.getOrderItemId(),
                toOrderDto(orderItem.getOrder()),
                ProductMapper.toProductDto(orderItem.getProduct()),
                orderItem.getQuantity()
        );
    }

    public static OrderItem swallowOrderItem(OrderItemDto orderItemDto) {
         if (orderItemDto == null) {
            return null;
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(orderItemDto.getOrderItemId());
        orderItem.setOrder(toOrder(orderItemDto.getOrder()));
        orderItem.setProduct(ProductMapper.toProduct(orderItemDto.getProduct()));
        orderItem.setQuantity(orderItemDto.getQuantity());
        return orderItem;
    }
}
