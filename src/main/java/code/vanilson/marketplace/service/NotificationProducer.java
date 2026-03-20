package code.vanilson.marketplace.service;

import code.vanilson.marketplace.config.KafkaNotificationConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationProducer {

    private static final Logger logger = LogManager.getLogger(NotificationProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String topic, String key, String message) {
        logger.info("Sending notification to topic: {}, key: {}", topic, key);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Notification sent successfully to topic: {}, partition: {}, offset: {}", 
                    topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send notification to topic: {}, error: {}", topic, ex.getMessage());
            }
        });
    }

    public void sendOrderNotification(String orderId, String message) {
        sendNotification(KafkaNotificationConfig.ORDER_NOTIFICATION_TOPIC, orderId, message);
    }

    public void sendPaymentNotification(String paymentId, String message) {
        sendNotification(KafkaNotificationConfig.PAYMENT_NOTIFICATION_TOPIC, paymentId, message);
    }

    public void sendEmailNotification(String email, String message) {
        sendNotification(KafkaNotificationConfig.EMAIL_NOTIFICATION_TOPIC, email, message);
    }

    public void sendGeneralNotification(String key, String message) {
        sendNotification(KafkaNotificationConfig.NOTIFICATION_TOPIC, key, message);
    }
}