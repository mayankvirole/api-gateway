package com.ecommerce.common.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", 
              String.format("User with ID %d not found", userId));
    }
    
    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }
}
