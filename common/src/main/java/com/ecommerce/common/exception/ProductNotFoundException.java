package com.ecommerce.common.exception;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends BusinessException {
    
    public ProductNotFoundException(Long productId) {
        super("PRODUCT_NOT_FOUND", 
              String.format("Product with ID %d not found", productId));
    }
    
    public ProductNotFoundException(String message) {
        super("PRODUCT_NOT_FOUND", message);
    }
}
