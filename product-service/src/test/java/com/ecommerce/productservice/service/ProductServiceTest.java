package com.ecommerce.productservice.service;

import com.ecommerce.common.exception.InsufficientInventoryException;
import com.ecommerce.common.exception.ProductNotFoundException;
import com.ecommerce.productservice.dto.ProductRequest;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Test
    void getProductById_missingProductThrowsTypedException() {
        ProductService service = new ProductService(productRepository);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.getProductById(99L));
    }

    @Test
    void updateInventory_rejectsNegativeStock() {
        ProductService service = new ProductService(productRepository);
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product("Keyboard", "", BigDecimal.TEN, 2)));

        assertThrows(InsufficientInventoryException.class, () -> service.updateInventory(1L, -3));
    }

    @Test
    void createProduct_persistsRequestValues() {
        ProductService service = new ProductService(productRepository);
        Product saved = new Product("Mouse", "Wireless", BigDecimal.valueOf(25), 5);
        saved.setId(10L);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        var response = service.createProduct(new ProductRequest("Mouse", "Wireless", BigDecimal.valueOf(25), 5));

        assertEquals(10L, response.id());
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertEquals("Mouse", captor.getValue().getName());
    }
}
