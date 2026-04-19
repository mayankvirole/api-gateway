package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.OrderLineItemDto;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderLineItem;
import com.ecommerce.orderservice.event.OrderPlacedEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.dto.ProductDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, Object> kafkaTemplate, ProductClient productClient) {
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
                    ProductDto product = productClient.getProductById(itemDto.productId());
                    if (product.inventory() < itemDto.quantity()) {
                        throw new IllegalArgumentException("Not enough inventory for product: " + product.name());
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

        // Send Domain Event to Kafka
        OrderPlacedEvent event = new OrderPlacedEvent(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalPrice(), savedOrder.getOrderStatus());
        kafkaTemplate.send("order-placed-topic", event);

        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderLineItemDto> dtos = order.getOrderLineItems().stream()
                .map(item -> new OrderLineItemDto(item.getProductId(), item.getPrice(), item.getQuantity()))
                .toList();
        return new OrderResponse(order.getId(), order.getUserId(), order.getOrderStatus(), order.getTotalPrice(), dtos);
    }
}
