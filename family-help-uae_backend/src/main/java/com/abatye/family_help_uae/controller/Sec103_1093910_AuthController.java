package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.dto.Sec103_1093910_JwtResponse;
import com.abatye.family_help_uae.dto.Sec103_1093910_LoginRequest;
import com.abatye.family_help_uae.dto.Sec103_1093910_SignupRequest;
import com.abatye.family_help_uae.model.Sec103_1093910_Family;
import com.abatye.family_help_uae.security.jwt.Sec103_1093910_JwtUtils;
import com.abatye.family_help_uae.security.services.Sec103_1093910_UserDetailsImpl;
import com.abatye.family_help_uae.service.Sec103_1093910_FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication requests (Sign Up and Sign In).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for family registration and login")
public class Sec103_1093910_AuthController {

    private final AuthenticationManager authenticationManager;
    private final Sec103_1093910_FamilyService familyService;
    private final Sec103_1093910_JwtUtils jwtUtils;

    @PostMapping("/signin")
    @Operation(summary = "Authenticate a family", description = "Validates credentials and returns a JWT token.")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody Sec103_1093910_LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Sec103_1093910_UserDetailsImpl userDetails = (Sec103_1093910_UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new Sec103_1093910_JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getFamilyName()));
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new family", description = "Creates a new family account.")
    public ResponseEntity<?> registerUser(@Valid @RequestBody Sec103_1093910_SignupRequest signUpRequest) {
        if (familyService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new family account
        Sec103_1093910_Family family = Sec103_1093910_Family.builder()
                .familyName(signUpRequest.getFamilyName())
                .email(signUpRequest.getEmail())
                .passwordHash(signUpRequest.getPassword()) // Service will hash it
                .address(signUpRequest.getAddress())
                .phone(signUpRequest.getPhone())
                .role("ROLE_USER")
                .build();

        familyService.save(family);

        return ResponseEntity.ok("Family registered successfully!");
    }
}
