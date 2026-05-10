package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_Reputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Sec103_1093910_ReputationRepository extends JpaRepository<Sec103_1093910_Reputation, Long> {

    Optional<Sec103_1093910_Reputation> findByFamilyId(Long familyId);

    void deleteByFamilyId(Long familyId);

    boolean existsByFamilyId(Long familyId);
}
