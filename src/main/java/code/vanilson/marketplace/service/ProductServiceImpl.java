package code.vanilson.marketplace.service;

import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.mapper.ProductMapper;
import code.vanilson.marketplace.model.Product;
import code.vanilson.marketplace.repository.ProductRepository;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LogManager.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;


    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductDto> findAllProducts() {
        List<Product> products = productRepository.findAll();
        return ProductMapper.toProductDtoList(products);

    }

    @Override
    public Optional<ProductDto> findById(long id) {
        return productRepository.findById(id)
                .map(ProductMapper::toProductDto);
    }

    @Override
    public ProductDto save(@NotNull ProductDto productDto) {
        if (productDto == null) {
            throw new IllegalArgumentException("The 'product' object must not be null.");
        }
        Product product = ProductMapper.toProduct(productDto);
        product.setVersion(1);
        Product savedProduct = productRepository.save(product);
        logger.info("Saved product to the database: {}", savedProduct);
        return ProductMapper.toProductDto(savedProduct);
    }

    @Override
    public boolean update(ProductDto productDto) {
        if (productDto == null || productDto.getProductId() == null) {
            return false;
        }
        return productRepository.findById(productDto.getProductId())
                .map(existingProduct -> {
                    existingProduct.setName(productDto.getName());
                    existingProduct.setQuantity(productDto.getQuantity());
                    if (productDto.getPrice() != null) existingProduct.setPrice(productDto.getPrice());
                    existingProduct.setVersion(productDto.getVersion());
                    productRepository.save(existingProduct);
                    logger.info("Updated product: {}", productDto);
                    return true;
                }).orElse(false);
    }

    @Override
    public boolean delete(long id) {
        if (!productRepository.existsById(id)) {
            throw new ObjectWithIdNotFound(MessageFormat.format("Product with id {0} not found", id));
        }
        productRepository.deleteById(id);
        logger.info("Delete product with id: {}", id);
        return true;
    }

}