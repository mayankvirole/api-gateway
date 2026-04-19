package com.ecommerce.productservice.dto;
import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer inventory
) implements java.io.Serializable {}
