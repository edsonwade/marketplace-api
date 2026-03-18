/**
 * Author: vanilson muhongo
 * Date:23/06/2024
 * Time:20:28
 * Version:1
 */
package code.vanilson.marketplace.stepdefinitions;

import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.ProductMapper;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.repository.ProductRepository;
import code.vanilson.marketplace.service.ProductServiceImpl;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class ProductServiceStep {

    private final ProductRepository productRepository = mock(ProductRepository.class);

    @InjectMocks
    private final ProductServiceImpl productService = new ProductServiceImpl(productRepository);

    Product savedProduct;
    Product product;
    private String actualErrorMessage;

    private List<Product> products;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        products = new ArrayList<>();
        product = new Product(1, "TV", 10, 1);
        savedProduct = new Product(4, "Chair", 50, 4);
    }

    /**
     * Step definition to populate the customer repository with the provided list of customers.
     *
     * @param dataTable DataTable containing the list of customers with their details.
     */
    @Given("the product repository contains the following products")
    public void the_product_repository_contains_the_following_Products(DataTable dataTable) {
        products = dataTable.asMaps().stream().map(row ->
                new Product(Integer.parseInt(row.get("id")),
                        row.get("name"),
                        Integer.parseInt(row.get("quantity")),
                        Integer.parseInt(row.get("version"))
                )).collect(Collectors.toList());


        // Mocking behavior of customerRepository.findAll()
        when(productRepository.findAll()).thenReturn(products);
    }

    @When("I request all Products")
    public void iRequestAllProducts() {
        List<ProductDto> productDos = productService.findAllProducts();
        products = ProductMapper.toProductList(productDos);
    }

    @Then("I should receive a list of products containing")
    public void iShouldReceiveAListOfProductsContaining(DataTable dataTable) {
        var productExpected = dataTable.asMaps().stream().map(row ->
                new Product(Integer.parseInt(row.get("id")),
                        row.get("name"),
                        Integer.parseInt(row.get("quantity")),
                        Integer.parseInt(row.get("version"))
                )).collect(Collectors.toList());

        assertEquals(productExpected, products);
        assertNotNull(productExpected);


    }

    @Given("the product repository contains a product with id {int}")
    public void theProductRepositoryContainsAProductWithId(int id) {
        product = new Product(1, "TV", 10, 1);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
    }

    @When("I request the product with id {int}")
    public void iRequestTheProductWithId(int id) {
        var result = productService.findById(id);
        if (result.isEmpty()) {
            actualErrorMessage = "product with id " + id + " not found";
        }
    }

    @Then("I should receive the product with id {int} and details")
    public void iShouldReceiveTheProductWithIdAndDetails(Integer id, DataTable dataTable) {
        Map<String, String> row = dataTable.asMaps().get(0);
        var expectedProduct = new Product(id, row.get("name"), Integer.parseInt(row.get("quantity")),
                Integer.parseInt(row.get("version")));
        assertEquals(expectedProduct, product);
    }

    @Given("the product repository does not contain a product with id {int}")
    public void theProductRepositoryDoesNotContainAProductWithId(int id) {
        when(productRepository.findById(id)).thenReturn(Optional.empty());
    }

    @Then("I should receive an error message as {string}")
    public void iShouldReceiveAnErrorMessageAs(String expectedErrorMessage) {
        //log.info("product with id {} not found", actualErrorMessage);
        assertNotNull(actualErrorMessage);
        assertEquals(expectedErrorMessage, actualErrorMessage.trim());
    }


    @Given("a new product with details")
    public void aNewProductWithDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        savedProduct = new Product(
                Integer.parseInt(row.get("id")),
                row.get("name"),
                Integer.parseInt(row.get("quantity")),
                Integer.parseInt(row.get("version")));
        // Prepare mock behavior for saving
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
    }

    @When("I save the product")
    public void iSaveTheProduct() {
        // Mock already prepared in Given or can be here
        // when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
    }

    @Then("the product should be saved with details")
    public void theProductShouldBeSavedWithDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        var expectedProduct = new Product(
                Integer.parseInt(row.get("id")),
                row.get("name"),
                Integer.parseInt(row.get("quantity")),
                Integer.parseInt(row.get("version")));

        var actualProduct = productService.save(ProductMapper.toProductDto(expectedProduct));

        assertEquals(expectedProduct.getProductId(), actualProduct.getProductId());
        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getQuantity(), actualProduct.getQuantity());
        assertEquals(expectedProduct.getVersion(), actualProduct.getVersion());
    }

    @Given("a null product")
    public void aNullProduct() {
        savedProduct = null;
    }

    @When("I try to save the null product")
    public void iTryToSaveTheNullProduct() {
        // No need to mock behavior for saving null product, just proceed to invocation
    }

    @Then("I should receive an error message like {string}")
    public void iShouldReceiveAnErrorMessageLike(String errorMessage) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.save(ProductMapper.toProductDto(savedProduct)));
        assertEquals(errorMessage, exception.getMessage());
    }

    @And("the product has details")
    public void theProductHasDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        product.setName(row.get("name"));
        product.setQuantity(Integer.parseInt(row.get("quantity")));
        product.setVersion(Integer.parseInt(row.get("version")));
    }

    @When("I update the product with id {int} to have details")
    public void iUpdateTheProductWithIdToHaveDetails(int id, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        product.setProductId(id);
        product.setName(row.get("name"));
        product.setQuantity(Integer.parseInt(row.get("quantity")));
        product.setVersion(Integer.parseInt(row.get("version")));
        productService.update(ProductMapper.toProductDto(product));
    }

    @Then("the product should be updated to have details")
    public void theProductShouldBeUpdatedToHaveDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        Map<String, String> row = rows.get(0);
        var updatedProduct = productRepository.findById(product.getProductId())
                .orElseThrow(() -> new ObjectWithIdNotFound("Product with id " + product.getProductId() + " not found"));
        assertNotNull(updatedProduct);
        assertEquals(row.get("name"), updatedProduct.getName());
        assertEquals(Integer.parseInt(row.get("quantity")), updatedProduct.getQuantity().intValue());
        assertEquals(Integer.parseInt(row.get("version")), updatedProduct.getVersion().intValue());
    }

    @When("I delete the product with id {int}")
    public void iDeleteTheProductWithId(int id) {
        when(productRepository.existsById(id)).thenReturn(true);
        productService.delete(id);
    }

    @Then("the product with id {int} should be deleted successfully")
    public void theProductWithIdShouldBeDeletedSuccessfully(int id) {
        verify(productRepository, times(1)).deleteById(any(Integer.class));
    }


    @When("I try to delete the product with id {int}")
    public void iTryToDeleteTheProductWithId(int id) {
        var exception = assertThrows(ObjectWithIdNotFound.class, () -> productService.delete(id));
        assertEquals(MessageFormat.format("Product with id {0} not found", id), exception.getMessage());
    }


    @Then("I should receive an error message when try to delete {string}")
    public void iShouldReceiveAnErrorMessageWhenTryToDelete(String expectedErrorMessage) {
        // The error message should be validated against the thrown exception
        var exception = assertThrows(ObjectWithIdNotFound.class, () -> productService.delete(1));
        assertEquals(expectedErrorMessage, exception.getMessage());
    }
}
