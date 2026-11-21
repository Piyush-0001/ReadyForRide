
package com.jsp.vehicle_rental.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.jsp.vehicle_rental.dto.BookingRequest;
import com.jsp.vehicle_rental.dto.BookingResponse;
import com.jsp.vehicle_rental.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BookingRequest request) {

        Long userId = Long.valueOf(jwt.getSubject());

        return bookingService.createBooking(userId, request);
    }

    @GetMapping("/{id}")
    public BookingResponse getMyBooking(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id) {

        Long userId = Long.valueOf(jwt.getSubject());

        return bookingService.getMyBooking(userId, id);
    }
}
