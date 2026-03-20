package code.vanilson.marketplace.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        logger.info("Sending email to: {}, subject: {}", to, subject);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@marketplace.com");
            
            mailSender.send(message);
            
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}, error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendOrderConfirmation(String email, String orderId, String orderDetails) {
        String subject = "Order Confirmation - Order #" + orderId;
        String body = String.format(
            "Dear Customer,\n\n" +
            "Thank you for your order!\n\n" +
            "Order ID: %s\n" +
            "Order Details:\n%s\n\n" +
            "We will notify you once your order is shipped.\n\n" +
            "Best regards,\n" +
            "Marketplace Team",
            orderId, orderDetails
        );
        sendEmail(email, subject, body);
    }

    public void sendPaymentConfirmation(String email, String paymentId, String amount) {
        String subject = "Payment Confirmation - Payment #" + paymentId;
        String body = String.format(
            "Dear Customer,\n\n" +
            "Your payment has been processed successfully.\n\n" +
            "Payment ID: %s\n" +
            "Amount: %s\n\n" +
            "Thank you for your purchase!\n\n" +
            "Best regards,\n" +
            "Marketplace Team",
            paymentId, amount
        );
        sendEmail(email, subject, body);
    }

    public void sendWelcomeEmail(String email, String name) {
        String subject = "Welcome to Marketplace!";
        String body = String.format(
            "Dear %s,\n\n" +
            "Welcome to Marketplace!\n\n" +
            "We're excited to have you on board. Start exploring our products and enjoy great deals!\n\n" +
            "Best regards,\n" +
            "Marketplace Team",
            name
        );
        sendEmail(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format(
            "Dear Customer,\n\n" +
            "You requested a password reset. Please use the following token to reset your password:\n\n" +
            "Token: %s\n\n" +
            "This token will expire in 24 hours.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Marketplace Team",
            resetToken
        );
        sendEmail(email, subject, body);
    }
}