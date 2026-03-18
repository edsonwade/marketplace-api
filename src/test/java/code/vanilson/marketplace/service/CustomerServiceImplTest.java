package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.exception.IllegalRequestException;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    /**
     * List of Customers
     */
    public List<CustomerDto> customerDtos;

    public List<Customer> customers;
    /**
     * Mock CustomerRepository
     */
    CustomerRepository customerRepositoryMock;
    /**
     * Current Object CustomerServiceImpl
     */
    CustomerServiceImpl currentInstance;
    /**
     * Object Customer
     */
    Customer customer;
    CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        customerDto = new CustomerDto(1L, "test", "test@test.test", "test 1");
        customer = new Customer(1L, "test", "test@test.test", "test 1");
        customerRepositoryMock = mock(CustomerRepository.class);
        currentInstance = new CustomerServiceImpl(customerRepositoryMock);
        customerDtos = List.of(
                new CustomerDto(1L, "test", "test@test.test", "test 1"),
                new CustomerDto(2L, "test1", "test1@test.test", "test 2"),
                new CustomerDto(3L, "test2", "test2@test.test", "test 3")
        );

        customers = List.of(
                new Customer(1L, "test", "test@test.test", "test 1"),
                new Customer(2L, "test1", "test1@test.test", "test 2"),
                new Customer(3L, "test2", "test2@test.test", "test 3"));

    }

    @Test
    @DisplayName("Get all Customers")
    void testGetAllCustomers() {
        //when
        when(customerRepositoryMock.findAll()).thenReturn(customers);
        var currentActual = currentInstance.findAllCustomers();

        //Asserts
        assertNotNull(customerDtos, "true");
        assertFalse(currentActual.isEmpty());
        assertNotNull(currentActual);
        //verify
        verify(customerRepositoryMock, times(1)).findAll();
    }

    @Test
    @DisplayName("Get Customer by id - Success")
    void testGetCustomerByIdSuccess() {
        when(customerRepositoryMock.findById(1L)).thenReturn(Optional.of(customer));
        assertEquals(customerDto, currentInstance.findCustomerById(1L).get(), "Customers should be the same");
        assertTrue(currentInstance.findCustomerById(1L).isPresent(), "Customer was  found");
        assertFalse(currentInstance.findCustomerById(1L).isEmpty());
        assertNotEquals(234L, currentInstance.findCustomerById(1L)
                .get()
                .getCustomerId());
        verify(customerRepositoryMock, times(4)).findById(1L);
    }

    @Test
    @DisplayName(" customer by id - Not Found")
    void testShouldThrowExceptionsWhenTheGivenIdIsNotFound() {
        when(customerRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        //assert
        assertThrows(ObjectWithIdNotFound.class, () -> currentInstance.findCustomerById(1L));
        verify(customerRepositoryMock).findById(1L);
    }

    @Test
    @DisplayName("create a new Customer - Success")
    void testCreateNewCustomerSuccess() {
        CustomerDto customerToSave = new CustomerDto(123L, "test01", "testo1@teste.test", "test 4");
        Customer mockCustomer = new Customer(123L, "test01", "testo1@teste.test", "test 4");
        when(customerRepositoryMock.save(any(Customer.class))).thenReturn(mockCustomer);
        var actualCurrent = currentInstance.saveCustomer(customerToSave);
        //asserts
        assertEquals(customerToSave, actualCurrent);
        assertEquals(123L, actualCurrent.getCustomerId().longValue());
        verify(customerRepositoryMock, atLeastOnce()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Create Customer - Not succeed")
    void testCreateCustomerThrowExceptionWhenIsCustomerIsNull() {
        assertThrows(IllegalRequestException.class, () -> currentInstance.saveCustomer(null));
    }

    @Test
    @DisplayName("Update Customer - Success")
    void testUpdateCustomerSuccess() {
        Customer existingCustomer = new Customer(1L, "John Doe", "john@example.com", "Address 1");
        Customer updatedCustomer = new Customer(1L, "Updated Name", "updated@example.com", "Updated Address");
        CustomerDto updateDto = new CustomerDto(1L, "Updated Name", "updated@example.com", "Updated Address");

        when(customerRepositoryMock.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepositoryMock.save(any(Customer.class))).thenReturn(updatedCustomer);

        CustomerDto result = currentInstance.updateCustomer(1L, updateDto);

        // Verify that the expected customer was returned
        assertEquals(updateDto, result);
    }

    @Test
    @DisplayName("Update Customer - Customer Not Found")
    void testUpdateCustomerCustomerNotFound() {
        CustomerDto updateDto = new CustomerDto(1L, "Updated Name", "updated@example.com", "Updated Address");

        when(customerRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ObjectWithIdNotFound.class, () -> currentInstance.updateCustomer(1L, updateDto));
    }

    @Test
    @DisplayName("Update Customer - Null Input Customer")
    void testUpdateCustomerNullInput() {
        assertThrows(IllegalRequestException.class, () -> currentInstance.updateCustomer(1L, null));
    }

    @Test
    @DisplayName("Update Customer - Null Values in Input")
    void testUpdateCustomerNullValuesInInput() {
        CustomerDto updateDto = new CustomerDto(1L, null, null, null);
        when(customerRepositoryMock.findById(1L)).thenReturn(Optional.of(new Customer()));
        assertThrows(IllegalRequestException.class, () -> currentInstance.updateCustomer(1L, updateDto));
    }

    @Test
    @DisplayName("Delete Customer - Success")
    void testDeleteCustomerWithSuccess() {
        when(customerRepositoryMock.findById(1L))
                .thenReturn(Optional.of(customer));
        var current = currentInstance.deleteCustomer(1L);
        assertTrue(current);
        verify(customerRepositoryMock, times(1))
                .delete(customer);
    }

    @Test
    @DisplayName(" Delete Customer - Not Found")
    void testDeleteCustomerNotFound() {
        when(customerRepositoryMock.findById(1L))
                .thenReturn(Optional.empty());
        assertThrows(ObjectWithIdNotFound.class,
                () -> currentInstance.deleteCustomer(1L));
        verify(customerRepositoryMock).findById(1L);
    }

    @Test
    @DisplayName("Delete Customer - Success")
    void testDeleteCustomerSuccess() {
        // Given
        long customerId = 1L;
        Customer customer = new Customer(customerId, "test", "test@test.test", "test 1");
        when(customerRepositoryMock.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        boolean result = currentInstance.deleteCustomer(customerId);

        // Then
        assertTrue(result, "Customer deletion should be successful");
        verify(customerRepositoryMock, times(1)).delete(customer);
    }

    @Test
    @DisplayName("Delete Customer - Not Found")
    void testDeleteCustomerNotFounded() {
        // Given
        long customerId = 1L;
        when(customerRepositoryMock.findById(customerId)).thenReturn(Optional.empty());

        // When
        assertThrows(ObjectWithIdNotFound.class, () -> currentInstance.deleteCustomer(customerId),
                "Exception should be thrown when customer is not found");

        // Then
        verify(customerRepositoryMock, never()).delete(any());
    }

}