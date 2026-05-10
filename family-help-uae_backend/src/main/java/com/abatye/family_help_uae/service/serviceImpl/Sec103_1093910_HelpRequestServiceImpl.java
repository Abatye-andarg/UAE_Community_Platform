package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.exception.Sec103_1093910_InvalidOperationException;
import com.abatye.family_help_uae.exception.Sec103_1093910_ResourceNotFoundException;
import com.abatye.family_help_uae.model.Sec103_1093910_HelpRequest;
import com.abatye.family_help_uae.repository.Sec103_1093910_HelpRequestRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for help request management.
 *
 * <p>Families use this to post what assistance they need from the community
 * (e.g., elder care, household help). Requests progress through states:
 * {@code OPEN → ACTIVE → COMPLETED / CANCELLED}.</p>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_HelpRequestServiceImpl implements Sec103_1093910_HelpRequestService {

    private final Sec103_1093910_HelpRequestRepository helpRequestRepository;

    @Override
    public Sec103_1093910_HelpRequest save(Sec103_1093910_HelpRequest helpRequest) {
        validateRequest(helpRequest);
        if (helpRequest.getStatus() == null || helpRequest.getStatus().isBlank()) {
            helpRequest.setStatus("OPEN");
        }
        if (helpRequest.getUrgency() == null || helpRequest.getUrgency().isBlank()) {
            helpRequest.setUrgency("NORMAL");
        }
        return helpRequestRepository.save(helpRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_HelpRequest> findById(Long id) {
        return helpRequestRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpRequest> findAll() {
        return helpRequestRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpRequest> findByFamilyId(Long familyId) {
        return helpRequestRepository.findByFamilyId(familyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpRequest> findByCategoryId(Long categoryId) {
        return helpRequestRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpRequest> findByStatus(String status) {
        return helpRequestRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpRequest> findByUrgency(String urgency) {
        return helpRequestRepository.findByUrgency(urgency);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpRequest> findByFamilyIdAndStatus(Long familyId, String status) {
        return helpRequestRepository.findByFamilyIdAndStatus(familyId, status);
    }

    @Override
    public Sec103_1093910_HelpRequest update(Long id, Sec103_1093910_HelpRequest updated) {
        Sec103_1093910_HelpRequest existing = helpRequestRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Help request not found with id: " + id));

        // Only OPEN requests can be edited
        if (!"OPEN".equalsIgnoreCase(existing.getStatus())) {
            throw new Sec103_1093910_InvalidOperationException(
                    "Only OPEN requests can be updated. Current status: " + existing.getStatus());
        }

        validateRequest(updated);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setUrgency(updated.getUrgency());
        existing.setCategory(updated.getCategory());
        existing.setStatus(updated.getStatus());

        return helpRequestRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Sec103_1093910_HelpRequest request = helpRequestRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Help request not found with id: " + id));

        if ("ACTIVE".equalsIgnoreCase(request.getStatus())) {
            throw new Sec103_1093910_InvalidOperationException("Cannot delete an ACTIVE help request.");
        }
        helpRequestRepository.delete(request);
    }

    // Private helpers

    private void validateRequest(Sec103_1093910_HelpRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Help request title must not be blank.");
        }
        if (request.getFamily() == null || request.getFamily().getId() == null) {
            throw new IllegalArgumentException("Help request must be associated with a family.");
        }
        if (request.getCategory() == null || request.getCategory().getId() == null) {
            throw new IllegalArgumentException("Help request must have a category.");
        }
        String urgency = request.getUrgency();
        if (urgency != null && !urgency.isBlank()
                && !List.of("LOW", "NORMAL", "HIGH", "URGENT").contains(urgency.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid urgency level: " + urgency + ". Allowed: LOW, NORMAL, HIGH, URGENT.");
        }
    }
}
