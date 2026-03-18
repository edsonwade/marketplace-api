package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer API", description = "API for managing customers")
public class CustomerController {

    private static final Logger logger = LogManager.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public static final String CUSTOMER = "/customers/";

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Returns all customers in the database.
     *
     * @return All customers in the database.
     */
    @GetMapping
    @Operation(summary = "Get all customers", description = "Returns a list of all customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all customers")
    })
    public ResponseEntity<Iterable<CustomerDto>> getCustomers() {
        return ResponseEntity.ok().body(customerService.findAllCustomers());
    }

    /**
     * Returns the customer with the specified ID.
     *
     * @param id The ID of the customer to retrieve.
     * @return The customer with the specified ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Returns the customer with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the customer"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Optional<CustomerDto>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok()
                .body(customerService.findCustomerById(id));
    }

    /**
     * Creates a new customer.
     *
     * @param customer The customer to create.
     * @return The created customer.
     */

    @Operation(summary = "Create a new customer", description = "Creates a new customer with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created the customer"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data")
    })

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        logger.info("Creating new customer with name: {}, email: {}, address: {}",
                customerDto.getName(),
                customerDto.getEmail(),
                customerDto.getAddress());

        // Create a new customer
        CustomerDto newCustomer = customerService.saveCustomer(customerDto);

        try {
            // Build a created response
            return ResponseEntity
                    .created(new URI(CUSTOMER + newCustomer.getCustomerId()))
                    .body(newCustomer);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates the fields in the specified customer with the specified ID.
     *
     * @param customer The updated customer data.
     * @param id       The ID of the customer to update.
     * @return The updated customer.
     */
    @Operation(summary = "Update a customer",
            description = "Updates the fields of the specified customer with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the customer"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@Valid @RequestBody CustomerDto customerDto,
                                                   @PathVariable Long id) {
        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok().body(updatedCustomer);
    }

    /**
     * Deletes the customer with the specified ID.
     *
     * @param id The ID of the customer to delete.
     * @return A ResponseEntity with one of the following status codes:
     * 200 OK if to delete was successful
     * 404 Not Found if a customer with the specified ID is not found
     * 500 Internal Service Error if an error occurs during deletion
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer", description = "Deletes the customer with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the customer"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "500", description = "Error occurred during deletion")
    })
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {

        logger.info("Deleting customer with ID {}", id);

        // Get the existing customer
        Optional<CustomerDto> existingCustomer = customerService.findCustomerById(id);

        return existingCustomer.map(p -> {
            if (customerService.deleteCustomer(p.getCustomerId())) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

}
