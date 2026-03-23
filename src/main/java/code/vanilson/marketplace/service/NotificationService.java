package code.vanilson.marketplace.service;

import code.vanilson.marketplace.model.Notification;
import code.vanilson.marketplace.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    private final EmailService emailService;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public NotificationService(EmailService emailService, NotificationRepository notificationRepository, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    public void sendNotification(String email, String subject, String message, String type) {
        logger.info("Sending notification to: {}, type: {}", email, type);

        try {
            emailService.sendEmail(email, subject, message);

            Notification notification = new Notification();
            notification.setRecipient(email);
            notification.setSubject(subject);
            notification.setMessage(message);
            notification.setType(type);
            notification.setStatus("SENT");
            notificationRepository.save(notification);

            logger.info("Notification sent and saved successfully");
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage());
            
            Notification notification = new Notification();
            notification.setRecipient(email);
            notification.setSubject(subject);
            notification.setMessage(message);
            notification.setType(type);
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    public void notifyOrderCreated(String email, String orderId, String orderDetails) {
        String subject = "Order Confirmation - Order #" + orderId;
        String message = String.format(
            "Dear Customer,\n\nYour order #%s has been confirmed.\n\nOrder Details:\n%s\n\nThank you for shopping with us!",
            orderId, orderDetails
        );
        sendNotification(email, subject, message, "ORDER_CREATED");
    }

    public void notifyPaymentProcessed(String email, String paymentId, String amount) {
        String subject = "Payment Confirmation - #" + paymentId;
        String message = String.format(
            "Dear Customer,\n\nYour payment of %s has been processed successfully.\n\nPayment ID: %s\n\nThank you!",
            amount, paymentId
        );
        sendNotification(email, subject, message, "PAYMENT_PROCESSED");
    }

    public void notifyOrderShipped(String email, String orderId, String trackingInfo) {
        String subject = "Order Shipped - Order #" + orderId;
        String message = String.format(
            "Dear Customer,\n\nYour order #%s has been shipped!\n\nTracking Info: %s\n\nThank you for shopping with us!",
            orderId, trackingInfo
        );
        sendNotification(email, subject, message, "ORDER_SHIPPED");
    }

    @KafkaListener(topics = "marketplace-email-notifications", groupId = "notification-service")
    public void handleEmailNotification(String message) {
        try {
            logger.info("Received email notification from Kafka: {}", message);
            EmailNotificationEvent event = objectMapper.readValue(message, EmailNotificationEvent.class);
            sendNotification(event.getEmail(), event.getSubject(), event.getMessage(), event.getType());
        } catch (Exception e) {
            logger.error("Error processing email notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "marketplace-order-notifications", groupId = "notification-service")
    public void handleOrderNotification(String message) {
        try {
            logger.info("Received order notification from Kafka: {}", message);
            EmailNotificationEvent event = objectMapper.readValue(message, EmailNotificationEvent.class);
            sendNotification(event.getEmail(), event.getSubject(), event.getMessage(), event.getType());
        } catch (Exception e) {
            logger.error("Error processing order notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "marketplace-payment-notifications", groupId = "notification-service")
    public void handlePaymentNotification(String message) {
        try {
            logger.info("Received payment notification from Kafka: {}", message);
            // Payment topic receives same format as email topic — just log, email already sent via email topic
            EmailNotificationEvent event = objectMapper.readValue(message, EmailNotificationEvent.class);
            logger.info("Payment confirmed for: {} — type: {}", event.getEmail(), event.getType());
        } catch (Exception e) {
            logger.error("Error processing payment notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "marketplace-notifications", groupId = "notification-service")
    public void handleGeneralNotification(String message) {
        try {
            logger.info("Received general notification from Kafka: {}", message);
        } catch (Exception e) {
            logger.error("Error processing general notification: {}", e.getMessage());
        }
    }

    public static class EmailNotificationEvent {
        private String email;
        private String subject;
        private String message;
        private String type;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}