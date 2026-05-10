package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_HelpTask;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_HelpTaskService {

    Sec103_1093910_HelpTask save(Sec103_1093910_HelpTask helpTask);

    Optional<Sec103_1093910_HelpTask> findById(Long id);

    List<Sec103_1093910_HelpTask> findAll();

    List<Sec103_1093910_HelpTask> findByRequesterFamilyId(Long requesterFamilyId);

    List<Sec103_1093910_HelpTask> findByHelperFamilyId(Long helperFamilyId);

    List<Sec103_1093910_HelpTask> findByStatus(String status);

    List<Sec103_1093910_HelpTask> findByHelpRequestId(Long helpRequestId);

    List<Sec103_1093910_HelpTask> findByHelpOfferId(Long helpOfferId);

    Sec103_1093910_HelpTask update(Long id, Sec103_1093910_HelpTask helpTask);

    void deleteById(Long id);
}
