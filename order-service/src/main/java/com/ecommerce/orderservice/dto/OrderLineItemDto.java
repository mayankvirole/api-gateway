package com.ecommerce.orderservice.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderLineItemDto(
    @NotNull Long productId,
    @NotNull @Min(0) BigDecimal price,
    @NotNull @Min(1) Integer quantity
) {}
