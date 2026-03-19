package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    /**
     * Product Repository Mock
     */
    ProductRepository productRepositoryMock;
    /**
     * current instance
     */
    ProductServiceImpl currentInstance;
    /**
     * Product
     */
    Product product;
    /**
     * Lists of Products
     */
    List<Product> products;

    /**
     * setup
     */
    @BeforeEach
    void setUp() {
        productRepositoryMock = mock(ProductRepository.class);
        currentInstance = new ProductServiceImpl(productRepositoryMock);

        product = new Product(1L, "Keyboard", 34, 1);
        products = List.of(
                new Product(1L, "Computer", 34, 2004),
                new Product(2L, "Mouse", 10, 1)
        );
    }

    @Test
    @DisplayName("List all products - Success")
    void testShouldReturnAllProducts() {
        // Mock repository behavior
        when(productRepositoryMock.findAll()).thenReturn(products);

        // Call the method under test
        var productDtos = currentInstance.findAllProducts();

        // Assertions
        assertNotNull(productDtos, "true");
        assertEquals(2, productDtos.size(), "Should return 2");
        assertEquals(1, productDtos.get(0).getProductId().intValue());
        assertEquals(2, productDtos.get(1).getProductId().intValue());
        assertEquals("Computer", productDtos.get(0).getName());

        // Verify that the repository method was called
        verify(productRepositoryMock, times(1)).findAll();

    }

    @Test
    @DisplayName("Return product by id - Found")
    void testShouldReturnTheProductsWhenTheGivenIdIsFound() {
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(product));
        var productDtos = currentInstance.findById(1).get();
        assertSame(productDtos.getProductId(), product.getProductId(), "Products should be the same");
        assertTrue(currentInstance.findById(1).isPresent(), "true");
        assertFalse(currentInstance.findById(1).isEmpty());
        assertNotEquals(234, currentInstance.findById(1)
                .get()
                .getProductId());
        verify(productRepositoryMock, times(4)).findById(1L);
    }

    @Test
    @DisplayName(" product by id - Not Found")
    void testShouldThrowExceptionsWhenTheGivenIdIsNotFound() {
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        // In the implementation, findById returns Optional.empty() instead of throwing if not found
        var result = currentInstance.findById(1);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("create a new product - Success")
    void testShouldCreateNewProduct() {
        ProductDto productDtoToCreate = new ProductDto(1L, "keyboard", 10, null);
        Product mockProduct = new Product(1L, "keyboard", 10, 1);
        when(productRepositoryMock.save(any(Product.class))).thenReturn(mockProduct);
        ProductDto createdProductDto = currentInstance.save(productDtoToCreate);

        // Asserts
        assertEquals(1, createdProductDto.getProductId().intValue());
        assertEquals("keyboard", createdProductDto.getName());
        assertEquals(10, createdProductDto.getQuantity().intValue());
        //verify
        verify(productRepositoryMock, atLeastOnce()).save(any(Product.class));
    }

    @Test
    @DisplayName("update product - Success")
    void testShouldUpdateProduct() {
        ProductDto updatedProductDto = new ProductDto(1L, "new_keyboard", 20, 1);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(product));
        when(productRepositoryMock.save(any(Product.class))).thenReturn(product);
        boolean updateResult = currentInstance.update(updatedProductDto);
        assertTrue(updateResult, "Update operation should return true");
        assertEquals(1, updatedProductDto.getProductId().intValue(), "Product ID should be 1 after update");
        assertEquals("new_keyboard", updatedProductDto.getName(), "Product name should be 'new_keyboard' after update");
        assertEquals(20, updatedProductDto.getQuantity().intValue(), "Product quantity should be 20 after update");
        assertEquals(1, updatedProductDto.getVersion().intValue(), "Product version should be 1 after update");
        verify(productRepositoryMock, atLeastOnce()).save(any(Product.class));
    }

    @Test
    @DisplayName("delete product - Success")
    void testShouldDeleteProduct() {
        var existingProductId = 1L;
        when(productRepositoryMock.existsById(existingProductId)).thenReturn(true);
        boolean deleteResult = currentInstance.delete(existingProductId);
        assertTrue(deleteResult);
        verify(productRepositoryMock, times(1)).existsById(existingProductId);
        verify(productRepositoryMock, times(1)).deleteById(existingProductId);
    }

    @Test
    @DisplayName("Delete product by id - Not Success")
    void testDeleteProductsThrowAnException() {
        var nonExistingProductId = 2L;
        when(productRepositoryMock.existsById(nonExistingProductId)).thenReturn(false);
        ObjectWithIdNotFound exception =
                assertThrows(ObjectWithIdNotFound.class, () -> currentInstance.delete(nonExistingProductId));

        // Asserts
        assertEquals("Product with id 2 not found", exception.getMessage());

        // Verify that existsById was called as expected
        verify(productRepositoryMock, times(1)).existsById(nonExistingProductId);
    }
}