package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_Reputation;

import java.util.Optional;

public interface Sec103_1093910_ReputationService {

    Sec103_1093910_Reputation save(Sec103_1093910_Reputation reputation);

    Optional<Sec103_1093910_Reputation> findByFamilyId(Long familyId);

    Sec103_1093910_Reputation update(Long familyId, Sec103_1093910_Reputation reputation);

    void deleteByFamilyId(Long familyId);

    boolean existsByFamilyId(Long familyId);
}
