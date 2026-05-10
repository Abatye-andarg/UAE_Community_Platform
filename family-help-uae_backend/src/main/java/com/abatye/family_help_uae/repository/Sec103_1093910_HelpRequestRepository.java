package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Sec103_1093910_HelpRequestRepository extends JpaRepository<Sec103_1093910_HelpRequest, Long> {

    List<Sec103_1093910_HelpRequest> findByFamilyId(Long familyId);

    List<Sec103_1093910_HelpRequest> findByCategoryId(Long categoryId);

    List<Sec103_1093910_HelpRequest> findByStatus(String status);

    List<Sec103_1093910_HelpRequest> findByUrgency(String urgency);

    List<Sec103_1093910_HelpRequest> findByFamilyIdAndStatus(Long familyId, String status);

    void deleteByFamilyId(Long familyId);
}
