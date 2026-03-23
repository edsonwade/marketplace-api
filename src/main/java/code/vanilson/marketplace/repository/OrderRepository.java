package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems")
    List<Order> findAllOrdersWithDetails();

    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems WHERE o.customer.customerId = :customerId")
    List<Order> findByCustomerIdWithDetails(@org.springframework.data.repository.query.Param("customerId") Long customerId);

    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems " +
           "WHERE o.customer.customerId = :customerId " +
           "AND o.orderId NOT IN (" +
           "  SELECT p.orderId FROM Payment p WHERE p.paymentStatus = 'COMPLETED'" +
           ")")
    List<Order> findUnpaidByCustomerId(@org.springframework.data.repository.query.Param("customerId") Long customerId);


}
