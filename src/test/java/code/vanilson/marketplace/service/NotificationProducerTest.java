package code.vanilson.marketplace.service;

import code.vanilson.marketplace.config.KafkaNotificationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    @Test
    void testSendNotification() {
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class))).thenReturn(future);

        notificationProducer.sendNotification("test-topic", "key1", "message1");

        verify(kafkaTemplate, times(1)).send(eq("test-topic"), eq("key1"), eq("message1"));
    }

    @Test
    void testSendOrderNotification() {
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class))).thenReturn(future);

        notificationProducer.sendOrderNotification("ORD-123", "Order created message");

        verify(kafkaTemplate, times(1)).send(
            eq(KafkaNotificationConfig.ORDER_NOTIFICATION_TOPIC),
            eq("ORD-123"),
            eq("Order created message")
        );
    }

    @Test
    void testSendPaymentNotification() {
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class))).thenReturn(future);

        notificationProducer.sendPaymentNotification("PAY-456", "Payment processed message");

        verify(kafkaTemplate, times(1)).send(
            eq(KafkaNotificationConfig.PAYMENT_NOTIFICATION_TOPIC),
            eq("PAY-456"),
            eq("Payment processed message")
        );
    }

    @Test
    void testSendEmailNotification() {
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class))).thenReturn(future);

        notificationProducer.sendEmailNotification("test@example.com", "Email content");

        verify(kafkaTemplate, times(1)).send(
            eq(KafkaNotificationConfig.EMAIL_NOTIFICATION_TOPIC),
            eq("test@example.com"),
            eq("Email content")
        );
    }

    @Test
    void testSendGeneralNotification() {
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class))).thenReturn(future);

        notificationProducer.sendGeneralNotification("general-key", "General message");

        verify(kafkaTemplate, times(1)).send(
            eq(KafkaNotificationConfig.NOTIFICATION_TOPIC),
            eq("general-key"),
            eq("General message")
        );
    }
}
