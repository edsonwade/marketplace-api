package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.*;
import code.vanilson.marketplace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Mock private CartRepository     cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository  productRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private OrderRepository    orderRepository;
    @Mock private StockRepository    stockRepository;
    @Mock private NotificationProducer notificationProducer;
    @Mock private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private CartService cartService;

    private Cart     cart;
    private Product  product;
    private CartItem cartItem;
    private Customer customer;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartRepository, cartItemRepository,
                productRepository, customerRepository, orderRepository, stockRepository,
                notificationProducer, objectMapper);

        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setQuantity(10);
        product.setPrice(BigDecimal.valueOf(9.99));

        cart = new Cart();
        cart.setCartId(1L);
        cart.setCustomerId(1L);
        cart.setStatus("ACTIVE");
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(BigDecimal.ZERO);

        cartItem = new CartItem();
        cartItem.setCartItemId(1L);
        cartItem.setCart(cart);
        cartItem.setProductId(1L);
        cartItem.setProductName("Test Product");
        cartItem.setQuantity(2);
        cartItem.setPrice(BigDecimal.valueOf(9.99));

        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setAddress("123 Test St");
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void testFindAllCartsReturnsAllCarts() {
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        List<CartDto> result = cartService.findAllCarts();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(1L);
    }

    @Test
    void testFindAllCartsReturnsEmptyList() {
        when(cartRepository.findAll()).thenReturn(new ArrayList<>());
        assertThat(cartService.findAllCarts()).isEmpty();
    }

    // ── findById ─────────────────────────────────────────────────────────────

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
        assertThat(cartService.findCartById(999L)).isEmpty();
    }

    // ── findActiveByCustomer ─────────────────────────────────────────────────

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
        assertThat(cartService.findActiveCartByCustomerId(1L)).isEmpty();
    }

    // ── createCart ───────────────────────────────────────────────────────────

    @Test
    void testCreateCartCreatesNewCart() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
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

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    void testAddItemToCartAddsNewItem() {
        CartItemDto itemDto = new CartItemDto(1L, "Test Product", 2, BigDecimal.valueOf(9.99));
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        // Stock check: findByCartCartIdAndProductId returns empty (0 already in cart)
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.addItemToCart(1L, itemDto);
        assertThat(result).isNotNull();
    }

    @Test
    void testAddItemToCartThrowsWhenOutOfStock() {
        product.setQuantity(0); // out of stock
        CartItemDto itemDto = new CartItemDto(1L, "Test Product", 1, BigDecimal.valueOf(9.99));
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItemToCart(1L, itemDto))
                .isInstanceOf(code.vanilson.marketplace.exception.BadRequestException.class)
                .hasMessageContaining("out of stock");
    }

    @Test
    void testAddItemToCartThrowsWhenNotEnoughStock() {
        product.setQuantity(3); // only 3 available
        CartItemDto itemDto = new CartItemDto(1L, "Test Product", 5, BigDecimal.valueOf(9.99)); // requesting 5
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(1L, itemDto))
                .isInstanceOf(code.vanilson.marketplace.exception.BadRequestException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void testAddItemToCartThrowsWhenProductNotFound() {
        CartItemDto itemDto = new CartItemDto(999L, "Test Product", 2, BigDecimal.valueOf(9.99));
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItemToCart(1L, itemDto))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void testAddItemToCartUpdatesQuantityWhenItemExists() {
        cart.getItems().add(cartItem);
        CartItemDto itemDto = new CartItemDto(1L, "Test Product", 3, BigDecimal.valueOf(9.99));
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.addItemToCart(1L, itemDto);
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    // ── updateQuantity ────────────────────────────────────────────────────────


    @Test
    void testUpdateCartItemQuantityUpdatesQuantity() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateCartItemQuantity(1L, 1L, 5);
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
    void testUpdateCartItemQuantityThrowsWhenExceedsStock() {
        product.setQuantity(3); // only 3 available
        cart.getItems().add(cartItem);
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartCartIdAndProductId(1L, 1L)).thenReturn(Optional.of(cartItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.updateCartItemQuantity(1L, 1L, 10))
                .isInstanceOf(code.vanilson.marketplace.exception.BadRequestException.class)
                .hasMessageContaining("Not enough stock");
    }

    // ── removeItem ────────────────────────────────────────────────────────────

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

    // ── clearCart ─────────────────────────────────────────────────────────────

    @Test
    void testClearCartClearsAllItems() {
        cart.getItems().add(cartItem);
        cart.setTotalAmount(BigDecimal.valueOf(19.98));
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDto result = cartService.clearCart(1L);
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
    }

    // ── checkout ──────────────────────────────────────────────────────────────

    @Test
    void testCheckoutChangesStatusToCheckedOut() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

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

    // ── checkoutAndCreateOrder ────────────────────────────────────────────────

    @Test
    void testCheckoutAndCreateOrderCreatesOrderFromCart() {
        cart.getItems().add(cartItem);
        cart.setTotalAmount(BigDecimal.valueOf(19.98));

        Order savedOrder = new Order();
        savedOrder.setOrderId(10L);
        savedOrder.setCustomer(customer);
        savedOrder.setOrderItems(new java.util.HashSet<>());

        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto result = cartService.checkoutAndCreateOrder(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(10L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(19.98));
        verify(orderRepository, atLeast(1)).save(any(Order.class));
    }

    @Test
    void testCheckoutAndCreateOrderThrowsWhenCartEmpty() {
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        assertThatThrownBy(() -> cartService.checkoutAndCreateOrder(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    void testCheckoutAndCreateOrderThrowsWhenCustomerNotFound() {
        cart.getItems().add(cartItem);
        when(cartRepository.findByCustomerIdAndStatus(1L, "ACTIVE")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.checkoutAndCreateOrder(1L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Customer not found");
    }

    // ── deleteCart ────────────────────────────────────────────────────────────

    @Test
    void testDeleteCartDeletesSuccessfully() {
        when(cartRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cartRepository).deleteById(1L);
        assertThat(cartService.deleteCart(1L)).isTrue();
    }

    @Test
    void testDeleteCartThrowsWhenNotFound() {
        when(cartRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> cartService.deleteCart(999L))
                .isInstanceOf(ObjectWithIdNotFound.class);
    }
}
