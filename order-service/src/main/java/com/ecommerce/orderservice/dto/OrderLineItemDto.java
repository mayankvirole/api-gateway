package com.ecommerce.orderservice.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderLineItemDto(
    @NotNull Long productId,
    @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
    @NotNull @Positive Integer quantity
) {}
