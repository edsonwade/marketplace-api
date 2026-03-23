package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CouponDto;
import code.vanilson.marketplace.dto.DiscountDto;
import code.vanilson.marketplace.service.DiscountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/discounts")
public class DiscountController {

    private static final Logger logger = LogManager.getLogger(DiscountController.class);
    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<List<DiscountDto>> getAllDiscounts() {
        return ResponseEntity.ok().body(discountService.findAllDiscounts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DiscountDto>> getActiveDiscounts() {
        return ResponseEntity.ok().body(discountService.findActiveDiscounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDiscountById(@PathVariable Long id) {
        return discountService.findDiscountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<DiscountDto> createDiscount(@RequestBody DiscountDto discountDto) {
        logger.info("Creating discount: {}", discountDto.getName());
        DiscountDto saved = discountService.createDiscount(discountDto);
        try {
            return ResponseEntity
                    .created(new URI("/api/discounts/" + saved.getDiscountId()))
                    .body(saved);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<DiscountDto> updateDiscount(@PathVariable Long id, @RequestBody DiscountDto discountDto) {
        discountDto.setDiscountId(id);
        return ResponseEntity.ok().body(discountService.updateDiscount(discountDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDiscount(@PathVariable Long id) {
        try {
            discountService.deleteDiscount(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponDto>> getAllCoupons() {
        return ResponseEntity.ok().body(discountService.findAllCoupons());
    }

    @GetMapping("/coupons/active")
    public ResponseEntity<List<CouponDto>> getActiveCoupons() {
        return ResponseEntity.ok().body(discountService.findActiveCoupons());
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable Long id) {
        return discountService.findCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/coupons/code/{code}")
    public ResponseEntity<?> getCouponByCode(@PathVariable String code) {
        return discountService.findCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/coupons")
    public ResponseEntity<CouponDto> createCoupon(@RequestBody CouponDto couponDto, @RequestParam Long discountId) {
        logger.info("Creating coupon: {}", couponDto.getCode());
        CouponDto saved = discountService.createCoupon(couponDto, discountId);
        try {
            return ResponseEntity
                    .created(new URI("/api/discounts/coupons/" + saved.getCouponId()))
                    .body(saved);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/coupons/{id}")
    public ResponseEntity<CouponDto> updateCoupon(@PathVariable Long id, @RequestBody CouponDto couponDto) {
        couponDto.setCouponId(id);
        return ResponseEntity.ok().body(discountService.updateCoupon(couponDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        try {
            discountService.deleteCoupon(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/coupons/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        boolean valid = discountService.validateCoupon(code);
        return ResponseEntity.ok().body(Map.of("valid", valid));
    }

    @PostMapping("/coupons/apply")
    public ResponseEntity<?> applyCoupon(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        Long customerId = Long.valueOf(request.get("customerId").toString());

        try {
            BigDecimal discount = discountService.applyCoupon(code, amount, customerId);
            return ResponseEntity.ok().body(Map.of("discount", discount, "finalAmount", amount.subtract(discount)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}