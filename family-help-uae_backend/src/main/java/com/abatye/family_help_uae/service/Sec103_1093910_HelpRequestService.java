package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpRequest;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_HelpRequestService {

    Sec103_1093910_HelpRequest save(Sec103_1093910_HelpRequest helpRequest);

    Optional<Sec103_1093910_HelpRequest> findById(Long id);

    List<Sec103_1093910_HelpRequest> findAll();

    List<Sec103_1093910_HelpRequest> findByFamilyId(Long familyId);

    List<Sec103_1093910_HelpRequest> findByCategoryId(Long categoryId);

    List<Sec103_1093910_HelpRequest> findByStatus(String status);

    List<Sec103_1093910_HelpRequest> findByUrgency(String urgency);

    List<Sec103_1093910_HelpRequest> findByFamilyIdAndStatus(Long familyId, String status);

    Sec103_1093910_HelpRequest update(Long id, Sec103_1093910_HelpRequest helpRequest);

    void deleteById(Long id);
}
