package com.ecommerce.common.exception;

/**
 * Exception thrown when a payment operation fails.
 */
public class PaymentException extends BusinessException {
    
    public PaymentException(String message) {
        super("PAYMENT_FAILED", message);
    }
    
    public PaymentException(String errorCode, String message) {
        super(errorCode, message);
    }
}
