package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpTask;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpTaskService;
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
 * REST controller for help task lifecycle management.
 *
 * <p>Tasks represent an agreed support interaction between two families.
 * Status lifecycle: {@code ACTIVE → COMPLETED / CANCELLED}.
 * Marking a task as COMPLETED automatically recalculates the helper's reputation score.</p>
 *
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Help Task", description = "Manage active and completed support interactions between families")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_HelpTaskController {

    private final Sec103_1093910_HelpTaskService helpTaskService;

    // POST /api/tasks

    @PostMapping
    @Operation(summary = "Create a new help task",
               description = "Initiates a support interaction. Must link to exactly one help request OR offer (XOR).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created"),
        @ApiResponse(responseCode = "400", description = "Validation error or XOR constraint violation")
    })
    public ResponseEntity<Sec103_1093910_HelpTask> create(
            @RequestBody Sec103_1093910_HelpTask helpTask) {
        return ResponseEntity.status(HttpStatus.CREATED).body(helpTaskService.save(helpTask));
    }

    // GET /api/tasks

    @GetMapping
    @Operation(summary = "List help tasks",
               description = "Filter tasks by requester, helper, status, or linked request/offer ID.")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved")
    public ResponseEntity<List<Sec103_1093910_HelpTask>> getAll(
            @Parameter(description = "Filter by requester family ID") @RequestParam(required = false) Long requesterFamilyId,
            @Parameter(description = "Filter by helper family ID") @RequestParam(required = false) Long helperFamilyId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by linked help request ID") @RequestParam(required = false) Long helpRequestId,
            @Parameter(description = "Filter by linked help offer ID") @RequestParam(required = false) Long helpOfferId) {

        if (requesterFamilyId != null) {
            return ResponseEntity.ok(helpTaskService.findByRequesterFamilyId(requesterFamilyId));
        }
        if (helperFamilyId != null) {
            return ResponseEntity.ok(helpTaskService.findByHelperFamilyId(helperFamilyId));
        }
        if (status != null) {
            return ResponseEntity.ok(helpTaskService.findByStatus(status));
        }
        if (helpRequestId != null) {
            return ResponseEntity.ok(helpTaskService.findByHelpRequestId(helpRequestId));
        }
        if (helpOfferId != null) {
            return ResponseEntity.ok(helpTaskService.findByHelpOfferId(helpOfferId));
        }
        return ResponseEntity.ok(helpTaskService.findAll());
    }

    // GET /api/tasks/{id}

    @GetMapping("/{id}")
    @Operation(summary = "Get a help task by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Sec103_1093910_HelpTask> getById(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        return helpTaskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/tasks/{id}

    @PutMapping("/{id}")
    @Operation(summary = "Update task status",
               description = "Updating a task to COMPLETED automatically sets the completed_at timestamp "
                           + "and triggers a reputation recalculation for the helper family. Terminal states "
                           + "cannot be changed.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task updated"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Task is in a terminal state")
    })
    public ResponseEntity<Sec103_1093910_HelpTask> update(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestBody Sec103_1093910_HelpTask updated) {
        return ResponseEntity.ok(helpTaskService.update(id, updated));
    }

    // DELETE /api/tasks/{id}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task",
               description = "Only non-ACTIVE tasks can be deleted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "409", description = "Cannot delete an ACTIVE task")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Task ID") @PathVariable Long id) {
        helpTaskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
