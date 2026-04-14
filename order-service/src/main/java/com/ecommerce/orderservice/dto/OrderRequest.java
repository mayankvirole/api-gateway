package com.ecommerce.orderservice.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
    @NotNull Long userId,
    @NotEmpty List<OrderLineItemDto> orderLineItems
) {}
