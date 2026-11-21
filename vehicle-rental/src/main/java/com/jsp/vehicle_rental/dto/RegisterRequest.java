package com.jsp.vehicle_rental.dto;


import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank String fullName,
    @NotBlank @Email String email,
    @NotBlank String phone,
    @NotBlank @Size(min = 8) String password
) {}

