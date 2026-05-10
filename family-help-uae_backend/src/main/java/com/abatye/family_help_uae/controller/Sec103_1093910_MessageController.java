package com.abatye.family_help_uae.controller;

import com.abatye.family_help_uae.model.Sec103_1093910_Message;
import com.abatye.family_help_uae.service.Sec103_1093910_MessageService;
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
 * REST controller for inter-family messaging.
 *
 * <p>Messages are immutable once sent and must be scoped to a specific HelpTask.
 * The endpoints support retrieving conversation threads for tasks.</p>
 *
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Message", description = "Inter-family messaging within the context of a support task")
@SecurityRequirement(name = "bearerAuth")
public class Sec103_1093910_MessageController {

    private final Sec103_1093910_MessageService messageService;

    // POST /api/messages

    @PostMapping
    @Operation(summary = "Send a message",
               description = "Sends a message between families linked to a specific HelpTask. "
                           + "Messages are immutable and self-messaging is prohibited.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Message sent successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error or self-messaging attempt")
    })
    public ResponseEntity<Sec103_1093910_Message> create(
            @RequestBody Sec103_1093910_Message message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.save(message));
    }

    // GET /api/messages

    @GetMapping
    @Operation(summary = "List messages",
               description = "Retrieve messages, optionally filtering by task ID, sender, or receiver.")
    @ApiResponse(responseCode = "200", description = "Messages retrieved")
    public ResponseEntity<List<Sec103_1093910_Message>> getAll(
            @Parameter(description = "Filter by task ID") @RequestParam(required = false) Long helpTaskId,
            @Parameter(description = "Filter by sender family ID") @RequestParam(required = false) Long senderFamilyId,
            @Parameter(description = "Filter by receiver family ID") @RequestParam(required = false) Long receiverFamilyId) {

        if (helpTaskId != null) {
            return ResponseEntity.ok(messageService.findByHelpTaskIdOrderByCreatedAtAsc(helpTaskId));
        }
        if (senderFamilyId != null && receiverFamilyId != null) {
            return ResponseEntity.ok(messageService.findByConversation(senderFamilyId, receiverFamilyId));
        }
        if (senderFamilyId != null) {
            return ResponseEntity.ok(messageService.findBySenderFamilyId(senderFamilyId));
        }
        if (receiverFamilyId != null) {
            return ResponseEntity.ok(messageService.findByReceiverFamilyId(receiverFamilyId));
        }
        return ResponseEntity.ok(messageService.findAll());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread message count")
    public ResponseEntity<Long> getUnreadCount(
            @Parameter(description = "Family ID") @RequestParam Long familyId) {
        return ResponseEntity.ok(messageService.getUnreadCount(familyId));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get list of recent conversations")
    public ResponseEntity<List<Sec103_1093910_Message>> getConversations(
            @Parameter(description = "Family ID") @RequestParam Long familyId) {
        return ResponseEntity.ok(messageService.getRecentConversations(familyId));
    }

    @PostMapping("/mark-as-read")
    @Operation(summary = "Mark messages as read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam Long receiverId,
            @RequestParam Long senderId) {
        messageService.markAsRead(receiverId, senderId);
        return ResponseEntity.ok().build();
    }

    // GET /api/messages/{id}

    @GetMapping("/msg/{id}")
    @Operation(summary = "Get a message by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message found"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<Sec103_1093910_Message> getById(
            @Parameter(description = "Message ID") @PathVariable Long id) {
        return messageService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/messages/{id}

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a message",
               description = "Delete a message by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Message deleted"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Message ID") @PathVariable Long id) {
        messageService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
