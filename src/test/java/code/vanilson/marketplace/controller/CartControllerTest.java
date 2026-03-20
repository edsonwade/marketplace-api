package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CartDto;
import code.vanilson.marketplace.dto.CartItemDto;
import code.vanilson.marketplace.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private ObjectMapper objectMapper;
    private CartDto cartDto;
    private CartItemDto cartItemDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();

        cartDto = new CartDto(1L, 1L, "ACTIVE");
        cartItemDto = new CartItemDto(1L, "Test Product", 2, BigDecimal.valueOf(100));
    }

    @Test
    void testGetAllCartsReturnsOk() throws Exception {
        when(cartService.findAllCarts()).thenReturn(List.of(cartDto));

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(cartService, times(1)).findAllCarts();
    }

    @Test
    void testGetCartByIdReturnsCart() throws Exception {
        when(cartService.findCartById(1L)).thenReturn(Optional.of(cartDto));

        mockMvc.perform(get("/api/carts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(cartService, times(1)).findCartById(1L);
    }

    @Test
    void testGetCartByIdReturnsNotFound() throws Exception {
        when(cartService.findCartById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/carts/999"))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).findCartById(999L);
    }

    @Test
    void testGetCartByCustomerIdReturnsCart() throws Exception {
        when(cartService.findActiveCartByCustomerId(1L)).thenReturn(Optional.of(cartDto));

        mockMvc.perform(get("/api/carts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1));

        verify(cartService, times(1)).findActiveCartByCustomerId(1L);
    }

    @Test
    void testGetCartByCustomerIdReturnsNotFound() throws Exception {
        when(cartService.findActiveCartByCustomerId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/carts/customer/999"))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).findActiveCartByCustomerId(999L);
    }

    @Test
    void testCreateCartReturnsCreated() throws Exception {
        when(cartService.createCart(anyLong())).thenReturn(cartDto);

        mockMvc.perform(post("/api/carts")
                        .param("customerId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(cartService, times(1)).createCart(1L);
    }

    @Test
    void testAddItemToCartReturnsOk() throws Exception {
        when(cartService.addItemToCart(anyLong(), any(CartItemDto.class))).thenReturn(cartDto);

        mockMvc.perform(post("/api/carts/items")
                        .param("customerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemDto)))
                .andExpect(status().isOk());

        verify(cartService, times(1)).addItemToCart(anyLong(), any(CartItemDto.class));
    }

    @Test
    void testUpdateCartItemQuantityReturnsOk() throws Exception {
        when(cartService.updateCartItemQuantity(anyLong(), anyLong(), any(Integer.class))).thenReturn(cartDto);

        mockMvc.perform(put("/api/carts/items/1")
                        .param("customerId", "1")
                        .param("quantity", "5"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).updateCartItemQuantity(anyLong(), anyLong(), any(Integer.class));
    }

    @Test
    void testRemoveItemFromCartReturnsOk() throws Exception {
        when(cartService.removeItemFromCart(anyLong(), anyLong())).thenReturn(cartDto);

        mockMvc.perform(delete("/api/carts/items/1")
                        .param("customerId", "1"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeItemFromCart(anyLong(), anyLong());
    }

    @Test
    void testClearCartReturnsOk() throws Exception {
        when(cartService.clearCart(anyLong())).thenReturn(cartDto);

        mockMvc.perform(delete("/api/carts/clear")
                        .param("customerId", "1"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(anyLong());
    }

    @Test
    void testCheckoutReturnsOk() throws Exception {
        CartDto checkedOutCart = new CartDto(1L, 1L, "CHECKED_OUT");
        when(cartService.checkout(anyLong())).thenReturn(checkedOutCart);

        mockMvc.perform(post("/api/carts/checkout")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        verify(cartService, times(1)).checkout(1L);
    }

    @Test
    void testCheckoutReturnsBadRequestWhenEmptyCart() throws Exception {
        when(cartService.checkout(anyLong())).thenThrow(new IllegalStateException("Cannot checkout empty cart"));

        mockMvc.perform(post("/api/carts/checkout")
                        .param("customerId", "1"))
                .andExpect(status().isBadRequest());

        verify(cartService, times(1)).checkout(1L);
    }

    @Test
    void testDeleteCartReturnsOk() throws Exception {
        when(cartService.deleteCart(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/carts/1"))
                .andExpect(status().isOk());

        verify(cartService, times(1)).deleteCart(1L);
    }

    @Test
    void testDeleteCartReturnsNotFound() throws Exception {
        when(cartService.deleteCart(999L)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(delete("/api/carts/999"))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).deleteCart(999L);
    }
}
