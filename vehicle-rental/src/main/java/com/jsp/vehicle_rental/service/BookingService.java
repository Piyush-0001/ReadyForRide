
package com.jsp.vehicle_rental.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jsp.vehicle_rental.dto.BookingRequest;
import com.jsp.vehicle_rental.dto.BookingResponse;
import com.jsp.vehicle_rental.entity.AppUser;
import com.jsp.vehicle_rental.entity.Booking;
import com.jsp.vehicle_rental.entity.Vehicle;
import com.jsp.vehicle_rental.repo.AppUserRepo;
import com.jsp.vehicle_rental.repo.BookingRepo;
import com.jsp.vehicle_rental.repo.VehicleRepo;

@Service
public class BookingService {

    private final BookingRepo bookingRepo;
    private final VehicleRepo vehicleRepo;
    private final AppUserRepo userRepo;

    public BookingService(BookingRepo bookingRepo,
                          VehicleRepo vehicleRepo,
                          AppUserRepo userRepo) {
        this.bookingRepo = bookingRepo;
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public BookingResponse createBooking(
            Long userId, BookingRequest request) {

        LocalDate start = request.startDate();
        LocalDate end = request.endDate();

        if (start.isBefore(LocalDate.now()) ||
            !end.isAfter(start)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid booking dates");
        }

        AppUser user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "User not found"));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Account disabled");
        }

        // Lock the vehicle before checking date conflicts.
        Vehicle vehicle = vehicleRepo
            .findLockedById(request.vehicleId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Vehicle not found"));

        if (!vehicle.isActive()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Vehicle is unavailable");
        }

        Instant now = Instant.now();

        long conflicts = bookingRepo.countConflicts(
            vehicle.getId(), start, end, now
        );

        if (conflicts > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Vehicle already booked for these dates");
        }

        long days = ChronoUnit.DAYS.between(start, end);

        BigDecimal total = vehicle.getDailyRate()
            .multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setTotalAmount(total);
        booking.setStatus("PENDING_PAYMENT");
        booking.setCreatedAt(now);
        booking.setHoldExpiresAt(
            now.plus(15, ChronoUnit.MINUTES)
        );

        Booking saved = bookingRepo.saveAndFlush(booking);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getMyBooking(
            Long userId, Long bookingId) {

        Booking booking = bookingRepo.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found");
        }

        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {

        Vehicle vehicle = booking.getVehicle();

        return new BookingResponse(
            booking.getId(),
            vehicle.getId(),
            vehicle.getMake() + " " + vehicle.getModel(),
            booking.getStartDate(),
            booking.getEndDate(),
            booking.getTotalAmount(),
            booking.getStatus(),
            booking.getHoldExpiresAt()
        );
    }
}
