package com.jsp.vehicle_rental.dto;

public record AuthResponse(
    String token,
    String tokenType,
    Long userId,
    String fullName,
    String role
) {}
