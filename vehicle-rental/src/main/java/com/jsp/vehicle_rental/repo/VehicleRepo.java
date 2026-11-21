package com.jsp.vehicle_rental.repo;

import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.vehicle_rental.entity.Vehicle;

public interface VehicleRepo extends JpaRepository<Vehicle, Long>{
	boolean existsByInventoryCode(String inventoryCode);
	List<Vehicle> findByActiveTrue();
	List<Vehicle> findByCategoryIgnoreCaseAndActiveTrue(String category);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT v FROM Vehicle v WHERE v.id = :id")
	Optional<Vehicle> findLockedById(@Param("id") Long id);
}
