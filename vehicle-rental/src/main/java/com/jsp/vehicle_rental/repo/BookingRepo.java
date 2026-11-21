
package com.jsp.vehicle_rental.repo;

import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jsp.vehicle_rental.entity.Booking;

public interface BookingRepo extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.vehicle.id = :vehicleId
          AND b.startDate < :endDate
          AND b.endDate > :startDate
          AND (
              b.status = 'CONFIRMED'
              OR (
                  b.status = 'PENDING_PAYMENT'
                  AND b.holdExpiresAt > :now
              )
          )
        """)
    long countConflicts(
        @Param("vehicleId") Long vehicleId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("now") Instant now
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findLockedById(@Param("id") Long id);

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}
