package code.vanilson.marketplace.mapper;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.model.Cart;
import code.vanilson.marketplace.model.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    private CartMapper() {
    }

    public static CartDto toCartDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(CartMapper::toCartItemDto)
                .collect(Collectors.toList());

        return new CartDto(
                cart.getCartId(),
                cart.getCustomerId(),
                cart.getStatus(),
                itemDtos,
                cart.getTotalAmount(),
                cart.getCreatedAt(),
                cart.getUpdatedAt(),
                cart.getVersion()
        );
    }

    public static List<CartDto> toCartDtoList(List<Cart> carts) {
        return carts.stream()
                .map(CartMapper::toCartDto)
                .collect(Collectors.toList());
    }

    public static Cart toCart(CartDto cartDto) {
        if (cartDto == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setCartId(cartDto.getCartId());
        cart.setCustomerId(cartDto.getCustomerId());
        cart.setStatus(cartDto.getStatus() != null ? cartDto.getStatus() : "ACTIVE");
        cart.setTotalAmount(cartDto.getTotalAmount() != null ? cartDto.getTotalAmount() : java.math.BigDecimal.ZERO);
        cart.setVersion(cartDto.getVersion());

        return cart;
    }

    public static CartItemDto toCartItemDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        return new CartItemDto(
                cartItem.getCartItemId(),
                cartItem.getCart() != null ? cartItem.getCart().getCartId() : null,
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }

    public static CartItem toCartItem(CartItemDto cartItemDto) {
        if (cartItemDto == null) {
            return null;
        }

        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(cartItemDto.getCartItemId());
        cartItem.setProductId(cartItemDto.getProductId());
        cartItem.setProductName(cartItemDto.getProductName());
        cartItem.setQuantity(cartItemDto.getQuantity());
        cartItem.setPrice(cartItemDto.getPrice());
        cartItem.setVersion(cartItemDto.getVersion());

        return cartItem;
    }

    public static List<CartItem> toCartItemList(List<CartItemDto> cartItemDtos) {
        return cartItemDtos.stream()
                .map(CartMapper::toCartItem)
                .collect(Collectors.toList());
    }
}