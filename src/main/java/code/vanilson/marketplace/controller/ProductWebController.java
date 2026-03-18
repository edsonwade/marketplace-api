package code.vanilson.marketplace.controller;

import code.vanilson.marketplace.dto.ProductDto;
import code.vanilson.marketplace.exception.ObjectWithIdNotFound;
import code.vanilson.marketplace.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductWebController {

    public static final String REDIRECT_PRODUCTS = "redirect:/products";
    private final ProductService productService;

    public ProductWebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        return "product-list";
    }

    @GetMapping("/create")
    public String createProductForm(Model model) {
        model.addAttribute("product", new ProductDto());
        return "product-form";
    }

    @PostMapping("/create")
    public String createProduct(ProductDto product) {
        productService.save(product);
        return REDIRECT_PRODUCTS;
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Integer id, Model model) {
        Optional<ProductDto> product = productService.findById(id);

        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "edit-product"; // Assuming your Thymeleaf template is named "edit-product.html"
        } else {
            // Handle the case where the product with the given ID doesn't exist
            return REDIRECT_PRODUCTS; // Redirect to the product list page or show an error message
        }
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@ModelAttribute ProductDto product, @PathVariable Integer id) {
        // Validate the product ID
        Optional<ProductDto> existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            // Handle the case where the product with the given ID doesn't exist
            // You can return an error page or redirect to an error page
            throw new ObjectWithIdNotFound("Product with id: " + id + " not found"); // Replace with your error page URL
        }

        // Logic to update the product goes here
        // Make sure to handle the update appropriately
        productService.update(product);

        return REDIRECT_PRODUCTS; // Redirect to the product list page after updating
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        // Delete the product by its ID
        productService.delete(id);

        return REDIRECT_PRODUCTS; // Redirect to the product list page after deleting
    }

}