package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.PaymentDto;
import code.vanilson.marketplace.dto.PaymentMethodDto;
import code.vanilson.marketplace.service.PaymentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LogManager.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentDto>> getAllPayments() {
        return ResponseEntity.ok().body(paymentService.findAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        return paymentService.findPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok().body(paymentService.findPaymentsByOrderId(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok().body(paymentService.findPaymentsByCustomerId(customerId));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody PaymentDto paymentDto) {
        logger.info("Creating payment for order: {}", paymentDto.getOrderId());
        PaymentDto saved = paymentService.createPayment(paymentDto);
        try {
            return ResponseEntity
                    .created(new URI("/api/payments/" + saved.getPaymentId()))
                    .body(saved);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentDto> processPayment(@RequestParam Long orderId,
                                                     @RequestParam Long customerId,
                                                     @RequestParam String paymentMethod,
                                                     @RequestParam(required = false) String token) {
        logger.info("Processing payment for order: {}", orderId);
        PaymentDto saved = paymentService.processPayment(orderId, customerId, paymentMethod, token);
        return ResponseEntity.ok().body(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentDto> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok().body(paymentService.updatePaymentStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/methods/customer/{customerId}")
    public ResponseEntity<List<PaymentMethodDto>> getPaymentMethodsByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok().body(paymentService.findPaymentMethodsByCustomerId(customerId));
    }

    @GetMapping("/methods/{id}")
    public ResponseEntity<?> getPaymentMethodById(@PathVariable Long id) {
        return paymentService.findPaymentMethodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/methods")
    public ResponseEntity<PaymentMethodDto> addPaymentMethod(@RequestBody PaymentMethodDto paymentMethodDto) {
        logger.info("Adding payment method for customer: {}", paymentMethodDto.getCustomerId());
        PaymentMethodDto saved = paymentService.addPaymentMethod(paymentMethodDto);
        try {
            return ResponseEntity
                    .created(new URI("/api/payments/methods/" + saved.getPaymentMethodId()))
                    .body(saved);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/methods/{id}/default")
    public ResponseEntity<PaymentMethodDto> setDefaultPaymentMethod(@PathVariable Long id, @RequestParam Long customerId) {
        return ResponseEntity.ok().body(paymentService.setDefaultPaymentMethod(customerId, id));
    }

    @DeleteMapping("/methods/{id}")
    public ResponseEntity<?> deletePaymentMethod(@PathVariable Long id) {
        try {
            paymentService.deletePaymentMethod(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}