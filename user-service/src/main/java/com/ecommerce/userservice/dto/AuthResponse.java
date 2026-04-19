package com.ecommerce.userservice.dto;

public record AuthResponse(String token, String type, long expiresIn, UserDto user) {}
