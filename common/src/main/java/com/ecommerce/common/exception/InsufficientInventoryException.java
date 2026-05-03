package com.ecommerce.common.exception;

/**
 * Exception thrown when there is insufficient inventory.
 */
public class InsufficientInventoryException extends BusinessException {
    
    public InsufficientInventoryException(Long productId, int requested, int available) {
        super("INSUFFICIENT_INVENTORY", 
              String.format("Insufficient inventory for product %d. Requested: %d, Available: %d", 
                          productId, requested, available));
    }
    
    public InsufficientInventoryException(String message) {
        super("INSUFFICIENT_INVENTORY", message);
    }
}
