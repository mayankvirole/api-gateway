package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.dto.InventoryUpdateRequest;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ProductClientFallback implements ProductClient {
    @Override
    public ProductDto getProductById(Long id) {
        // Return a default or "unavailable" product response
        return new ProductDto(id, "Service Unavailable", "Product service is currently down", BigDecimal.ZERO, 0);
    }

    @Override
    public void updateInventory(Long id, InventoryUpdateRequest request) {
        // Log or handle fallback for inventory update
        System.err.println("Product service unavailable, cannot update inventory for product " + id);
    }
}
