package code.vanilson.marketplace.service;

import code.vanilson.marketplace.model.Notification;
import code.vanilson.marketplace.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationService notificationService;

    private Notification savedNotification;

    @BeforeEach
    void setUp() throws Exception {
        savedNotification = new Notification();
        savedNotification.setId("notif-1");
        savedNotification.setRecipient("test@example.com");
        savedNotification.setSubject("Test Subject");
        savedNotification.setMessage("Test Message");
        savedNotification.setType("TEST");
        savedNotification.setStatus("SENT");
    }

    @Test
    void testSendNotificationSuccess() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendNotification("test@example.com", "Test Subject", "Test Message", "TEST");

        verify(emailService, times(1)).sendEmail(eq("test@example.com"), eq("Test Subject"), eq("Test Message"));
        
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        
        Notification captured = captor.getValue();
        assertThat(captured.getRecipient()).isEqualTo("test@example.com");
        assertThat(captured.getSubject()).isEqualTo("Test Subject");
        assertThat(captured.getStatus()).isEqualTo("SENT");
    }

    @Test
    void testSendNotificationFailure() {
        doThrow(new RuntimeException("Email failed")).when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.sendNotification("test@example.com", "Test Subject", "Test Message", "TEST");

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
        
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        
        Notification captured = captor.getValue();
        assertThat(captured.getRecipient()).isEqualTo("test@example.com");
        assertThat(captured.getStatus()).isEqualTo("FAILED");
        assertThat(captured.getErrorMessage()).contains("Email failed");
    }

    @Test
    void testNotifyOrderCreated() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyOrderCreated("test@example.com", "ORD-123", "Product A x 1");

        verify(emailService, times(1)).sendEmail(
            eq("test@example.com"),
            eq("Order Confirmation - Order #ORD-123"),
            anyString()
        );
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyPaymentProcessed() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyPaymentProcessed("test@example.com", "PAY-456", "$100.00");

        verify(emailService, times(1)).sendEmail(
            eq("test@example.com"),
            eq("Payment Confirmation - #PAY-456"),
            anyString()
        );
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testNotifyOrderShipped() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyOrderShipped("test@example.com", "ORD-123", "Track: 1Z999AA10123456784");

        verify(emailService, times(1)).sendEmail(
            eq("test@example.com"),
            eq("Order Shipped - Order #ORD-123"),
            anyString()
        );
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testHandleEmailNotificationSuccess() throws Exception {
        String message = "{\"email\":\"test@example.com\",\"subject\":\"Test\",\"message\":\"Body\",\"type\":\"TEST\"}";
        
        NotificationService.EmailNotificationEvent event = new NotificationService.EmailNotificationEvent();
        event.setEmail("test@example.com");
        event.setSubject("Test");
        event.setMessage("Body");
        event.setType("TEST");
        
        when(objectMapper.readValue(message, NotificationService.EmailNotificationEvent.class)).thenReturn(event);
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.handleEmailNotification(message);

        verify(objectMapper, times(1)).readValue(message, NotificationService.EmailNotificationEvent.class);
        verify(emailService, times(1)).sendEmail(eq("test@example.com"), eq("Test"), eq("Body"));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testHandleEmailNotificationInvalidJson() throws Exception {
        String invalidMessage = "not valid json";
        when(objectMapper.readValue(invalidMessage, NotificationService.EmailNotificationEvent.class))
            .thenThrow(new RuntimeException("Invalid JSON"));
        
        notificationService.handleEmailNotification(invalidMessage);

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testSendNotificationWithDifferentTypes() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendNotification("test@example.com", "Subject", "Message", "ORDER_CREATED");
        notificationService.sendNotification("test@example.com", "Subject", "Message", "ORDER_SHIPPED");
        notificationService.sendNotification("test@example.com", "Subject", "Message", "PAYMENT_PROCESSED");

        verify(emailService, times(3)).sendEmail(anyString(), anyString(), anyString());
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }
}
