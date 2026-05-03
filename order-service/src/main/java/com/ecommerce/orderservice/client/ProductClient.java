package com.ecommerce.orderservice.client;

import com.ecommerce.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.dto.InventoryUpdateRequest;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/v1/products/{id}/inventory")
    void updateInventory(@PathVariable("id") Long id, @RequestBody InventoryUpdateRequest request);

}
