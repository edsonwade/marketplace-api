package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.CartMapper;
import code.vanilson.marketplace.model.Cart;
import code.vanilson.marketplace.model.CartItem;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.repository.CartItemRepository;
import code.vanilson.marketplace.repository.CartRepository;
import code.vanilson.marketplace.repository.ProductRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger logger = LogManager.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartDto> findAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        return CartMapper.toCartDtoList(carts);
    }

    public Optional<CartDto> findCartById(Long id) {
        return cartRepository.findById(id)
                .map(CartMapper::toCartDto);
    }

    public Optional<CartDto> findCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .map(CartMapper::toCartDto);
    }

    public Optional<CartDto> findActiveCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .map(CartMapper::toCartDto);
    }

    @Transactional
    public CartDto createCart(Long customerId) {
        logger.info("Creating new cart for customer: {}", customerId);

        Optional<Cart> existingCart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE");
        if (existingCart.isPresent()) {
            logger.info("Active cart already exists for customer: {}", customerId);
            return CartMapper.toCartDto(existingCart.get());
        }

        Cart cart = new Cart(customerId, "ACTIVE");
        Cart savedCart = cartRepository.save(cart);
        logger.info("Created new cart with id: {}", savedCart.getCartId());
        return CartMapper.toCartDto(savedCart);
    }

    @Transactional
    public CartDto addItemToCart(Long customerId, CartItemDto cartItemDto) {
        logger.info("Adding item to cart for customer: {}, product: {}", customerId, cartItemDto.getProductId());

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseGet(() -> {
                    Cart newCart = new Cart(customerId, "ACTIVE");
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(cartItemDto.getProductId())
                .orElseThrow(() -> new ObjectWithIdNotFound("Product not found with id: " + cartItemDto.getProductId()));

        Optional<CartItem> existingItem = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), cartItemDto.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItemDto.getQuantity());
            item.setPrice(product.getQuantity() != null ? BigDecimal.valueOf(product.getQuantity().doubleValue()) : BigDecimal.ZERO);
            cartItemRepository.save(item);
            logger.info("Updated quantity for existing cart item: {}", item.getCartItemId());
        } else {
            CartItem newItem = new CartItem(cart, product.getProductId(), product.getName(), cartItemDto.getQuantity(),
                    product.getQuantity() != null ? BigDecimal.valueOf(product.getQuantity().doubleValue()) : BigDecimal.ZERO);
            cart.addItem(newItem);
            logger.info("Added new cart item to cart: {}", cart.getCartId());
        }

        cartRepository.save(cart);
        return CartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto updateCartItemQuantity(Long customerId, Long productId, Integer quantity) {
        logger.info("Updating cart item quantity for customer: {}, product: {}, quantity: {}", customerId, productId, quantity);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseThrow(() -> new ObjectWithIdNotFound("Active cart not found for customer: " + customerId));

        CartItem cartItem = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new ObjectWithIdNotFound("Cart item not found for product: " + productId));

        if (quantity <= 0) {
            cart.removeItem(cartItem);
            cartItemRepository.delete(cartItem);
            logger.info("Removed cart item from cart: {}", cartItem.getCartItemId());
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            logger.info("Updated cart item quantity: {}", cartItem.getCartItemId());
        }

        cart.recalculateTotal();
        cartRepository.save(cart);
        return CartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto removeItemFromCart(Long customerId, Long productId) {
        logger.info("Removing item from cart for customer: {}, product: {}", customerId, productId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseThrow(() -> new ObjectWithIdNotFound("Active cart not found for customer: " + customerId));

        CartItem cartItem = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new ObjectWithIdNotFound("Cart item not found for product: " + productId));

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cart.recalculateTotal();
        cartRepository.save(cart);

        logger.info("Removed item from cart successfully");
        return CartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto clearCart(Long customerId) {
        logger.info("Clearing cart for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseThrow(() -> new ObjectWithIdNotFound("Active cart not found for customer: " + customerId));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);

        logger.info("Cart cleared successfully");
        return CartMapper.toCartDto(cart);
    }

    @Transactional
    public CartDto checkout(Long customerId) {
        logger.info("Checking out cart for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseThrow(() -> new ObjectWithIdNotFound("Active cart not found for customer: " + customerId));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout empty cart");
        }

        cart.setStatus("CHECKED_OUT");
        Cart checkedOutCart = cartRepository.save(cart);

        logger.info("Cart checked out successfully, cart id: {}", checkedOutCart.getCartId());
        return CartMapper.toCartDto(checkedOutCart);
    }

    @Transactional
    public boolean deleteCart(Long id) {
        if (!cartRepository.existsById(id)) {
            throw new ObjectWithIdNotFound("Cart not found with id: " + id);
        }
        cartRepository.deleteById(id);
        logger.info("Deleted cart with id: {}", id);
        return true;
    }
}