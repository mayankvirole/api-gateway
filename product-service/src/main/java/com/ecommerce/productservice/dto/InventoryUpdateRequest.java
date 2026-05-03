package com.ecommerce.productservice.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryUpdateRequest(@NotNull Integer quantityChange) {
}
