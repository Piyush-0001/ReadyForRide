
package com.jsp.vehicle_rental.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentVerifyRequest(
    @NotBlank String razorpayOrderId,
    @NotBlank String razorpayPaymentId,
    @NotBlank String razorpaySignature
) {}
