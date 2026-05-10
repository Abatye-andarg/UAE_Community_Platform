package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.exception.Sec103_1093910_InvalidOperationException;
import com.abatye.family_help_uae.exception.Sec103_1093910_ResourceNotFoundException;
import com.abatye.family_help_uae.model.Sec103_1093910_HelpOffer;
import com.abatye.family_help_uae.repository.Sec103_1093910_HelpOfferRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_HelpOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for help offer management.
 *
 * <p>Families use this to post what support they can provide to the community
 * (e.g., tutoring, transport, childcare). Offers transition through states:
 * {@code OPEN → ACTIVE → COMPLETED / CANCELLED}.</p>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_HelpOfferServiceImpl implements Sec103_1093910_HelpOfferService {

    private final Sec103_1093910_HelpOfferRepository helpOfferRepository;

    @Override
    public Sec103_1093910_HelpOffer save(Sec103_1093910_HelpOffer helpOffer) {
        validateOffer(helpOffer);
        // Ensure default status
        if (helpOffer.getStatus() == null || helpOffer.getStatus().isBlank()) {
            helpOffer.setStatus("OPEN");
        }
        return helpOfferRepository.save(helpOffer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_HelpOffer> findById(Long id) {
        return helpOfferRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpOffer> findAll() {
        return helpOfferRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpOffer> findByFamilyId(Long familyId) {
        return helpOfferRepository.findByFamilyId(familyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpOffer> findByCategoryId(Long categoryId) {
        return helpOfferRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpOffer> findByStatus(String status) {
        return helpOfferRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_HelpOffer> findByFamilyIdAndStatus(Long familyId, String status) {
        return helpOfferRepository.findByFamilyIdAndStatus(familyId, status);
    }

    @Override
    public Sec103_1093910_HelpOffer update(Long id, Sec103_1093910_HelpOffer updated) {
        Sec103_1093910_HelpOffer existing = helpOfferRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Help offer not found with id: " + id));

        // Only OPEN offers can be edited
        if (!"OPEN".equalsIgnoreCase(existing.getStatus())) {
            throw new Sec103_1093910_InvalidOperationException(
                    "Only OPEN offers can be updated. Current status: " + existing.getStatus());
        }

        validateOffer(updated);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setStatus(updated.getStatus());

        return helpOfferRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Sec103_1093910_HelpOffer offer = helpOfferRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Help offer not found with id: " + id));

        if ("ACTIVE".equalsIgnoreCase(offer.getStatus())) {
            throw new Sec103_1093910_InvalidOperationException("Cannot delete an ACTIVE help offer.");
        }
        helpOfferRepository.delete(offer);
    }

    // Private helpers

    private void validateOffer(Sec103_1093910_HelpOffer offer) {
        if (offer.getTitle() == null || offer.getTitle().isBlank()) {
            throw new IllegalArgumentException("Help offer title must not be blank.");
        }
        if (offer.getFamily() == null || offer.getFamily().getId() == null) {
            throw new IllegalArgumentException("Help offer must be associated with a family.");
        }
        if (offer.getCategory() == null || offer.getCategory().getId() == null) {
            throw new IllegalArgumentException("Help offer must have a category.");
        }
    }
}
