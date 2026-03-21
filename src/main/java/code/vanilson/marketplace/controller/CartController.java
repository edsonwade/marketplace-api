package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "Cart Management", description = "APIs for managing shopping carts")
public class CartController {

    private static final Logger logger = LogManager.getLogger(CartController.class);
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartDto>> getAllCarts() {
        return ResponseEntity.ok().body(cartService.findAllCarts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartById(@PathVariable Long id) {
        return cartService.findCartById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCartByCustomerId(@PathVariable Long customerId) {
        Optional<CartDto> cart = cartService.findActiveCartByCustomerId(customerId);
        return cart.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CartDto> createCart(@RequestParam Long customerId) {
        logger.info("Creating cart for customer: {}", customerId);
        CartDto cart = cartService.createCart(customerId);
        try {
            return ResponseEntity
                    .created(new URI("/api/carts/" + cart.getCartId()))
                    .body(cart);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(@RequestParam Long customerId, @RequestBody CartItemDto cartItemDto) {
        logger.info("Adding item to cart for customer: {}", customerId);
        CartDto cart = cartService.addItemToCart(customerId, cartItemDto);
        return ResponseEntity.ok().body(cart);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateCartItemQuantity(
            @RequestParam Long customerId,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        logger.info("Updating cart item quantity for customer: {}, product: {}, quantity: {}", customerId, productId, quantity);
        CartDto cart = cartService.updateCartItemQuantity(customerId, productId, quantity);
        return ResponseEntity.ok().body(cart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItemFromCart(
            @RequestParam Long customerId,
            @PathVariable Long productId) {
        logger.info("Removing item from cart for customer: {}, product: {}", customerId, productId);
        CartDto cart = cartService.removeItemFromCart(customerId, productId);
        return ResponseEntity.ok().body(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDto> clearCart(@RequestParam Long customerId) {
        logger.info("Clearing cart for customer: {}", customerId);
        CartDto cart = cartService.clearCart(customerId);
        return ResponseEntity.ok().body(cart);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestParam Long customerId) {
        logger.info("Checking out cart for customer: {}", customerId);
        try {
            CartDto cart = cartService.checkout(customerId);
            return ResponseEntity.ok().body(cart);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCart(@PathVariable Long id) {
        logger.info("Deleting cart with id: {}", id);
        try {
            cartService.deleteCart(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}