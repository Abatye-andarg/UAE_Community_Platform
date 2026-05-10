package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.dto.Sec103_1093910_FamilyDTO;
import com.abatye.family_help_uae.model.Sec103_1093910_Family;
import com.abatye.family_help_uae.service.Sec103_1093910_FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for family profile management.
 */
@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
@Tag(name = "Family", description = "Endpoints for family profile management")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_FamilyController {

    private final Sec103_1093910_FamilyService familyService;

    private Sec103_1093910_FamilyDTO convertToDTO(Sec103_1093910_Family family) {
        return Sec103_1093910_FamilyDTO.builder()
                .id(family.getId())
                .familyName(family.getFamilyName())
                .email(family.getEmail())
                .address(family.getAddress())
                .phone(family.getPhone())
                .role(family.getRole())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "List all families", description = "Returns a list of all registered families.")
    @ApiResponse(responseCode = "200", description = "Families retrieved successfully")
    public ResponseEntity<List<Sec103_1093910_FamilyDTO>> getAll() {
        List<Sec103_1093910_FamilyDTO> families = familyService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(families);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get a family by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Family found"),
            @ApiResponse(responseCode = "404", description = "Family not found")
    })
    public ResponseEntity<Sec103_1093910_FamilyDTO> getById(
            @Parameter(description = "Family ID") @PathVariable Long id) {
        return familyService.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Find a family by email address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Family found"),
            @ApiResponse(responseCode = "404", description = "No family with that email")
    })
    public ResponseEntity<Sec103_1093910_FamilyDTO> getByEmail(
            @Parameter(description = "Email address") @RequestParam String email) {
        return familyService.findByEmail(email)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update a family profile", description = "Updates name, address, phone, and email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Family not found")
    })
    public ResponseEntity<Sec103_1093910_FamilyDTO> update(
            @Parameter(description = "Family ID") @PathVariable Long id,
            @RequestBody Sec103_1093910_Family updated) {
        return ResponseEntity.ok(convertToDTO(familyService.update(id, updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete a family account")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Family deleted"),
            @ApiResponse(responseCode = "404", description = "Family not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Family ID") @PathVariable Long id) {
        familyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
