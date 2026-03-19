package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository repository;

    @Test
    void testFindAll() {
        Product p1 = new Product("Test Computer", 10);
        Product p2 = new Product("Test Keyboard", 5);
        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.flush();

        List<Product> products = repository.findAll();
        assertNotNull(products);
        assertTrue(products.size() >= 2);
    }

    @Test
    void testFindByIdSuccess() {
        Product product = new Product("Computer", 10);
        entityManager.persist(product);
        entityManager.flush();

        Optional<Product> found = repository.findById(product.getProductId());
        assertTrue(found.isPresent());
        assertEquals("Computer", found.get().getName());
        assertEquals(10, found.get().getQuantity());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Product> product = repository.findById(999L);
        assertTrue(product.isEmpty());
    }

    @Test
    void testSaveProduct() {
        Product product = new Product("New Product", 50);
        Product saved = repository.save(product);
        assertNotNull(saved.getProductId());
        assertEquals("New Product", saved.getName());
        assertEquals(50, saved.getQuantity());
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product("Original", 10);
        entityManager.persist(product);
        entityManager.flush();

        product.setName("Updated");
        product.setQuantity(20);
        Product updated = repository.save(product);
        assertEquals("Updated", updated.getName());
        assertEquals(20, updated.getQuantity());
    }

    @Test
    void testDeleteProduct() {
        Product product = new Product("To Delete", 5);
        entityManager.persist(product);
        entityManager.flush();

        repository.delete(product);
        Optional<Product> deleted = repository.findById(product.getProductId());
        assertTrue(deleted.isEmpty());
    }
}
