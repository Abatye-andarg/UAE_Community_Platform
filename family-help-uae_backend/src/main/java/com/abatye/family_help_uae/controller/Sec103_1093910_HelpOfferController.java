package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpOffer;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for help offer management.
 *
 * <p>Families use this to advertise the support services they can provide to the community,
 * such as tutoring, transportation, or childcare.
 * Offers follow a status lifecycle: {@code OPEN → ACTIVE → COMPLETED / CANCELLED}.</p>
 *
 */
@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Help Offer", description = "Post and manage support offers to the community")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_HelpOfferController {

    private final Sec103_1093910_HelpOfferService helpOfferService;

    // POST /api/offers

    @PostMapping
    @Operation(summary = "Post a new help offer",
               description = "A family advertises a service they can provide (e.g., tutoring, transport).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Offer created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Sec103_1093910_HelpOffer> create(
            @RequestBody Sec103_1093910_HelpOffer helpOffer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(helpOfferService.save(helpOffer));
    }

    // GET /api/offers

    @GetMapping
    @Operation(summary = "List all help offers",
               description = "Optionally filter by familyId, categoryId, or status via query parameters.")
    @ApiResponse(responseCode = "200", description = "Offers retrieved")
    public ResponseEntity<List<Sec103_1093910_HelpOffer>> getAll(
            @Parameter(description = "Filter by family ID") @RequestParam(required = false) Long familyId,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by status (OPEN, ACTIVE, COMPLETED, CANCELLED)")
            @RequestParam(required = false) String status) {

        if (familyId != null && status != null) {
            return ResponseEntity.ok(helpOfferService.findByFamilyIdAndStatus(familyId, status));
        }
        if (familyId != null) {
            return ResponseEntity.ok(helpOfferService.findByFamilyId(familyId));
        }
        if (categoryId != null) {
            return ResponseEntity.ok(helpOfferService.findByCategoryId(categoryId));
        }
        if (status != null) {
            return ResponseEntity.ok(helpOfferService.findByStatus(status));
        }
        return ResponseEntity.ok(helpOfferService.findAll());
    }

    // GET /api/offers/{id}

    @GetMapping("/{id}")
    @Operation(summary = "Get a help offer by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offer found"),
        @ApiResponse(responseCode = "404", description = "Offer not found")
    })
    public ResponseEntity<Sec103_1093910_HelpOffer> getById(
            @Parameter(description = "Offer ID") @PathVariable Long id) {
        return helpOfferService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/offers/{id}

    @PutMapping("/{id}")
    @Operation(summary = "Update a help offer",
               description = "Only OPEN offers can be edited. Attempting to update an ACTIVE or COMPLETED offer returns 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Offer updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Offer not found"),
        @ApiResponse(responseCode = "409", description = "Offer is not in OPEN state")
    })
    public ResponseEntity<Sec103_1093910_HelpOffer> update(
            @Parameter(description = "Offer ID") @PathVariable Long id,
            @RequestBody Sec103_1093910_HelpOffer updated) {
        return ResponseEntity.ok(helpOfferService.update(id, updated));
    }

    // DELETE /api/offers/{id}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a help offer",
               description = "Deletes an offer. ACTIVE offers cannot be deleted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Offer deleted"),
        @ApiResponse(responseCode = "404", description = "Offer not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete an ACTIVE offer")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Offer ID") @PathVariable Long id) {
        helpOfferService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
