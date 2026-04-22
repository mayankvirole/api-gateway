package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.ProductDto;
import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products")
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getInventory()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "product", key = "#id")
    public ProductDto getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getInventory());
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDto createProduct(ProductRequest request) {
        Product product = new Product(request.name(), request.description(), request.price(), request.inventory());
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getPrice(), saved.getInventory());
    }

    @Transactional
    @CacheEvict(value = {"products", "product"}, key = "#id")
    public void updateInventory(Long id, int quantityChange) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        int newInventory = product.getInventory() + quantityChange;
        if (newInventory < 0) {
            throw new IllegalArgumentException("Insufficient inventory");
        }
        product.setInventory(newInventory);
        productRepository.save(product);
    }
}
