package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_Feedback;
import com.abatye.family_help_uae.service.Sec103_1093910_FeedbackService;
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
 * REST controller for feedback and trust submission.
 *
 * <p>Feedback directly impacts a family's reputation score. Submitting, updating,
 * or deleting feedback automatically triggers a recalculation of the target family's
 * trust metrics.</p>
 *
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Feedback", description = "Submit and manage ratings/reviews between families")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_FeedbackController {

    private final Sec103_1093910_FeedbackService feedbackService;

    // POST /api/feedback

    @PostMapping
    @Operation(summary = "Submit feedback",
               description = "Provide a rating (1-5) and comment for a completed task. "
                           + "Reviewers can only submit one review per task. Self-review is prohibited.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Feedback submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid rating or self-review attempt"),
        @ApiResponse(responseCode = "409", description = "Feedback already submitted for this task by this reviewer")
    })
    public ResponseEntity<Sec103_1093910_Feedback> create(
            @RequestBody Sec103_1093910_Feedback feedback) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.save(feedback));
    }

    // GET /api/feedback

    @GetMapping
    @Operation(summary = "List feedback",
               description = "Optionally filter by helpTaskId, targetFamilyId, or reviewerFamilyId.")
    @ApiResponse(responseCode = "200", description = "Feedback retrieved")
    public ResponseEntity<List<Sec103_1093910_Feedback>> getAll(
            @Parameter(description = "Filter by help task ID") @RequestParam(required = false) Long helpTaskId,
            @Parameter(description = "Filter by target family ID") @RequestParam(required = false) Long targetFamilyId,
            @Parameter(description = "Filter by reviewer family ID") @RequestParam(required = false) Long reviewerFamilyId) {

        if (helpTaskId != null) {
            return ResponseEntity.ok(feedbackService.findByHelpTaskId(helpTaskId));
        }
        if (targetFamilyId != null) {
            return ResponseEntity.ok(feedbackService.findByTargetFamilyId(targetFamilyId));
        }
        if (reviewerFamilyId != null) {
            return ResponseEntity.ok(feedbackService.findByReviewerFamilyId(reviewerFamilyId));
        }
        return ResponseEntity.ok(feedbackService.findAll());
    }

    // GET /api/feedback/{id}

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Feedback found"),
        @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    public ResponseEntity<Sec103_1093910_Feedback> getById(
            @Parameter(description = "Feedback ID") @PathVariable Long id) {
        return feedbackService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/feedback/{id}

    @PutMapping("/{id}")
    @Operation(summary = "Update feedback",
               description = "Edit the rating and comment. Triggers target family reputation recalculation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Feedback updated"),
        @ApiResponse(responseCode = "400", description = "Invalid rating"),
        @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    public ResponseEntity<Sec103_1093910_Feedback> update(
            @Parameter(description = "Feedback ID") @PathVariable Long id,
            @RequestBody Sec103_1093910_Feedback updated) {
        return ResponseEntity.ok(feedbackService.update(id, updated));
    }

    // DELETE /api/feedback/{id}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete feedback",
               description = "Removes the feedback and recalculates the target family's reputation score.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Feedback deleted"),
        @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Feedback ID") @PathVariable Long id) {
        feedbackService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
