package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByRecipient(String recipient);
    List<Notification> findByStatus(String status);
    List<Notification> findByType(String type);
    List<Notification> findByRecipientAndStatus(String recipient, String status);
}