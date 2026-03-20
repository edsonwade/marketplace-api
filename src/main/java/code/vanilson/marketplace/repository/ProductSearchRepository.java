package code.vanilson.marketplace.repository;

import code.vanilson.marketplace.model.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSearchRepository extends MongoRepository<ProductDocument, String> {
    Optional<ProductDocument> findByProductId(Long productId);
    List<ProductDocument> findByCategory(String category);
    List<ProductDocument> findByCategoryAndPriceBetween(String category, BigDecimal minPrice, BigDecimal maxPrice);
    List<ProductDocument> findByNameContainingIgnoreCase(String name);
    List<ProductDocument> findByIsActiveTrue();
    
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<ProductDocument> searchByName(String name);
    
    @Query("{ $text: { $search: ?0 } }")
    List<ProductDocument> fullTextSearch(String searchTerm);
}