package code.vanilson.marketplace.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaNotificationConfig {

    public static final String NOTIFICATION_TOPIC = "marketplace-notifications";
    public static final String EMAIL_NOTIFICATION_TOPIC = "marketplace-email-notifications";
    public static final String ORDER_NOTIFICATION_TOPIC = "marketplace-order-notifications";
    public static final String PAYMENT_NOTIFICATION_TOPIC = "marketplace-payment-notifications";

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailNotificationTopic() {
        return TopicBuilder.name(EMAIL_NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderNotificationTopic() {
        return TopicBuilder.name(ORDER_NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentNotificationTopic() {
        return TopicBuilder.name(PAYMENT_NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}