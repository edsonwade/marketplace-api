package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Product;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({DBUnitExtension.class, SpringExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class ProductRepositoryImplTest {

    @Autowired
    private ProductRepository repository;

    @Test
    @DataSet("datasets/products.yml")
    void testFindAll() {
        List<Product> products = repository.findAll();
        assertEquals(3, products.size(), "We should have 3 products in our database");
    }

    @Test
    @DataSet("datasets/products.yml")
    void testFindByIdSuccess() {
        // Find the product with ID 2
        Optional<Product> product = repository.findById(2);

        // Validate that we found it
        Assertions.assertTrue(product.isPresent(),
                "code.vanilson.marketplace.product.ProductSteps with ID 2 should be found");

        // Validate the product values
        Product p = product.get();
        assertEquals(2, p.getProductId().intValue(), "code.vanilson.marketplace.product.ProductSteps ID should be 2");
        assertEquals("Keyboard", p.getName(),
                "code.vanilson.marketplace.product.ProductSteps name should be \"code.vanilson.marketplace.product.ProductSteps 2\"");
        assertEquals(5, p.getQuantity().intValue(), "code.vanilson.marketplace.product.ProductSteps quantity should be 5");
        assertEquals(2, p.getVersion().intValue(), "code.vanilson.marketplace.product.ProductSteps version should be 2");
    }

    @Test
    @DataSet("datasets/products.yml")
    void testFindByIdNotFound() {
        // Find the product with ID 2
        Optional<Product> product = repository.findById(4);

        // Validate that we found it
        Assertions.assertFalse(product.isPresent(),
                " product with ID 4 should be not be found");
    }

    @Test
    @DataSet(value = "datasets/products.yml")
    void testUpdateSuccess() {
        // Update product 1's name, quantity, and version
        Product product = repository.findById(1).orElseThrow();
        product.setName("This is product 1");
        product.setQuantity(100);
        // Note: version will be incremented by JPA due to @Version
        Product result = repository.save(product);

        // Validate that our product is returned by update()
        Assertions.assertNotNull(result, "The product should have been updated");

        // Retrieve product 1 from the database and validate its fields
        Optional<Product> loadedProduct = repository.findById(1);
        Assertions.assertTrue(loadedProduct.isPresent(), "Updated product should exist in the database");
        assertEquals("This is product 1", loadedProduct.get().getName(), "The product name does not match");
        assertEquals(100, loadedProduct.get().getQuantity().intValue(), "The quantity should now be 100");
        // version 1 -> 2
        assertEquals(2, loadedProduct.get().getVersion().intValue(), "The version should now be 2");
    }

    @Test
    @DataSet(value = "datasets/products.yml")
    void testUpdateFailure() {
        // In JpaRepository, save() updates if ID exists, otherwise inserts. 
        // To mimic "update failure" for non-existent ID, we check if it exists first.
        Product product = new Product(4, "Mouse", 100, 5);
        boolean exists = repository.existsById(product.getProductId());
        
        Assertions.assertFalse(exists, "The product should not exist");
    }

    @Test
    @DataSet("datasets/products.yml")
    void testDeleteSuccess() {
        repository.deleteById(1);
        
        // Validate that the product has been deleted
        Optional<Product> product = repository.findById(1);
        Assertions.assertFalse(product.isPresent(),
                "product with ID 1 should have been deleted");
    }

    @Test
    @DataSet("datasets/products.yml")
    void testDeleteFailure() {
        // SimpleJpaRepository.deleteById throws EmptyResultDataAccessException if ID not found in Spring Boot 3.0.x
        Integer id = 4;
        Assertions.assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> repository.deleteById(id));
    }
}