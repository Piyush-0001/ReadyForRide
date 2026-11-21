
package com.jsp.vehicle_rental.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jsp.vehicle_rental.dto.*;
import com.jsp.vehicle_rental.entity.AppUser;
import com.jsp.vehicle_rental.repo.AppUserRepo;

@Service
public class AuthService {

    private final AppUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(AppUserRepo userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder) {

        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.email()
            .trim().toLowerCase(Locale.ROOT);

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Email already registered");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPassword(
            passwordEncoder.encode(request.password())
        );

        // Never accept the role from the frontend.
        user.setRole("CUSTOMER");
        user.setActive(true);

        AppUser saved = userRepo.saveAndFlush(user);

        return generateToken(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String email = request.email()
            .trim().toLowerCase(Locale.ROOT);

        AppUser user = userRepo
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
            ));

        if (!user.isActive() ||
            !passwordEncoder.matches(
                request.password(), user.getPassword()
            )) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
            );
        }

        return generateToken(user);
    }

    private AuthResponse generateToken(AppUser user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("readyforride")
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(now.plus(2, ChronoUnit.HOURS))
            .claim("role", user.getRole())
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build();

        String token = jwtEncoder.encode(
            JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        return new AuthResponse(
            token,
            "Bearer",
            user.getId(),
            user.getFullName(),
            user.getRole()
        );
    }
}
