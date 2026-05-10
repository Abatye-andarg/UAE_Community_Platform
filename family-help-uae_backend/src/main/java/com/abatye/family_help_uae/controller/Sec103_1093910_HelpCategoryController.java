package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpCategory;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpCategoryService;
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
 * REST controller for help category management.
 *
 * <p>Categories classify offers and requests (e.g., Tutoring, Transport, Childcare).
 * They are platform-level reference data.</p>
 *
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Help Category", description = "Manage help service categories (e.g., Tutoring, Childcare, Transport)")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_HelpCategoryController {

    private final Sec103_1093910_HelpCategoryService helpCategoryService;

    // POST /api/categories
    @PostMapping
    @Operation(summary = "Create a new help category")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created"),
        @ApiResponse(responseCode = "400", description = "Category name already exists")
    })
    public ResponseEntity<Sec103_1093910_HelpCategory> create(
            @RequestBody Sec103_1093910_HelpCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(helpCategoryService.save(category));
    }

    // GET /api/categories

    @GetMapping
    @Operation(summary = "List all help categories")
    @ApiResponse(responseCode = "200", description = "Categories retrieved")
    public ResponseEntity<List<Sec103_1093910_HelpCategory>> getAll() {
        return ResponseEntity.ok(helpCategoryService.findAll());
    }

    // GET /api/categories/{id}

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Sec103_1093910_HelpCategory> getById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        return helpCategoryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/categories/by-name?name=

    @GetMapping("/by-name")
    @Operation(summary = "Find a category by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Sec103_1093910_HelpCategory> getByName(
            @Parameter(description = "Category name") @RequestParam String name) {
        return helpCategoryService.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/categories/{id}

    @PutMapping("/{id}")
    @Operation(summary = "Update a category name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category updated"),
        @ApiResponse(responseCode = "400", description = "Name already in use"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Sec103_1093910_HelpCategory> update(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @RequestBody Sec103_1093910_HelpCategory updated) {
        return ResponseEntity.ok(helpCategoryService.update(id, updated));
    }

    // DELETE /api/categories/{id}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Category deleted"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        helpCategoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
