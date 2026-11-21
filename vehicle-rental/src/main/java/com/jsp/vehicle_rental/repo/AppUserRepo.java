package com.jsp.vehicle_rental.repo;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.vehicle_rental.entity.AppUser;

public interface AppUserRepo
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}

