
package com.jsp.vehicle_rental.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import com.jsp.vehicle_rental.dto.*;
import com.jsp.vehicle_rental.entity.Booking;
import com.jsp.vehicle_rental.repo.BookingRepo;
import com.jsp.vehicle_rental.repo.VehicleRepo;

@Service
public class PaymentService {

    private final BookingRepo bookingRepo;
    private final VehicleRepo vehicleRepo;
    private final String keyId;
    private final String keySecret;

    public PaymentService(
            BookingRepo bookingRepo,
            VehicleRepo vehicleRepo,
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret) {

        this.bookingRepo = bookingRepo;
        this.vehicleRepo = vehicleRepo;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Transactional
    public PaymentOrderResponse createOrder(
            Long userId, Long bookingId) {

        Booking booking = bookingRepo.findLockedById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"));

        checkOwner(userId, booking);

        if (!booking.getUser().isActive()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Account disabled");
        }

        if (!"PENDING_PAYMENT".equals(booking.getStatus())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Booking not pending payment");
        }

        if (!booking.getHoldExpiresAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Booking hold expired");
        }

        long paise = booking.getTotalAmount()
            .multiply(BigDecimal.valueOf(100))
            .longValueExact();

        if (booking.getRazorpayOrderId() != null) {
            return new PaymentOrderResponse(
                bookingId,
                booking.getRazorpayOrderId(),
                keyId, paise, "INR");
        }

        try {
            RazorpayClient client =
                new RazorpayClient(keyId, keySecret);

            JSONObject request = new JSONObject();
            request.put("amount", paise);
            request.put("currency", "INR");
            request.put("receipt", "booking_" + bookingId);

            Order order = client.orders.create(request);

            String orderId = order.get("id");

            booking.setRazorpayOrderId(orderId);
            bookingRepo.saveAndFlush(booking);

            return new PaymentOrderResponse(
                bookingId, orderId, keyId, paise, "INR");

        } catch (Exception ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not create Razorpay order", ex);
        }
    }

    @Transactional
    public BookingResponse verifyPayment(
            Long userId,
            Long bookingId,
            PaymentVerifyRequest request) {

        // Read the vehicle ID before acquiring locks.
        Booking initial = bookingRepo.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"));

        checkOwner(userId, initial);

        // Use the same vehicle lock as booking creation.
        vehicleRepo.findLockedById(initial.getVehicle().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Vehicle not found"));

        Booking booking = bookingRepo.findLockedById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found"));

        checkOwner(userId, booking);

        if (!booking.getUser().isActive()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Account disabled");
        }

        if (!Objects.equals(
                booking.getRazorpayOrderId(),
                request.razorpayOrderId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Order ID mismatch");
        }

        // Allow safe retries of an already confirmed payment.
        if ("CONFIRMED".equals(booking.getStatus())) {
            if (Objects.equals(
                    booking.getRazorpayPaymentId(),
                    request.razorpayPaymentId())) {
                return toResponse(booking);
            }
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Booking already confirmed");
        }

        if (!"PENDING_PAYMENT".equals(booking.getStatus())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Booking cannot be confirmed");
        }

        try {
            JSONObject attributes = new JSONObject();
            attributes.put(
                "razorpay_order_id",
                booking.getRazorpayOrderId());
            attributes.put(
                "razorpay_payment_id",
                request.razorpayPaymentId());
            attributes.put(
                "razorpay_signature",
                request.razorpaySignature());

            boolean valid = Utils.verifyPaymentSignature(
                attributes, keySecret);

            if (!valid) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment signature");
            }

            RazorpayClient client =
                new RazorpayClient(keyId, keySecret);

            Payment payment = client.payments.fetch(
                request.razorpayPaymentId());

            String paymentOrderId = payment.get("order_id");
            String paymentStatus = payment.get("status");
            String currency = payment.get("currency");
            Number paidAmount = payment.get("amount");

            long expectedPaise = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

            if (!booking.getRazorpayOrderId()
                    .equals(paymentOrderId)
                || !"captured".equals(paymentStatus)
                || !"INR".equals(currency)
                || paidAmount == null
                || paidAmount.longValue() != expectedPaise) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment not captured or details mismatch");
            }

            if (bookingRepo.existsByRazorpayPaymentId(
                    request.razorpayPaymentId())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment ID already used");
            }

            if (!booking.getHoldExpiresAt()
                    .isAfter(Instant.now())) {

                // Never confirm an expired hold automatically.
                // Manual reconciliation/refund is needed.
                booking.setStatus("PAYMENT_REVIEW");
                booking.setRazorpayPaymentId(
                    request.razorpayPaymentId());

            } else {
                booking.setStatus("CONFIRMED");
                booking.setRazorpayPaymentId(
                    request.razorpayPaymentId());
            }

            bookingRepo.saveAndFlush(booking);

            return toResponse(booking);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Payment verification failed", ex);
        }
    }

    private void checkOwner(Long userId, Booking booking) {
        if (!booking.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Booking not found");
        }
    }

    private BookingResponse toResponse(Booking booking) {
        var v = booking.getVehicle();

        return new BookingResponse(
            booking.getId(),
            v.getId(),
            v.getMake() + " " + v.getModel(),
            booking.getStartDate(),
            booking.getEndDate(),
            booking.getTotalAmount(),
            booking.getStatus(),
            booking.getHoldExpiresAt()
        );
    }
}
