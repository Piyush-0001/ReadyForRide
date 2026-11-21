
package com.jsp.vehicle_rental.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.jsp.vehicle_rental.dto.*;
import com.jsp.vehicle_rental.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{bookingId}/order")
    public PaymentOrderResponse createOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long bookingId) {

        Long userId = Long.valueOf(jwt.getSubject());

        return paymentService.createOrder(userId, bookingId);
    }

    @PostMapping("/{bookingId}/verify")
    public BookingResponse verifyPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentVerifyRequest request) {

        Long userId = Long.valueOf(jwt.getSubject());

        return paymentService.verifyPayment(
            userId, bookingId, request);
    }
}
