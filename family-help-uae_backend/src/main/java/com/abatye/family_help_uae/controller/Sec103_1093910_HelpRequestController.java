package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpRequest;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpRequestService;
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
 * REST controller for help request management.
 *
 * <p>Families use this to request assistance from the community (e.g., elder care, household help).
 * Requests follow a status lifecycle: {@code OPEN → ACTIVE → COMPLETED / CANCELLED}.</p>
 *
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Help Request", description = "Submit and manage support requests to the community")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_HelpRequestController {

    private final Sec103_1093910_HelpRequestService helpRequestService;

    // POST /api/requests

    @PostMapping
    @Operation(summary = "Submit a new help request",
               description = "A family requests assistance from the community.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Request created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Sec103_1093910_HelpRequest> create(
            @RequestBody Sec103_1093910_HelpRequest helpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(helpRequestService.save(helpRequest));
    }

    // GET /api/requests

    @GetMapping
    @Operation(summary = "List all help requests",
               description = "Optionally filter by familyId, categoryId, urgency, or status via query parameters.")
    @ApiResponse(responseCode = "200", description = "Requests retrieved")
    public ResponseEntity<List<Sec103_1093910_HelpRequest>> getAll(
            @Parameter(description = "Filter by family ID") @RequestParam(required = false) Long familyId,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by status (OPEN, ACTIVE, COMPLETED, CANCELLED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by urgency (LOW, NORMAL, HIGH, URGENT)")
            @RequestParam(required = false) String urgency) {

        if (familyId != null && status != null) {
            return ResponseEntity.ok(helpRequestService.findByFamilyIdAndStatus(familyId, status));
        }
        if (familyId != null) {
            return ResponseEntity.ok(helpRequestService.findByFamilyId(familyId));
        }
        if (categoryId != null) {
            return ResponseEntity.ok(helpRequestService.findByCategoryId(categoryId));
        }
        if (status != null) {
            return ResponseEntity.ok(helpRequestService.findByStatus(status));
        }
        if (urgency != null) {
            return ResponseEntity.ok(helpRequestService.findByUrgency(urgency));
        }
        return ResponseEntity.ok(helpRequestService.findAll());
    }

    // GET /api/requests/{id}

    @GetMapping("/{id}")
    @Operation(summary = "Get a help request by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request found"),
        @ApiResponse(responseCode = "404", description = "Request not found")
    })
    public ResponseEntity<Sec103_1093910_HelpRequest> getById(
            @Parameter(description = "Request ID") @PathVariable Long id) {
        return helpRequestService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/requests/{id}

    @PutMapping("/{id}")
    @Operation(summary = "Update a help request",
               description = "Only OPEN requests can be edited. Attempting to update an ACTIVE or COMPLETED request returns 409.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request updated"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Request not found"),
        @ApiResponse(responseCode = "409", description = "Request is not in OPEN state")
    })
    public ResponseEntity<Sec103_1093910_HelpRequest> update(
            @Parameter(description = "Request ID") @PathVariable Long id,
            @RequestBody Sec103_1093910_HelpRequest updated) {
        return ResponseEntity.ok(helpRequestService.update(id, updated));
    }

    // DELETE /api/requests/{id}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a help request",
               description = "Deletes a request. ACTIVE requests cannot be deleted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Request deleted"),
        @ApiResponse(responseCode = "404", description = "Request not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete an ACTIVE request")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Request ID") @PathVariable Long id) {
        helpRequestService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
