package com.ecommerce.orderservice.service;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.common.exception.InsufficientInventoryException;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.InventoryUpdateRequest;
import com.ecommerce.orderservice.dto.OrderLineItemDto;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderLineItem;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private KafkaOperations<String, Object> kafkaTemplate;
    @Mock
    private ProductClient productClient;

    @Test
    void placeOrder_rejectsInsufficientInventory() {
        OrderService service = new OrderService(orderRepository, kafkaTemplate, productClient);
        when(productClient.getProductById(10L)).thenReturn(ApiResponse.success(new ProductDto(10L, "Desk", "", BigDecimal.TEN, 1)));

        OrderRequest request = new OrderRequest(7L, List.of(new OrderLineItemDto(10L, null, 2)));

        assertThrows(InsufficientInventoryException.class, () -> service.placeOrder(request));
    }

    @Test
    void failedPayment_marksOrderFailedAndRestoresInventory() {
        OrderService service = new OrderService(orderRepository, kafkaTemplate, productClient);
        Order order = new Order();
        order.setId(33L);
        order.setOrderStatus("PENDING");
        OrderLineItem item = new OrderLineItem();
        item.setProductId(10L);
        item.setQuantity(4);
        item.setOrder(order);
        order.setOrderLineItems(List.of(item));
        when(orderRepository.findWithOrderLineItemsById(33L)).thenReturn(Optional.of(order));

        service.handlePaymentProcessed(new PaymentProcessedEvent(33L, 8L, "FAILED", "declined"));

        assertEquals("PAYMENT_FAILED", order.getOrderStatus());
        ArgumentCaptor<InventoryUpdateRequest> captor = ArgumentCaptor.forClass(InventoryUpdateRequest.class);
        verify(productClient).updateInventory(any(Long.class), captor.capture());
        assertEquals(4, captor.getValue().quantityChange());
    }
}
