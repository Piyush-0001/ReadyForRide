package com.jsp.vehicle_rental.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
@Data
@Entity
@Table(name = "vehicles")
public class Vehicle {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(unique = true, nullable = false)
	@NotBlank
    private String inventoryCode;
	
	@NotBlank
    private String make;
	
	@NotBlank
    private String model;
	
	@NotBlank
    private String category;
	
	@NotBlank
    private String transmission;
	
	@PositiveOrZero
    private Integer seats;
	
    private String description;
    
    private String imageUrl;
    
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal dailyRate;
    
    private boolean active = true;
    
}
