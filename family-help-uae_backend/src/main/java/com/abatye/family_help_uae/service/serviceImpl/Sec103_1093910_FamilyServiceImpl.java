package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.exception.Sec103_1093910_InvalidOperationException;
import com.abatye.family_help_uae.exception.Sec103_1093910_ResourceNotFoundException;
import com.abatye.family_help_uae.model.Sec103_1093910_Family;
import com.abatye.family_help_uae.repository.Sec103_1093910_FamilyRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for family profile management.
 *
 * <p>Handles family registration, profile updates, and lookup operations.
 * Passwords are always stored as BCrypt hashes — never in plain text.</p>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_FamilyServiceImpl implements Sec103_1093910_FamilyService {

    private final Sec103_1093910_FamilyRepository familyRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.abatye.family_help_uae.repository.Sec103_1093910_HelpOfferRepository offerRepository;
    private final com.abatye.family_help_uae.repository.Sec103_1093910_HelpRequestRepository requestRepository;
    private final com.abatye.family_help_uae.repository.Sec103_1093910_HelpTaskRepository taskRepository;
    private final com.abatye.family_help_uae.repository.Sec103_1093910_MessageRepository messageRepository;
    private final com.abatye.family_help_uae.repository.Sec103_1093910_FeedbackRepository feedbackRepository;
    private final com.abatye.family_help_uae.repository.Sec103_1093910_ReputationRepository reputationRepository;

    /**
     * Registers a new family. The raw password in {@code family.passwordHash}
     * is encoded before persistence.
     *
     * @throws IllegalArgumentException if the email is already registered
     */
    @Override
    public Sec103_1093910_Family save(Sec103_1093910_Family family) {
        if (familyRepository.existsByEmail(family.getEmail())) {
            throw new Sec103_1093910_InvalidOperationException(
                    "A family with email '" + family.getEmail() + "' is already registered.");
        }
        // Securely hash the password before storing
        family.setPasswordHash(passwordEncoder.encode(family.getPasswordHash()));
        return familyRepository.save(family);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_Family> findById(Long id) {
        return familyRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_Family> findByEmail(String email) {
        return familyRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Family> findAll() {
        return familyRepository.findAll();
    }

    /**
     * Updates an existing family profile. Only non-sensitive fields are updated;
     * password changes must go through a dedicated password-change flow (not here).
     *
     * @throws IllegalArgumentException if the family does not exist
     */
    @Override
    public Sec103_1093910_Family update(Long id, Sec103_1093910_Family updated) {
        Sec103_1093910_Family existing = familyRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Family not found with id: " + id));

        existing.setFamilyName(updated.getFamilyName());
        existing.setAddress(updated.getAddress());
        existing.setPhone(updated.getPhone());

        // Email uniqueness guard when changing email
        if (!existing.getEmail().equalsIgnoreCase(updated.getEmail())) {
            if (familyRepository.existsByEmail(updated.getEmail())) {
                throw new Sec103_1093910_InvalidOperationException(
                        "Email '" + updated.getEmail() + "' is already in use.");
            }
            existing.setEmail(updated.getEmail());
        }

        return familyRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!familyRepository.existsById(id)) {
            throw new Sec103_1093910_ResourceNotFoundException("Family not found with id: " + id);
        }
        
        // Manual Cascade Cleanup
        offerRepository.deleteByFamilyId(id);
        requestRepository.deleteByFamilyId(id);
        taskRepository.deleteByRequesterFamilyIdOrHelperFamilyId(id, id);
        messageRepository.deleteBySenderFamilyIdOrReceiverFamilyId(id, id);
        feedbackRepository.deleteByReviewerFamilyIdOrTargetFamilyId(id, id);
        reputationRepository.deleteByFamilyId(id);
        
        familyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return familyRepository.existsByEmail(email);
    }
}
