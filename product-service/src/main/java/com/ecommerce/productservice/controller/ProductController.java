package com.ecommerce.productservice.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.productservice.dto.ProductDto;
import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.dto.InventoryUpdateRequest;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(productService.createProduct(request)));
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<Void> updateInventory(@PathVariable("id") Long id, @Valid @RequestBody InventoryUpdateRequest request) {
        productService.updateInventory(id, request.quantityChange());
        return ResponseEntity.ok().build();
    }
}
