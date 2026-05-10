package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpOffer;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_HelpOfferService {

    Sec103_1093910_HelpOffer save(Sec103_1093910_HelpOffer helpOffer);

    Optional<Sec103_1093910_HelpOffer> findById(Long id);

    List<Sec103_1093910_HelpOffer> findAll();

    List<Sec103_1093910_HelpOffer> findByFamilyId(Long familyId);

    List<Sec103_1093910_HelpOffer> findByCategoryId(Long categoryId);

    List<Sec103_1093910_HelpOffer> findByStatus(String status);

    List<Sec103_1093910_HelpOffer> findByFamilyIdAndStatus(Long familyId, String status);

    Sec103_1093910_HelpOffer update(Long id, Sec103_1093910_HelpOffer helpOffer);

    void deleteById(Long id);
}
