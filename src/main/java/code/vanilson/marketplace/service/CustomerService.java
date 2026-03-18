package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    /**
     * Returns all Customers in the database.
     *
     * @return All Customers in the database.
     */
    List<CustomerDto> findAllCustomers();

    /**
     * Returns the customer with the specified id.
     *
     * @param id ID of the customer to retrieve.
     * @return The requested Customer if found.
     */
    Optional<CustomerDto> findCustomerById(Long id);

    /**
     * Updates the specifiedCustomer, identified by its id.
     *
     * @param customerDto The customer to update.
     * @return The updated Customer.
     */
    CustomerDto updateCustomer(long id, CustomerDto customerDto);

    /**
     * Saves the specified customer to the database.
     *
     * @param customerDto The customer to save to the database.
     * @return The savedCustomer.
     */
    CustomerDto saveCustomer(CustomerDto customerDto);

    /**
     * Deletes the customer with the specified id.
     *
     * @param id The id of the customer to delete.
     * @return True if the operation was successful.
     */
    boolean deleteCustomer(long id);
}
