package com.ecommerce.orderservice.dto;
import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
    Long id,
    Long userId,
    String orderStatus,
    BigDecimal totalPrice,
    List<OrderLineItemDto> orderLineItems
) {}
