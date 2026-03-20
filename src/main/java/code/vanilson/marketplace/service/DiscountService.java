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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    private static final Logger logger = LogManager.getLogger(DiscountService.class);

    private final DiscountRepository discountRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    public DiscountService(DiscountRepository discountRepository, CouponRepository couponRepository, CouponUsageRepository couponUsageRepository) {
        this.discountRepository = discountRepository;
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
    }

    public List<DiscountDto> findAllDiscounts() {
        return DiscountMapper.toDiscountDtoList(discountRepository.findAll());
    }

    public List<DiscountDto> findActiveDiscounts() {
        return DiscountMapper.toDiscountDtoList(discountRepository.findAllActiveDiscounts(LocalDateTime.now()));
    }

    public Optional<DiscountDto> findDiscountById(Long id) {
        return discountRepository.findById(id)
                .map(DiscountMapper::toDiscountDto);
    }

    @Transactional
    public DiscountDto createDiscount(DiscountDto discountDto) {
        logger.info("Creating discount: {}", discountDto.getName());
        Discount discount = DiscountMapper.toDiscount(discountDto);
        Discount saved = discountRepository.save(discount);
        logger.info("Created discount with id: {}", saved.getDiscountId());
        return DiscountMapper.toDiscountDto(saved);
    }

    @Transactional
    public DiscountDto updateDiscount(DiscountDto discountDto) {
        logger.info("Updating discount: {}", discountDto.getDiscountId());
        Discount discount = discountRepository.findById(discountDto.getDiscountId())
                .orElseThrow(() -> new ObjectWithIdNotFound("Discount not found with id: " + discountDto.getDiscountId()));

        discount.setName(discountDto.getName());
        discount.setDescription(discountDto.getDescription());
        discount.setDiscountType(discountDto.getDiscountType());
        discount.setDiscountValue(discountDto.getDiscountValue());
        discount.setMinPurchaseAmount(discountDto.getMinPurchaseAmount());
        discount.setMaxDiscountAmount(discountDto.getMaxDiscountAmount());
        discount.setStartDate(discountDto.getStartDate());
        discount.setEndDate(discountDto.getEndDate());
        discount.setIsActive(discountDto.getIsActive());

        Discount saved = discountRepository.save(discount);
        logger.info("Updated discount with id: {}", saved.getDiscountId());
        return DiscountMapper.toDiscountDto(saved);
    }

    @Transactional
    public boolean deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new ObjectWithIdNotFound("Discount not found with id: " + id);
        }
        discountRepository.deleteById(id);
        logger.info("Deleted discount with id: {}", id);
        return true;
    }

    public List<CouponDto> findAllCoupons() {
        return DiscountMapper.toCouponDtoList(couponRepository.findAll());
    }

    public List<CouponDto> findActiveCoupons() {
        return DiscountMapper.toCouponDtoList(couponRepository.findByIsActiveTrue());
    }

    public Optional<CouponDto> findCouponById(Long id) {
        return couponRepository.findById(id)
                .map(DiscountMapper::toCouponDto);
    }

    public Optional<CouponDto> findCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .map(DiscountMapper::toCouponDto);
    }

    @Transactional
    public CouponDto createCoupon(CouponDto couponDto, Long discountId) {
        logger.info("Creating coupon: {}", couponDto.getCode());

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new ObjectWithIdNotFound("Discount not found with id: " + discountId));

        if (couponRepository.existsByCode(couponDto.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists: " + couponDto.getCode());
        }

        Coupon coupon = DiscountMapper.toCoupon(couponDto);
        coupon.setDiscount(discount);
        Coupon saved = couponRepository.save(coupon);

        logger.info("Created coupon with id: {}", saved.getCouponId());
        return DiscountMapper.toCouponDto(saved);
    }

    @Transactional
    public CouponDto updateCoupon(CouponDto couponDto) {
        logger.info("Updating coupon: {}", couponDto.getCouponId());
        Coupon coupon = couponRepository.findById(couponDto.getCouponId())
                .orElseThrow(() -> new ObjectWithIdNotFound("Coupon not found with id: " + couponDto.getCouponId()));

        if (couponDto.getCode() != null && !couponDto.getCode().equals(coupon.getCode())) {
            if (couponRepository.existsByCode(couponDto.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists: " + couponDto.getCode());
            }
            coupon.setCode(couponDto.getCode());
        }

        coupon.setUsageLimit(couponDto.getUsageLimit());
        coupon.setIsActive(couponDto.getIsActive());

        Coupon saved = couponRepository.save(coupon);
        logger.info("Updated coupon with id: {}", saved.getCouponId());
        return DiscountMapper.toCouponDto(saved);
    }

    @Transactional
    public boolean deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ObjectWithIdNotFound("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
        logger.info("Deleted coupon with id: {}", id);
        return true;
    }

    @Transactional
    public BigDecimal applyCoupon(String code, BigDecimal amount, Long customerId) {
        logger.info("Applying coupon: {} to amount: {}", code, amount);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ObjectWithIdNotFound("Coupon not found: " + code));

        if (!coupon.canUse()) {
            throw new IllegalStateException("Coupon is not valid or has reached usage limit");
        }

        if (couponUsageRepository.existsByCouponCouponIdAndCustomerId(coupon.getCouponId(), customerId)) {
            throw new IllegalStateException("You have already used this coupon");
        }

        BigDecimal discount = coupon.getDiscount().calculateDiscount(amount);
        
        CouponUsage usage = new CouponUsage(coupon, customerId);
        couponUsageRepository.save(usage);
        
        coupon.incrementUsage();
        couponRepository.save(coupon);

        logger.info("Applied discount: {}", discount);
        return discount;
    }

    public boolean validateCoupon(String code) {
        return couponRepository.findByCode(code)
                .map(Coupon::canUse)
                .orElse(false);
    }
}