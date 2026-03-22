package code.vanilson.marketplace.mapper;

import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductMapper {

    private ProductMapper() {
        //default constructor
    }

    public static ProductDto toProductDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDto dto = new ProductDto(product.getProductId(), product.getName(), product.getQuantity(), product.getVersion());
        dto.setPrice(product.getPrice());
        return dto;
    }

    public static List<ProductDto> toProductDtoList(List<Product> productList) {
        return productList.stream()
                .map(ProductMapper::toProductDto)
                .collect(Collectors.toList());
    }

    public static List<Product> toProductList(List<ProductDto> productList) {
        return productList.stream()
                .map(ProductMapper::toProduct)
                .collect(Collectors.toList());
    }

    // Method to convert ProductDto to Product
    public static Product toProduct(ProductDto productDto) {
        if (productDto == null) {
            return null;
        }

        Product product = new Product();
        product.setProductId(productDto.getProductId());
        product.setName(productDto.getName());
        product.setQuantity(productDto.getQuantity());
        product.setVersion(productDto.getVersion());
        if (productDto.getPrice() != null) product.setPrice(productDto.getPrice());

        return product;
    }

    public static Optional<ProductDto> toProduct(Optional<Product> productOptional) {
        return productOptional.map(product ->
        {
                ProductDto dto = new ProductDto(product.getProductId(), product.getName(), product.getQuantity(), product.getVersion());
                dto.setPrice(product.getPrice());
                return dto;
        });
    }
}


