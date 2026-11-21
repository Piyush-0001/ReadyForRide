
package com.jsp.vehicle_rental.dto;

public record PaymentOrderResponse(
    Long bookingId,
    String razorpayOrderId,
    String keyId,
    long amount,
    String currency
) {}
