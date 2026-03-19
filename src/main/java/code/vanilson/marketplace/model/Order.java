package code.vanilson.marketplace.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_orders")
@Getter
@Setter
@JsonPropertyOrder({"orderId", "customer", "localDateTime", "orderItems"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "orderId")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "order_date")
    private LocalDateTime localDateTime;
    //@JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems;

    public Order() {
        //default constructor
    }

    public Order(LocalDateTime localDateTime, Customer customer, Set<OrderItem> orderItems) {
        this.localDateTime = localDateTime;
        this.customer = customer;
        this.orderItems = orderItems;
    }

    public Order(long orderId, Customer customer, Set<OrderItem> orderItems) {
        this.orderId = orderId;
        this.customer = customer;
        this.orderItems = orderItems;
    }

    public Order(long orderId, LocalDateTime localDateTime, Customer customer, Set<OrderItem> orderItems) {
        this.orderId = orderId;
        this.localDateTime = localDateTime;
        this.customer = customer;
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this); // Set the back reference to the Order
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Order order = (Order) o;

        if (!Objects.equals(orderId, order.orderId)) {
            return false;
        }
        if (!Objects.equals(localDateTime, order.localDateTime)) {
            return false;
        }
        if (!Objects.equals(customer, order.customer)) {
            return false;
        }
        return Objects.equals(orderItems, order.orderItems);
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (localDateTime != null ? localDateTime.hashCode() : 0);
        result = 31 * result + (customer != null ? customer.hashCode() : 0);
        result = 31 * result + (orderItems != null ? orderItems.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", localDateTime=" + localDateTime +
                ", customer=" + customer +
                '}';
    }
}
