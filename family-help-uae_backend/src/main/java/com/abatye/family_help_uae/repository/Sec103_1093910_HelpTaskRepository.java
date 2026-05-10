package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Sec103_1093910_HelpTaskRepository extends JpaRepository<Sec103_1093910_HelpTask, Long> {

    List<Sec103_1093910_HelpTask> findByRequesterFamilyId(Long requesterFamilyId);

    List<Sec103_1093910_HelpTask> findByHelperFamilyId(Long helperFamilyId);

    void deleteByRequesterFamilyIdOrHelperFamilyId(Long requesterId, Long helperId);

    List<Sec103_1093910_HelpTask> findByStatus(String status);

    List<Sec103_1093910_HelpTask> findByHelpRequestId(Long helpRequestId);

    List<Sec103_1093910_HelpTask> findByHelpOfferId(Long helpOfferId);
}
