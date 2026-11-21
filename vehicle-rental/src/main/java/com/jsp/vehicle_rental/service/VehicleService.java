package com.jsp.vehicle_rental.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jsp.vehicle_rental.entity.Vehicle;
import com.jsp.vehicle_rental.repo.VehicleRepo;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VehicleService {
	@Autowired
	VehicleRepo vehicleRepo;

	public List<Vehicle> findAll() {
		return vehicleRepo.findByActiveTrue();
	}

	public Vehicle findById(Long id) {
		return vehicleRepo.findById(id).filter(Vehicle::isActive)
				.orElseThrow(() -> new EntityNotFoundException("Vehicle Not found"));
	}

	public Vehicle save(Vehicle vehicle) {
		if (vehicle.getId() == null && vehicleRepo.existsByInventoryCode(vehicle.getInventoryCode())) {
			throw new IllegalArgumentException("Inventory code already exists");
		}

		return vehicleRepo.save(vehicle);
	}
	
	public Vehicle update(Long id, Vehicle updated) {
        Vehicle existing = vehicleRepo.findById(id)
                .orElseThrow(() ->
                    new EntityNotFoundException("Vehicle not found"));

        existing.setMake(updated.getMake());
        existing.setModel(updated.getModel());
        existing.setCategory(updated.getCategory());
        existing.setTransmission(updated.getTransmission());
        existing.setSeats(updated.getSeats());
        existing.setDescription(updated.getDescription());
        existing.setImageUrl(updated.getImageUrl());
        existing.setDailyRate(updated.getDailyRate());
        

        return vehicleRepo.save(existing);
    }
	
	public void deactivate(Long id) {
		Vehicle vehicle = vehicleRepo.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
		vehicle.setActive(false);
		vehicleRepo.save(vehicle);
	}

}
