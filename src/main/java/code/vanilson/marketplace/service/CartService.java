package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.dto.OrderDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.CartMapper;
import code.vanilson.marketplace.mapper.CustomerMapper;
import code.vanilson.marketplace.exception.BadRequestException;
import code.vanilson.marketplace.model.*;
import code.vanilson.marketplace.repository.*;
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
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final NotificationProducer notificationProducer;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository, CustomerRepository customerRepository,
                       OrderRepository orderRepository, StockRepository stockRepository,
                       NotificationProducer notificationProducer,
                       com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.stockRepository = stockRepository;
        this.notificationProducer = notificationProducer;
        this.objectMapper = objectMapper;
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

        // Force-load lazy items collection BEFORE modifying it.
        int existingCount = cart.getItems().size();
        logger.debug("Cart {} has {} existing items", cart.getCartId(), existingCount);

        Product product = productRepository.findById(cartItemDto.getProductId())
                .orElseThrow(() -> new ObjectWithIdNotFound("Product not found with id: " + cartItemDto.getProductId()));

        // ── Stock check ──────────────────────────────────────────────────────
        // Use product.quantity as source of truth (shown in the UI)
        int available = product.getQuantity() != null ? product.getQuantity() : 0;
        if (available <= 0) {
            throw new BadRequestException("Product '" + product.getName() + "' is out of stock");
        }
        int requested = cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1;
        // Account for quantity already in cart
        int alreadyInCart = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), product.getProductId())
                .map(CartItem::getQuantity).orElse(0);
        if (alreadyInCart + requested > available) {
            throw new BadRequestException("Not enough stock for '" + product.getName() +
                    "'. Available: " + available + ", already in cart: " + alreadyInCart);
        }
        // ─────────────────────────────────────────────────────────────────────

        BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.valueOf(9.99);

        Optional<CartItem> existingItem = cartItemRepository.findByCartCartIdAndProductId(cart.getCartId(), cartItemDto.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItemDto.getQuantity());
            item.setPrice(unitPrice);
            cartItemRepository.save(item);
            logger.info("Updated quantity for existing cart item: {}", item.getCartItemId());
        } else {
            CartItem newItem = new CartItem(cart, product.getProductId(), product.getName(), cartItemDto.getQuantity(), unitPrice);
            cart.addItem(newItem);
            logger.info("Added new cart item to cart: {}", cart.getCartId());
        }

        // Always recalculate before saving — covers both new and updated items
        cart.recalculateTotal();
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
            // Verify new quantity does not exceed available stock
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ObjectWithIdNotFound("Product not found: " + productId));
            int available = product.getQuantity() != null ? product.getQuantity() : 0;
            if (quantity > available) {
                throw new BadRequestException("Not enough stock for '" + product.getName() +
                        "'. Available: " + available);
            }
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

    /**
     * Checkout cart AND automatically create an Order.
     * Returns the created OrderDto so the frontend can redirect to payment.
     */
    @Transactional
    public OrderDto checkoutAndCreateOrder(Long customerId) {
        logger.info("Checkout + create order for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .orElseThrow(() -> new ObjectWithIdNotFound("Active cart not found for customer: " + customerId));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        // Mark cart as checked out
        cart.setStatus("CHECKED_OUT");
        cartRepository.save(cart);

        // Resolve customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ObjectWithIdNotFound("Customer not found: " + customerId));

        // Build Order entity
        Order order = new Order();
        order.setLocalDateTime(java.time.LocalDateTime.now());
        order.setCustomer(customer);
        order.setOrderItems(new java.util.HashSet<>());

        Order savedOrder = orderRepository.save(order);

        // Convert each CartItem → OrderItem
        for (CartItem ci : cart.getItems()) {
            Product product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new ObjectWithIdNotFound("Product not found: " + ci.getProductId()));
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setProduct(product);
            oi.setQuantity(ci.getQuantity());
            savedOrder.getOrderItems().add(oi);
        }

        Order finalOrder = orderRepository.save(savedOrder);
        logger.info("Order {} created from cart {} for customer {}", finalOrder.getOrderId(), cart.getCartId(), customerId);

        // ── Publish order-created event to Kafka ────────────────────────────
        try {
            java.util.Map<String, String> event = new java.util.HashMap<>();
            event.put("email", customer.getEmail());
            event.put("subject", "Order Confirmation - Order #" + finalOrder.getOrderId());
            event.put("message", String.format(
                    "Dear %s,%n%nYour order #%d has been confirmed with %d item(s).%n%nTotal: $%s USD%n%nThank you for shopping with us!",
                    customer.getName(), finalOrder.getOrderId(),
                    finalOrder.getOrderItems().size(), cart.getTotalAmount()));
            event.put("type", "ORDER_CREATED");
            notificationProducer.sendOrderNotification(
                    String.valueOf(finalOrder.getOrderId()),
                    objectMapper.writeValueAsString(event));
            logger.info("Order notification queued via Kafka for order {}", finalOrder.getOrderId());
        } catch (Exception e) {
            logger.warn("Failed to queue order notification: {}", e.getMessage());
        }

        // Map to DTO
        OrderDto dto = new OrderDto();
        dto.setOrderId(finalOrder.getOrderId());
        dto.setLocalDateTime(finalOrder.getLocalDateTime());
        dto.setCustomer(CustomerMapper.toCustomerDto(customer));
        dto.setOrderItems(new java.util.ArrayList<>(finalOrder.getOrderItems()));
        // Attach cart total so frontend/payment knows the amount
        dto.setTotalAmount(cart.getTotalAmount());
        return dto;
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