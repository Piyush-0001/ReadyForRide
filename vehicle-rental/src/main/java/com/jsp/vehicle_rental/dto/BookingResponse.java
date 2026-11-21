
package com.jsp.vehicle_rental.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
    Long bookingId,
    Long vehicleId,
    String vehicleName,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalAmount,
    String status,
    Instant holdExpiresAt
) {}
