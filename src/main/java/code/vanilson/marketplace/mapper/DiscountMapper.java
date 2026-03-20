package code.vanilson.marketplace.mapper;

import code.vanilson.marketplace.dto.CouponDto;
import code.vanilson.marketplace.dto.DiscountDto;
import code.vanilson.marketplace.model.Coupon;
import code.vanilson.marketplace.model.Discount;

import java.util.List;
import java.util.stream.Collectors;

public class DiscountMapper {

    private DiscountMapper() {
    }

    public static DiscountDto toDiscountDto(Discount discount) {
        if (discount == null) {
            return null;
        }

        return new DiscountDto(
                discount.getDiscountId(),
                discount.getName(),
                discount.getDescription(),
                discount.getDiscountType(),
                discount.getDiscountValue(),
                discount.getMinPurchaseAmount(),
                discount.getMaxDiscountAmount(),
                discount.getStartDate(),
                discount.getEndDate(),
                discount.getIsActive(),
                discount.getCreatedAt(),
                discount.getUpdatedAt(),
                discount.getVersion()
        );
    }

    public static List<DiscountDto> toDiscountDtoList(List<Discount> discounts) {
        return discounts.stream()
                .map(DiscountMapper::toDiscountDto)
                .collect(Collectors.toList());
    }

    public static Discount toDiscount(DiscountDto discountDto) {
        if (discountDto == null) {
            return null;
        }

        Discount discount = new Discount();
        discount.setDiscountId(discountDto.getDiscountId());
        discount.setName(discountDto.getName());
        discount.setDescription(discountDto.getDescription());
        discount.setDiscountType(discountDto.getDiscountType());
        discount.setDiscountValue(discountDto.getDiscountValue());
        discount.setMinPurchaseAmount(discountDto.getMinPurchaseAmount() != null ? discountDto.getMinPurchaseAmount() : java.math.BigDecimal.ZERO);
        discount.setMaxDiscountAmount(discountDto.getMaxDiscountAmount());
        discount.setStartDate(discountDto.getStartDate());
        discount.setEndDate(discountDto.getEndDate());
        discount.setIsActive(discountDto.getIsActive() != null ? discountDto.getIsActive() : true);
        discount.setVersion(discountDto.getVersion());

        return discount;
    }

    public static CouponDto toCouponDto(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        CouponDto dto = new CouponDto(
                coupon.getCouponId(),
                coupon.getCode(),
                coupon.getDiscount() != null ? coupon.getDiscount().getDiscountId() : null,
                coupon.getUsageLimit(),
                coupon.getUsageCount()
        );
        dto.setIsActive(coupon.getIsActive());
        dto.setCreatedAt(coupon.getCreatedAt());
        dto.setUpdatedAt(coupon.getUpdatedAt());
        dto.setVersion(coupon.getVersion());
        return dto;
    }

    public static List<CouponDto> toCouponDtoList(List<Coupon> coupons) {
        return coupons.stream()
                .map(DiscountMapper::toCouponDto)
                .collect(Collectors.toList());
    }

    public static Coupon toCoupon(CouponDto couponDto) {
        if (couponDto == null) {
            return null;
        }

        Coupon coupon = new Coupon();
        coupon.setCouponId(couponDto.getCouponId());
        coupon.setCode(couponDto.getCode());
        coupon.setUsageLimit(couponDto.getUsageLimit());
        coupon.setUsageCount(couponDto.getUsageCount() != null ? couponDto.getUsageCount() : 0);
        coupon.setIsActive(couponDto.getIsActive() != null ? couponDto.getIsActive() : true);
        coupon.setVersion(couponDto.getVersion());

        return coupon;
    }
}