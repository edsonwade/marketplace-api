package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CouponDto;
import code.vanilson.marketplace.dto.DiscountDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.service.DiscountService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DiscountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private DiscountController discountController;

    private ObjectMapper objectMapper;
    private DiscountDto discountDto;
    private CouponDto couponDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(discountController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        discountDto = new DiscountDto(1L, "Summer Sale", "Summer discount", "PERCENTAGE", BigDecimal.valueOf(10),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));

        couponDto = new CouponDto(1L, "SUMMER10", 1L, 100, 0);
    }

    @Test
    void testGetAllDiscountsReturnsOk() throws Exception {
        when(discountService.findAllDiscounts()).thenReturn(List.of(discountDto));

        mockMvc.perform(get("/api/v1/discounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(discountService, times(1)).findAllDiscounts();
    }

    @Test
    void testGetActiveDiscountsReturnsOk() throws Exception {
        when(discountService.findActiveDiscounts()).thenReturn(List.of(discountDto));

        mockMvc.perform(get("/api/v1/discounts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(discountService, times(1)).findActiveDiscounts();
    }

    @Test
    void testGetDiscountByIdReturnsDiscount() throws Exception {
        when(discountService.findDiscountById(1L)).thenReturn(Optional.of(discountDto));

        mockMvc.perform(get("/api/v1/discounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(discountService, times(1)).findDiscountById(1L);
    }

    @Test
    void testGetDiscountByIdReturnsNotFound() throws Exception {
        when(discountService.findDiscountById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/discounts/999"))
                .andExpect(status().isNotFound());

        verify(discountService, times(1)).findDiscountById(999L);
    }

    @Test
    void testCreateDiscountReturnsCreated() throws Exception {
        when(discountService.createDiscount(any(DiscountDto.class))).thenReturn(discountDto);

        mockMvc.perform(post("/api/v1/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(discountService, times(1)).createDiscount(any(DiscountDto.class));
    }

    @Test
    void testUpdateDiscountReturnsOk() throws Exception {
        when(discountService.updateDiscount(any(DiscountDto.class))).thenReturn(discountDto);

        mockMvc.perform(put("/api/v1/discounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(discountService, times(1)).updateDiscount(any(DiscountDto.class));
    }

    @Test
    void testDeleteDiscountReturnsOk() throws Exception {
        when(discountService.deleteDiscount(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/discounts/1"))
                .andExpect(status().isOk());

        verify(discountService, times(1)).deleteDiscount(1L);
    }

    @Test
    void testDeleteDiscountReturnsNotFound() throws Exception {
        when(discountService.deleteDiscount(999L)).thenThrow(new ObjectWithIdNotFound("Discount not found"));

        mockMvc.perform(delete("/api/v1/discounts/999"))
                .andExpect(status().isNotFound());

        verify(discountService, times(1)).deleteDiscount(999L);
    }

    @Test
    void testGetAllCouponsReturnsOk() throws Exception {
        when(discountService.findAllCoupons()).thenReturn(List.of(couponDto));

        mockMvc.perform(get("/api/v1/discounts/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(discountService, times(1)).findAllCoupons();
    }

    @Test
    void testGetActiveCouponsReturnsOk() throws Exception {
        when(discountService.findActiveCoupons()).thenReturn(List.of(couponDto));

        mockMvc.perform(get("/api/v1/discounts/coupons/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(discountService, times(1)).findActiveCoupons();
    }

    @Test
    void testGetCouponByIdReturnsCoupon() throws Exception {
        when(discountService.findCouponById(1L)).thenReturn(Optional.of(couponDto));

        mockMvc.perform(get("/api/v1/discounts/coupons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(discountService, times(1)).findCouponById(1L);
    }

    @Test
    void testGetCouponByIdReturnsNotFound() throws Exception {
        when(discountService.findCouponById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/discounts/coupons/999"))
                .andExpect(status().isNotFound());

        verify(discountService, times(1)).findCouponById(999L);
    }

    @Test
    void testGetCouponByCodeReturnsCoupon() throws Exception {
        when(discountService.findCouponByCode("SUMMER10")).thenReturn(Optional.of(couponDto));

        mockMvc.perform(get("/api/v1/discounts/coupons/code/SUMMER10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUMMER10"));

        verify(discountService, times(1)).findCouponByCode("SUMMER10");
    }

    @Test
    void testGetCouponByCodeReturnsNotFound() throws Exception {
        when(discountService.findCouponByCode("INVALID")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/discounts/coupons/code/INVALID"))
                .andExpect(status().isNotFound());

        verify(discountService, times(1)).findCouponByCode("INVALID");
    }

    @Test
    void testCreateCouponReturnsCreated() throws Exception {
        when(discountService.createCoupon(any(CouponDto.class), eq(1L))).thenReturn(couponDto);

        mockMvc.perform(post("/api/v1/discounts/coupons")
                        .param("discountId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(discountService, times(1)).createCoupon(any(CouponDto.class), eq(1L));
    }

    @Test
    void testUpdateCouponReturnsOk() throws Exception {
        when(discountService.updateCoupon(any(CouponDto.class))).thenReturn(couponDto);

        mockMvc.perform(put("/api/v1/discounts/coupons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(discountService, times(1)).updateCoupon(any(CouponDto.class));
    }

    @Test
    void testDeleteCouponReturnsOk() throws Exception {
        when(discountService.deleteCoupon(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/discounts/coupons/1"))
                .andExpect(status().isOk());

        verify(discountService, times(1)).deleteCoupon(1L);
    }

    @Test
    void testDeleteCouponReturnsNotFound() throws Exception {
        when(discountService.deleteCoupon(999L)).thenThrow(new ObjectWithIdNotFound("Coupon not found"));

        mockMvc.perform(delete("/api/v1/discounts/coupons/999"))
                .andExpect(status().isNotFound());

        verify(discountService, times(1)).deleteCoupon(999L);
    }

    @Test
    void testValidateCouponReturnsOk() throws Exception {
        when(discountService.validateCoupon("SUMMER10")).thenReturn(true);

        mockMvc.perform(post("/api/v1/discounts/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "SUMMER10"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        verify(discountService, times(1)).validateCoupon("SUMMER10");
    }

    @Test
    void testApplyCouponReturnsOk() throws Exception {
        BigDecimal discountAmount = BigDecimal.valueOf(10);
        when(discountService.applyCoupon("SUMMER10", BigDecimal.valueOf(100), 1L)).thenReturn(discountAmount);

        mockMvc.perform(post("/api/v1/discounts/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "SUMMER10",
                                "amount", 100,
                                "customerId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount").value(10));

        verify(discountService, times(1)).applyCoupon("SUMMER10", BigDecimal.valueOf(100), 1L);
    }

    @Test
    void testApplyCouponReturnsBadRequest() throws Exception {
        when(discountService.applyCoupon(anyString(), any(BigDecimal.class), anyLong()))
                .thenThrow(new IllegalStateException("Coupon expired"));

        mockMvc.perform(post("/api/v1/discounts/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "EXPIRED",
                                "amount", 100,
                                "customerId", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Coupon expired"));

        verify(discountService, times(1)).applyCoupon("EXPIRED", BigDecimal.valueOf(100), 1L);
    }
}
