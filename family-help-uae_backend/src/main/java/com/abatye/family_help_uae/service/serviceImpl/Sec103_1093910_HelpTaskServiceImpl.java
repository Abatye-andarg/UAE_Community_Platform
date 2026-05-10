package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.exception.Sec103_1093910_InvalidOperationException;
import com.abatye.family_help_uae.exception.Sec103_1093910_ResourceNotFoundException;
import com.abatye.family_help_uae.model.Sec103_1093910_HelpTask;
import com.abatye.family_help_uae.repository.Sec103_1093910_HelpTaskRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for help task lifecycle management.
 *
 * <h3>Task Lifecycle:</h3>
 * <pre>
 *   [created] → ACTIVE → COMPLETED
 *                       ↘ CANCELLED
 * </pre>
 *
 * <h3>XOR constraint (Assignment ERD Note 1):</h3>
 * <p>A task must be linked to exactly one of {@code helpRequest} OR {@code helpOffer}, never both,
 * never neither. This is enforced at the service layer before persistence.</p>
 *
 * <h3>Reputation trigger:</h3>
 * <p>When a task transitions to {@code COMPLETED}, the helper family's reputation is automatically
 * recalculated so trust scores remain up-to-date.</p>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_HelpTaskServiceImpl implements Sec103_1093910_HelpTaskService {

    private final Sec103_1093910_HelpTaskRepository helpTaskRepository;
    private final Sec103_1093910_ReputationServiceImpl reputationService;

    @Override
    public Sec103_1093910_HelpTask save(Sec103_1093910_HelpTask helpTask) {
        validateTask(helpTask);
        if (helpTask.getStatus() == null || helpTask.getStatus().isBlank()) {
            helpTask.setStatus("ACTIVE");
        }
        return helpTaskRepository.save(helpTask);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_HelpTask> findById(Long id) {
        return helpTaskRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpTask> findAll() {
        return helpTaskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpTask> findByRequesterFamilyId(Long requesterFamilyId) {
        return helpTaskRepository.findByRequesterFamilyId(requesterFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpTask> findByHelperFamilyId(Long helperFamilyId) {
        return helpTaskRepository.findByHelperFamilyId(helperFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpTask> findByStatus(String status) {
        return helpTaskRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpTask> findByHelpRequestId(Long helpRequestId) {
        return helpTaskRepository.findByHelpRequestId(helpRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpTask> findByHelpOfferId(Long helpOfferId) {
        return helpTaskRepository.findByHelpOfferId(helpOfferId);
    }

    /**
     * Updates task status. When marking as {@code COMPLETED}:
     * <ul>
     *   <li>Sets {@code completedAt} timestamp.</li>
     *   <li>Triggers an automatic reputation recalculation for the helper family.</li>
     * </ul>
     */
    @Override
    public Sec103_1093910_HelpTask update(Long id, Sec103_1093910_HelpTask updated) {
        Sec103_1093910_HelpTask existing = helpTaskRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Help task not found with id: " + id));

        // Terminal states cannot be re-opened
        if ("COMPLETED".equalsIgnoreCase(existing.getStatus())
                || "CANCELLED".equalsIgnoreCase(existing.getStatus())) {
            throw new Sec103_1093910_InvalidOperationException(
                    "Cannot update a task in terminal state: " + existing.getStatus());
        }

        String newStatus = updated.getStatus();
        existing.setStatus(newStatus);

        if ("COMPLETED".equalsIgnoreCase(newStatus)) {
            existing.setCompletedAt(LocalDateTime.now());
            helpTaskRepository.save(existing);
            // Trigger reputation recalculation for the helper
            reputationService.recalculate(existing.getHelperFamily().getId());
            return existing;
        }

        return helpTaskRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Sec103_1093910_HelpTask task = helpTaskRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Help task not found with id: " + id));

        if ("ACTIVE".equalsIgnoreCase(task.getStatus())) {
            throw new Sec103_1093910_InvalidOperationException("Cannot delete an ACTIVE help task.");
        }
        helpTaskRepository.delete(task);
    }

    // Private helpers

    /**
     * Enforces the XOR constraint from the ERD: exactly one of helpRequest or helpOffer must be set.
     */
    private void validateTask(Sec103_1093910_HelpTask task) {
        boolean hasRequest = task.getHelpRequest() != null && task.getHelpRequest().getId() != null;
        boolean hasOffer = task.getHelpOffer() != null && task.getHelpOffer().getId() != null;

        if (hasRequest == hasOffer) {
            // both true OR both false → violation
            throw new IllegalArgumentException(
                    "A help task must be linked to exactly one of helpRequest OR helpOffer (XOR), not both or neither.");
        }
        if (task.getRequesterFamily() == null || task.getRequesterFamily().getId() == null) {
            throw new IllegalArgumentException("Help task must have a requester family.");
        }
        if (task.getHelperFamily() == null || task.getHelperFamily().getId() == null) {
            throw new IllegalArgumentException("Help task must have a helper family.");
        }
        if (task.getCategory() == null || task.getCategory().getId() == null) {
            throw new IllegalArgumentException("Help task must have a category.");
        }
    }
}
