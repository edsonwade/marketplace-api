package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CouponDto;
import code.vanilson.marketplace.dto.DiscountDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.DiscountMapper;
import code.vanilson.marketplace.model.Coupon;
import code.vanilson.marketplace.model.CouponUsage;
import code.vanilson.marketplace.model.Discount;
import code.vanilson.marketplace.repository.CouponRepository;
import code.vanilson.marketplace.repository.CouponUsageRepository;
import code.vanilson.marketplace.repository.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private DiscountService discountService;

    private Discount discount;
    private DiscountDto discountDto;
    private Coupon coupon;
    private CouponDto couponDto;

    @BeforeEach
    void setUp() {
        discount = new Discount("Summer Sale", "Summer discount", "PERCENTAGE", BigDecimal.valueOf(10));
        discount.setDiscountId(1L);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setEndDate(LocalDateTime.now().plusDays(30));
        discount.setIsActive(true);

        discountDto = new DiscountDto(1L, "Summer Sale", "Summer discount", "PERCENTAGE", BigDecimal.valueOf(10),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));

        coupon = new Coupon("SUMMER10", discount);
        coupon.setCouponId(1L);
        coupon.setUsageLimit(100);
        coupon.setUsageCount(0);
        coupon.setIsActive(true);

        couponDto = new CouponDto(1L, "SUMMER10", 1L, 100, 0);
    }

    @Test
    void testFindAllDiscountsReturnsList() {
        when(discountRepository.findAll()).thenReturn(List.of(discount));

        List<DiscountDto> result = discountService.findAllDiscounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Summer Sale");
        verify(discountRepository, times(1)).findAll();
    }

    @Test
    void testFindActiveDiscountsReturnsList() {
        when(discountRepository.findAllActiveDiscounts(any())).thenReturn(List.of(discount));

        List<DiscountDto> result = discountService.findActiveDiscounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(discountRepository, times(1)).findAllActiveDiscounts(any());
    }

    @Test
    void testFindDiscountByIdReturnsDiscount() {
        when(discountRepository.findById(1L)).thenReturn(Optional.of(discount));

        Optional<DiscountDto> result = discountService.findDiscountById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountId()).isEqualTo(1L);
        verify(discountRepository, times(1)).findById(1L);
    }

    @Test
    void testFindDiscountByIdReturnsEmpty() {
        when(discountRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<DiscountDto> result = discountService.findDiscountById(999L);

        assertThat(result).isEmpty();
        verify(discountRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateDiscountReturnsCreated() {
        when(discountRepository.save(any(Discount.class))).thenReturn(discount);

        DiscountDto result = discountService.createDiscount(discountDto);

        assertThat(result.getDiscountId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Summer Sale");
        verify(discountRepository, times(1)).save(any(Discount.class));
    }

    @Test
    void testUpdateDiscountReturnsUpdated() {
        DiscountDto updateDto = new DiscountDto(1L, "Updated Sale", "Updated description", "FIXED", BigDecimal.valueOf(20),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        when(discountRepository.findById(1L)).thenReturn(Optional.of(discount));
        when(discountRepository.save(any(Discount.class))).thenReturn(discount);

        DiscountDto result = discountService.updateDiscount(updateDto);

        assertThat(result).isNotNull();
        verify(discountRepository, times(1)).findById(1L);
        verify(discountRepository, times(1)).save(any(Discount.class));
    }

    @Test
    void testUpdateDiscountThrowsWhenNotFound() {
        DiscountDto updateDto = new DiscountDto(999L, "Updated Sale", "Updated description", "FIXED", BigDecimal.valueOf(20),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        when(discountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.updateDiscount(updateDto))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Discount not found");

        verify(discountRepository, times(1)).findById(999L);
        verify(discountRepository, never()).save(any());
    }

    @Test
    void testDeleteDiscountReturnsTrue() {
        when(discountRepository.existsById(1L)).thenReturn(true);
        doNothing().when(discountRepository).deleteById(1L);

        boolean result = discountService.deleteDiscount(1L);

        assertThat(result).isTrue();
        verify(discountRepository, times(1)).existsById(1L);
        verify(discountRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteDiscountThrowsWhenNotFound() {
        when(discountRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> discountService.deleteDiscount(999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Discount not found");

        verify(discountRepository, times(1)).existsById(999L);
        verify(discountRepository, never()).deleteById(any());
    }

    @Test
    void testFindAllCouponsReturnsList() {
        when(couponRepository.findAll()).thenReturn(List.of(coupon));

        List<CouponDto> result = discountService.findAllCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("SUMMER10");
        verify(couponRepository, times(1)).findAll();
    }

    @Test
    void testFindActiveCouponsReturnsList() {
        when(couponRepository.findByIsActiveTrue()).thenReturn(List.of(coupon));

        List<CouponDto> result = discountService.findActiveCoupons();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(couponRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testFindCouponByIdReturnsCoupon() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        Optional<CouponDto> result = discountService.findCouponById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCouponId()).isEqualTo(1L);
        verify(couponRepository, times(1)).findById(1L);
    }

    @Test
    void testFindCouponByIdReturnsEmpty() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CouponDto> result = discountService.findCouponById(999L);

        assertThat(result).isEmpty();
        verify(couponRepository, times(1)).findById(999L);
    }

    @Test
    void testFindCouponByCodeReturnsCoupon() {
        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));

        Optional<CouponDto> result = discountService.findCouponByCode("SUMMER10");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("SUMMER10");
        verify(couponRepository, times(1)).findByCode("SUMMER10");
    }

    @Test
    void testFindCouponByCodeReturnsEmpty() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        Optional<CouponDto> result = discountService.findCouponByCode("INVALID");

        assertThat(result).isEmpty();
        verify(couponRepository, times(1)).findByCode("INVALID");
    }

    @Test
    void testCreateCouponReturnsCreated() {
        CouponDto newCouponDto = new CouponDto(null, "NEWCODE", 1L, 50, 0);
        when(discountRepository.findById(1L)).thenReturn(Optional.of(discount));
        when(couponRepository.existsByCode("NEWCODE")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        CouponDto result = discountService.createCoupon(newCouponDto, 1L);

        assertThat(result).isNotNull();
        verify(discountRepository, times(1)).findById(1L);
        verify(couponRepository, times(1)).existsByCode("NEWCODE");
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testCreateCouponThrowsWhenDiscountNotFound() {
        CouponDto newCouponDto = new CouponDto(null, "NEWCODE", 999L, 50, 0);
        when(discountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.createCoupon(newCouponDto, 999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Discount not found");

        verify(discountRepository, times(1)).findById(999L);
        verify(couponRepository, never()).save(any());
    }

    @Test
    void testCreateCouponThrowsWhenCodeExists() {
        CouponDto newCouponDto = new CouponDto(null, "SUMMER10", 1L, 50, 0);
        when(discountRepository.findById(1L)).thenReturn(Optional.of(discount));
        when(couponRepository.existsByCode("SUMMER10")).thenReturn(true);

        assertThatThrownBy(() -> discountService.createCoupon(newCouponDto, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon code already exists");

        verify(couponRepository, times(1)).existsByCode("SUMMER10");
        verify(couponRepository, never()).save(any());
    }

    @Test
    void testUpdateCouponReturnsUpdated() {
        CouponDto updateDto = new CouponDto(1L, "UPDATED", 1L, 200, 0);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.existsByCode("UPDATED")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        CouponDto result = discountService.updateCoupon(updateDto);

        assertThat(result).isNotNull();
        verify(couponRepository, times(1)).findById(1L);
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testUpdateCouponThrowsWhenNotFound() {
        CouponDto updateDto = new CouponDto(999L, "UPDATED", 1L, 200, 0);
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.updateCoupon(updateDto))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Coupon not found");

        verify(couponRepository, times(1)).findById(999L);
        verify(couponRepository, never()).save(any());
    }

    @Test
    void testDeleteCouponReturnsTrue() {
        when(couponRepository.existsById(1L)).thenReturn(true);
        doNothing().when(couponRepository).deleteById(1L);

        boolean result = discountService.deleteCoupon(1L);

        assertThat(result).isTrue();
        verify(couponRepository, times(1)).existsById(1L);
        verify(couponRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCouponThrowsWhenNotFound() {
        when(couponRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> discountService.deleteCoupon(999L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Coupon not found");

        verify(couponRepository, times(1)).existsById(999L);
        verify(couponRepository, never()).deleteById(any());
    }

    @Test
    void testApplyCouponReturnsDiscount() {
        BigDecimal amount = BigDecimal.valueOf(100);
        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCouponCouponIdAndCustomerId(1L, 1L)).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);

        BigDecimal result = discountService.applyCoupon("SUMMER10", amount, 1L);

        assertThat(result).isNotNull();
        assertThat(result.compareTo(BigDecimal.ZERO)).isGreaterThan(0);
        verify(couponRepository, times(1)).findByCode("SUMMER10");
        verify(couponUsageRepository, times(1)).existsByCouponCouponIdAndCustomerId(1L, 1L);
        verify(couponUsageRepository, times(1)).save(any(CouponUsage.class));
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void testApplyCouponThrowsWhenCouponNotFound() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> discountService.applyCoupon("INVALID", BigDecimal.valueOf(100), 1L))
                .isInstanceOf(ObjectWithIdNotFound.class)
                .hasMessageContaining("Coupon not found");

        verify(couponRepository, times(1)).findByCode("INVALID");
        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void testApplyCouponThrowsWhenCouponNotValid() {
        coupon.setIsActive(false);
        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> discountService.applyCoupon("SUMMER10", BigDecimal.valueOf(100), 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Coupon is not valid");

        verify(couponRepository, times(1)).findByCode("SUMMER10");
        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void testApplyCouponThrowsWhenAlreadyUsed() {
        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));
        when(couponUsageRepository.existsByCouponCouponIdAndCustomerId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> discountService.applyCoupon("SUMMER10", BigDecimal.valueOf(100), 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already used");

        verify(couponUsageRepository, times(1)).existsByCouponCouponIdAndCustomerId(1L, 1L);
        verify(couponUsageRepository, never()).save(any());
    }

    @Test
    void testValidateCouponReturnsTrue() {
        when(couponRepository.findByCode("SUMMER10")).thenReturn(Optional.of(coupon));

        boolean result = discountService.validateCoupon("SUMMER10");

        assertThat(result).isTrue();
        verify(couponRepository, times(1)).findByCode("SUMMER10");
    }

    @Test
    void testValidateCouponReturnsFalse() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        boolean result = discountService.validateCoupon("INVALID");

        assertThat(result).isFalse();
        verify(couponRepository, times(1)).findByCode("INVALID");
    }
}
