package com.ecommerce.common.exception;

/**
 * Exception thrown when an order is not found.
 */
public class OrderNotFoundException extends BusinessException {
    
    public OrderNotFoundException(Long orderId) {
        super("ORDER_NOT_FOUND", 
              String.format("Order with ID %d not found", orderId));
    }
    
    public OrderNotFoundException(String message) {
        super("ORDER_NOT_FOUND", message);
    }
}
