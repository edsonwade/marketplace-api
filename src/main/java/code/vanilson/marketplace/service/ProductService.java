package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.ProductDto;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    /**
     * Returns all products in the database.
     *
     * @return All products in the database.
     */
    List<ProductDto> findAllProducts();

    /**
     * Returns the product with the specified id.
     *
     * @param id ID of the product to retrieve.
     * @return The requested Product if found.
     */
    Optional<ProductDto> findById(long id);

    /**
     * Updates the specified product, identified by its id.
     *
     * @param product The product to update.
     * @return True if the update succeeded, otherwise false.
     */
    boolean update(ProductDto product);

    /**
     * Saves the specified product to the database.
     *
     * @param product The product to save to the database.
     * @return The saved product.
     */
    ProductDto save(ProductDto product);

    /**
     * Deletes the product with the specified id.
     *
     * @param id The id of the product to delete.
     * @return True if the operation was successful.
     */
    boolean delete(long id);
}
