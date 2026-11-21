
package com.jsp.vehicle_rental.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
    @NotNull Long vehicleId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
