package com.jsp.vehicle_rental.controller;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.vehicle_rental.entity.Vehicle;
import com.jsp.vehicle_rental.service.VehicleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = {
	    "http://10.116.42.127:5500",
	    "http://localhost:5500",
	    "http://127.0.0.1:5500"
	})
public class VehicleController {
	@Autowired
	private VehicleService service;
	
	@GetMapping
	public List<Vehicle> getAllVehicles(){
		return service.findAll();
	}
	
	@GetMapping("/{id}")
	public Vehicle findById(@PathVariable Long id) {
		return service.findById(id);
	}
	
	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle createVehicle(@Valid @RequestBody Vehicle vehicle) {
        return service.save(vehicle);
    }
	
	@PutMapping("/{id}")
    public Vehicle updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody Vehicle vehicle) {
        return service.update(id, vehicle);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateVehicle(@PathVariable Long id) {
        service.deactivate(id);
    }
}
