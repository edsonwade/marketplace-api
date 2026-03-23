package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.CustomerDto;
import code.vanilson.marketplace.model.Customer;
import code.vanilson.marketplace.repository.CustomerRepository;
import code.vanilson.marketplace.service.CustomerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer API", description = "API for managing customers")
public class CustomerController {

    private static final Logger logger = LogManager.getLogger(CustomerController.class);
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public static final String CUSTOMER = "/customers/";

    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    /**
     * Returns the Customer record for the currently authenticated user (matched by email).
     * Used by the frontend to resolve customerId from the JWT.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current customer", description = "Returns the Customer linked to the authenticated user email")
    public ResponseEntity<?> getCurrentCustomer(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String email = principal.getUsername();
        // Find existing customer or auto-create one for users registered before this feature
        Customer customer = customerRepository.findByEmail(email)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setEmail(email);
                    newCustomer.setName(email.split("@")[0]);
                    newCustomer.setAddress("");
                    return customerRepository.save(newCustomer);
                });
        return ResponseEntity.ok(
                new CustomerDto(customer.getCustomerId(), customer.getName(), customer.getEmail(), customer.getAddress())
        );
    }

    /**
     * Returns all customers in the database.
     *
     * @return All customers in the database.
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
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
