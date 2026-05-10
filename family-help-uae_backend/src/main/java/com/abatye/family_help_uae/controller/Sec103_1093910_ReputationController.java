package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_Reputation;
import com.abatye.family_help_uae.service.Sec103_1093910_ReputationService;
import com.abatye.family_help_uae.service.serviceImpl.Sec103_1093910_ReputationServiceImpl;
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

/**
 * REST controller for family trust and reputation scores.
 *
 * <h3>Reputation is scoped to a family (one-to-one).</h3>
 * <p>The primary read path for trust scores is {@code GET /api/reputations/{familyId}}.
 * Recalculation is triggered automatically by the FeedbackService and HelpTaskService,
 * but can also be triggered manually via the recalculate endpoint (useful for admin/testing).</p>
 *
 * <h3>Trust Score Algorithm (transparent, abuse-resistant):</h3>
 * <pre>
 *   reliabilityScore = ((avgRating / 5.0) * 0.6 + min(completedTasks / 10.0, 1.0) * 0.4) * 5.0
 * </pre>
 *
 */
@RestController
@RequestMapping("/api/reputations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Reputation", description = "View and manage family trust and reputation scores")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_ReputationController {

    private final Sec103_1093910_ReputationService reputationService;
    private final Sec103_1093910_ReputationServiceImpl reputationServiceImpl;

    // GET /api/reputations/{familyId}  — View a family's trust score

    @GetMapping("/{familyId}")
    @Operation(summary = "Get reputation for a family",
               description = "Returns the trust score, average rating, completed tasks, and reliability score for the specified family.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reputation record found"),
        @ApiResponse(responseCode = "404", description = "No reputation record for this family")
    })
    public ResponseEntity<Sec103_1093910_Reputation> getByFamilyId(
            @Parameter(description = "Family ID") @PathVariable Long familyId) {
        return reputationService.findByFamilyId(familyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/reputations/{familyId}/recalculate  — Trigger recalculation

    @PostMapping("/{familyId}/recalculate")
    @Operation(summary = "Recalculate reputation for a family",
               description = "Recomputes avg_rating, total_reviews, completed_tasks, and reliability_score "
                           + "based on current feedback and task data. Called automatically on feedback/task events "
                           + "but can be triggered manually.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reputation recalculated"),
        @ApiResponse(responseCode = "404", description = "Family not found")
    })
    public ResponseEntity<Sec103_1093910_Reputation> recalculate(
            @Parameter(description = "Family ID") @PathVariable Long familyId) {
        return ResponseEntity.ok(reputationServiceImpl.recalculate(familyId));
    }

    // PUT /api/reputations/{familyId}  — Manual update (admin use)

    @PutMapping("/{familyId}")
    @Operation(summary = "Manually update a reputation record (admin)",
               description = "Allows direct override of reputation fields. Prefer the recalculate endpoint for automatic computation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reputation updated"),
        @ApiResponse(responseCode = "404", description = "Reputation record not found")
    })
    public ResponseEntity<Sec103_1093910_Reputation> update(
            @Parameter(description = "Family ID") @PathVariable Long familyId,
            @RequestBody Sec103_1093910_Reputation updated) {
        return ResponseEntity.ok(reputationService.update(familyId, updated));
    }

    // DELETE /api/reputations/{familyId}

    @DeleteMapping("/{familyId}")
    @Operation(summary = "Delete a reputation record",
               description = "Removes the reputation record for a family. Typically used when a family account is deleted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Reputation deleted"),
        @ApiResponse(responseCode = "404", description = "Reputation record not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Family ID") @PathVariable Long familyId) {
        reputationService.deleteByFamilyId(familyId);
        return ResponseEntity.noContent().build();
    }
}
