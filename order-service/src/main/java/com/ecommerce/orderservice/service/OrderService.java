package com.ecommerce.orderservice.service;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.event.OrderLineItemEvent;
import com.ecommerce.common.event.OrderPlacedEvent;
import com.ecommerce.common.event.PaymentProcessedEvent;
import com.ecommerce.common.exception.InsufficientInventoryException;
import com.ecommerce.common.exception.OrderNotFoundException;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.OrderLineItemDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderLineItem;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.ProductDto;
import com.ecommerce.orderservice.dto.InventoryUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final KafkaOperations<String, Object> kafkaTemplate;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, KafkaOperations<String, Object> kafkaTemplate, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.userId());
        order.setOrderStatus("PENDING");

        List<OrderLineItem> lineItems = orderRequest.orderLineItems()
                .stream()
                .map(itemDto -> {
                    ApiResponse<ProductDto> productResponse = productClient.getProductById(itemDto.productId());
                    ProductDto product = productResponse.getData();
                    if (product == null) {
                        throw new OrderNotFoundException("Product service returned no product for id: " + itemDto.productId());
                    }
                    if (product.inventory() < itemDto.quantity()) {
                        throw new InsufficientInventoryException("Not enough inventory for product: " + product.name());
                    }

                    OrderLineItem item = new OrderLineItem();
                    item.setProductId(product.id());
                    item.setPrice(product.price());
                    item.setQuantity(itemDto.quantity());
                    item.setOrder(order);
                    return item;
                }).collect(Collectors.toList());

        order.setOrderLineItems(lineItems);
        
        BigDecimal total = lineItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        // Update product inventories
        for (OrderLineItem item : lineItems) {
            productClient.updateInventory(item.getProductId(), new InventoryUpdateRequest(-item.getQuantity()));
        }

        // Send Domain Event to Kafka
        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderStatus(),
                lineItems.stream()
                        .map(item -> new OrderLineItemEvent(item.getProductId(), item.getPrice(), item.getQuantity()))
                        .toList()
        );
        kafkaTemplate.send("order-placed-topic", event);
        log.info("Published order placed event for orderId={} amount={}", savedOrder.getId(), savedOrder.getTotalPrice());

        return mapToResponse(savedOrder);
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "order-saga-group")
    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        Order order = orderRepository.findWithOrderLineItemsById(event.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + event.getOrderId()));

        if ("SUCCESS".equals(order.getOrderStatus()) || "PAYMENT_FAILED".equals(order.getOrderStatus())) {
            log.info("Ignoring already finalized orderId={} status={}", order.getId(), order.getOrderStatus());
            return;
        }

        if ("SUCCESS".equals(event.getStatus())) {
            order.setOrderStatus("SUCCESS");
            orderRepository.save(order);
            log.info("Order marked successful orderId={}", order.getId());
            return;
        }

        order.setOrderStatus("PAYMENT_FAILED");
        orderRepository.save(order);
        for (OrderLineItem item : order.getOrderLineItems()) {
            productClient.updateInventory(item.getProductId(), new InventoryUpdateRequest(item.getQuantity()));
        }
        log.warn("Payment failed for orderId={}; inventory compensation completed", order.getId());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderLineItemDto> dtos = order.getOrderLineItems().stream()
                .map(item -> new OrderLineItemDto(item.getProductId(), item.getPrice(), item.getQuantity()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getOrderStatus(), order.getTotalPrice(), dtos);
    }
}
