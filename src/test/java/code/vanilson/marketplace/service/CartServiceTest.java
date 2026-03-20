package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.Cart;
import code.vanilson.marketplace.model.CartItem;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.repository.CartItemRepository;
import code.vanilson.marketplace.repository.CartRepository;
import code.vanilson.marketplace.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setQuantity(10);

        cart = new Cart();
        cart.setCartId(1L);
        cart.setCustomerId(1L);
        cart.setStatus("ACTIVE");
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setCartItemId(1L);
        cartItem.setCart(cart);
        cartItem.setProductId(1L);
        cartItem.setProductName("Test Product");
        cartItem.setQuantity(2);
        cartItem.setPrice(BigDecimal.valueOf(100));
    }

    @Test
    void testFindAllCartsReturnsAllCarts() {
        List<Cart> carts = List.of(cart);
        when(cartRepository.findAll()).thenReturn(carts);

        List<CartDto> result = cartService.findAllCarts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void testFindAllCartsReturnsEmptyList() {
        when(cartRepository.findAll()).thenReturn(new ArrayList<>());

        List<CartDto> result = cartService.findAllCarts();

        assertThat(result).isEmpty();
    }

    @Test
    void testFindCartByIdReturnsCart() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        Optional<CartDto> result = cartService.findCartById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCartId()).isEqualTo(1L);
    }

    @Test
    void testFindCartByIdReturnsEmpty() {
        when(cartRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CartDto> result = cartService.findCartById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void testFindActiveCartByCustomerIdReturnsCart() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));

        Optional<CartDto> result = cartService.findActiveCartByCustomerId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void testFindActiveCartByCustomerIdReturnsEmpty() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.empty());

        Optional<CartDto> result = cartService.findActiveCartByCustomerId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void testCreateCartCreatesNewCart() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart c = invocation.getArgument(0);
            c.setCartId(1L);
            return c;
        });

        CartDto result = cartService.createCart(1L);

        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void testCreateCartReturnsExistingActiveCart() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));

        CartDto result = cartService.createCart(1L);

        assertThat(result.getCartId()).isEqualTo(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testAddItemToCartAddsNewItem() {
        CartItemDto itemDto = new CartItemDto(1L, "Test Product", 2, BigDecimal.valueOf(100));
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.addItemToCart(1L, itemDto);

        assertThat(result).isNotNull();
    }

    @Test
    void testAddItemToCartThrowsWhenProductNotFound() {
        CartItemDto itemDto = new CartItemDto(999L, "Test Product", 2, BigDecimal.valueOf(100));
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(1L, itemDto))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void testAddItemToCartUpdatesQuantityWhenItemExists() {
        cart.getItems().add(cartItem);
        CartItemDto itemDto = new CartItemDto(1L, "Test Product", 3, BigDecimal.valueOf(100));
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItemToCart(1L, itemDto);

        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void testUpdateCartItemQuantityUpdatesQuantity() {
        cart.getItems().add(cartItem);
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.updateCartItemQuantity(1L, 1L, 5);

        assertThat(result).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void testUpdateCartItemQuantityRemovesItemWhenZero() {
        cart.getItems().add(cartItem);
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateCartItemQuantity(1L, 1L, 0);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void testUpdateCartItemQuantityThrowsWhenCartNotFound() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1L, 1L, 5))
                .isInstanceOf(ObjectWithIdNotFound.class);
    }

    @Test
    void testRemoveItemFromCartRemovesItem() {
        cart.getItems().add(cartItem);
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.removeItemFromCart(1L, 1L);

        assertThat(result).isNotNull();
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void testClearCartClearsAllItems() {
        cart.getItems().add(cartItem);
        cart.setTotalAmount(BigDecimal.valueOf(100));
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.clearCart(1L);

        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void testCheckoutChangesStatusToCheckedOut() {
        cart.getItems().add(cartItem);
        
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDto result = cartService.checkout(1L);

        assertThat(result.getStatus()).isEqualTo("CHECKED_OUT");
    }

    @Test
    void testCheckoutThrowsWhenCartEmpty() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.checkout(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    void testDeleteCartDeletesSuccessfully() {
        when(cartRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cartRepository).deleteById(1L);

        boolean result = cartService.deleteCart(1L);

        assertThat(result).isTrue();
    }

    @Test
    void testDeleteCartThrowsWhenNotFound() {
        when(cartRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> cartService.deleteCart(999L))
                .isInstanceOf(ObjectWithIdNotFound.class);
    }
}