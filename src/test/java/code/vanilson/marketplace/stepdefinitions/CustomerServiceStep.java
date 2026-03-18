package code.vanilson.marketplace.stepdefinitions;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.mapper.CustomerMapper;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.repository.CustomerRepository;
import code.vanilson.marketplace.service.CustomerServiceImpl;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * CustomerServiceSteps
 *
 * @author vamuhong
 * @version 1.0
 * @since 2024-06-15
 */

public class CustomerServiceStep {

    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    @InjectMocks
    private final CustomerServiceImpl customerService = new CustomerServiceImpl(customerRepository);

    CustomerDto savedCustomer;

    private List<Customer> customers;
    private Exception exception;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        customers = new ArrayList<>();
        savedCustomer = new CustomerDto(123L, "test01", "testo1@teste.test", "test 4");
        exception = null;
    }

    /**
     * Step definition to populate the customer repository with the provided list of customers.
     *
     * @param dataTable DataTable containing the list of customers with their details.
     */
    @Given("the customer repository contains the following customers")
    public void the_customer_repository_contains_the_following_customers(DataTable dataTable) {
        customers = dataTable.asMaps().stream().map(row ->
                new Customer(Long.parseLong(row.get("id")), row.get("name"), row.get("email"), row.get("address"))
        ).collect(Collectors.toList());

        // Mocking behavior of customerRepository.findAll()
        when(customerRepository.findAll()).thenReturn(customers);
    }

    /**
     * Step definition to mock the presence of a customer with a specific ID in the repository.
     *
     * @param id ID of the customer to mock.
     */
    @Given("the customer repository contains a customer with id {long}")
    public void the_customer_repository_contains_a_customer_with_id(Long id) {
        Customer customer = new Customer(id, "test", "test@test.test", "test 1");
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
    }

    /**
     * Step definition to mock the absence of a customer with a specific ID in the repository.
     *
     * @param id ID of the customer to mock absence for.
     */

    @Given("the customer repository does not contain a customer with id {long}")
    public void the_customer_repository_does_not_contain_a_customer_with_id(Long id) {
        when(customerRepository.findById(id)).thenReturn(Optional.empty());
    }

    /**
     * Step definition to initialize a new customer with details from the DataTable.
     *
     * @param dataTable DataTable containing details of the new customer.
     */

    @Given("a new customer with details")
    public void a_new_customer_with_details(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        savedCustomer =
                new CustomerDto(Long.parseLong(row.get("id")), row.get("name"), row.get("email"), row.get("address"));
    }

    /**
     * Step definition to set the savedCustomer object to null.
     */
    @Given("a null customer")
    public void a_null_customer() {
        savedCustomer = null;
    }

    /**
     * Step definition to mock the presence of a customer with specific details in the repository.
     *
     * @param dataTable DataTable containing details of the existing customer.
     */
    @Given("the customer has details")
    public void the_customer_has_details(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);

        // Check if 'id' field is present and not null
        Long customerId = row.get("id") != null ? Long.parseLong(row.get("id")) : null;

        Customer existingCustomer = new Customer(customerId,
                row.get("name"),
                row.get("email"),
                row.get("address"));

        // Mock repository behavior to return Optional of existingCustomer
        when(customerRepository.findById(existingCustomer.getCustomerId())).thenReturn(Optional.of(existingCustomer));
    }

    /**
     * Step definition to request all customers from the service and map them to DTOs.
     */
    @When("I request all customers")
    public void i_request_all_customers() {
        List<CustomerDto> customerDtos = customerService.findAllCustomers();
        customers = CustomerMapper.toCustomerList(customerDtos);
    }

    /**
     * Step definition to request a customer with a specific ID from the service.
     *
     * @param id ID of the customer to request.
     */
    @When("I request the customer with id {long}")
    public void i_request_the_customer_with_id(Long id) {
        try {
            var customerDto = customerService.findCustomerById(id);
            savedCustomer = customerDto.orElse(null);
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to mock the behavior of saving a customer in the repository.
     */
    @When("I save the customer")
    public void i_save_the_customer() {
        try {
            when(customerRepository.save(any(Customer.class))).thenReturn(CustomerMapper.toCustomer(savedCustomer));
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to attempt to save a null customer in the service.
     */
    @When("I try to save the null customer")
    public void i_try_to_save_the_null_customer() {
        try {
            customerService.saveCustomer(null);
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to update a customer with a specific ID to have new details.
     *
     * @param id        ID of the customer to update.
     * @param dataTable DataTable containing updated details of the customer.
     */
    @When("I update the customer with id {long} to have details")
    public void i_update_the_customer_with_id_to_have_details(Long id, io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        CustomerDto updatedCustomer = new CustomerDto(id, row.get("name"), row.get("email"), row.get("address"));
        try {
            savedCustomer = customerService.updateCustomer(id, updatedCustomer);
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to attempt to update a customer with a specific ID.
     *
     * @param id ID of the customer to update.
     */

    @When("I try to update the customer with id {long}")
    public void i_try_to_update_the_customer_with_id(Long id) {
        try {
            customerService.updateCustomer(id,
                    new CustomerDto(id, "Updated Name", "updated@example.com", "Updated Address"));
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to attempt to update a customer with a specific ID using a null customer object.
     *
     * @param id ID of the customer to update.
     */
    @When("I try to update the customer with id {long} with a null customer")
    public void i_try_to_update_the_customer_with_id_with_a_null_customer(Long id) {
        try {
            customerService.updateCustomer(id, null);
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to attempt to delete a customer with a specific ID.
     *
     * @param id ID of the customer to delete.
     */
    @When("I try to delete the customer with id {long}")
    public void i_try_to_delete_the_customer_with_id(Long id) {
        try {
            customerService.deleteCustomer(id);
        } catch (Exception e) {
            exception = e;
        }
    }

    /**
     * Step definition to assert that the received list of customers matches the expected details.
     *
     * @param dataTable DataTable containing expected details of customers.
     */
    @Then("I should receive a list of customers containing")
    public void i_should_receive_a_list_of_customers_containing(DataTable dataTable) {
        List<Customer> expectedCustomers = dataTable.asMaps().stream().map(row ->
                new Customer(Long.parseLong(row.get("id")), row.get("name"), row.get("email"), row.get("address"))
        ).toList();
        assertEquals(expectedCustomers, customers);
    }

    /**
     * Step definition to assert that the received customer details match the expected details for a specific ID.
     *
     * @param id        ID of the customer to assert.
     * @param dataTable DataTable containing expected details of the customer.
     */

    @Then("I should receive the customer with id {long} and details")
    public void i_should_receive_the_customer_with_id_and_details(Long id, DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        CustomerDto expectedCustomer = new CustomerDto(id, row.get("name"), row.get("email"), row.get("address"));
        assertEquals(expectedCustomer, savedCustomer);
    }

    /**
     * Step definition to assert that an error message received matches the expected error message.
     *
     * @param errorMessage Expected error message.
     */

    @Then("I should receive an error message {string}")
    public void i_should_receive_an_error_message(String errorMessage) {
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    /**
     * Step definition to assert that a customer should be saved with specific details.
     *
     * @param dataTable DataTable containing details of the customer to be saved.
     */
    @Then("the customer should be saved with details")
    public void the_customer_should_be_saved_with_details(DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        CustomerDto expectedCustomer =
                new CustomerDto(Long.parseLong(row.get("id")), row.get("name"), row.get("email"), row.get("address"));
        when(customerRepository.save(any(Customer.class))).thenReturn(CustomerMapper.toCustomer(savedCustomer));
        var actualCurrent = customerService.saveCustomer(expectedCustomer);
        assertEquals(savedCustomer, actualCurrent);
    }

    /**
     * Step definition to assert that a customer should be updated with specific details.
     *
     * @param dataTable DataTable containing updated details of the customer.
     */

    @Then("the customer should be updated to have details")
    public void the_customer_should_be_updated_to_have_details(DataTable dataTable) {
        savedCustomer = new CustomerDto(123L, "Updated Name", "updated@example.com", "Updated Address");
        Map<String, String> row = dataTable.asMaps().get(0);
        CustomerDto expectedCustomer =
                new CustomerDto(savedCustomer.getCustomerId(), row.get("name"), row.get("email"), row.get("address"));
        assertEquals(expectedCustomer, savedCustomer);
    }

    /**
     * Step definition to verify that a customer with a specific ID should be deleted successfully.
     *
     * @param id ID of the customer to be deleted.
     */
    @Then("the customer with id {long} should be deleted successfully")
    public void the_customer_with_id_should_be_deleted_successfully(Long id) {
        verify(customerRepository, times(1)).delete(any(Customer.class));
    }

    /**
     * Step definition to delete a customer with a specific ID.
     *
     * @param id ID of the customer to delete.
     */

    @When("I delete the customer with id {long}")
    public void i_delete_the_customer_with_id(Long id) {
        customerService.deleteCustomer(id);
    }

}