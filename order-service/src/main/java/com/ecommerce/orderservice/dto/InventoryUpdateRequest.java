package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryUpdateRequest(@NotNull Integer quantityChange) {
}
