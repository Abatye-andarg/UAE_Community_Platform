package com.abatye.family_help_uae.service;

import com.abatye.family_help_uae.model.Sec103_1093910_Feedback;

import java.util.List;
import java.util.Optional;

public interface Sec103_1093910_FeedbackService {

    Sec103_1093910_Feedback save(Sec103_1093910_Feedback feedback);

    Optional<Sec103_1093910_Feedback> findById(Long id);

    List<Sec103_1093910_Feedback> findAll();

    List<Sec103_1093910_Feedback> findByHelpTaskId(Long helpTaskId);

    List<Sec103_1093910_Feedback> findByTargetFamilyId(Long targetFamilyId);

    List<Sec103_1093910_Feedback> findByReviewerFamilyId(Long reviewerFamilyId);

    Optional<Sec103_1093910_Feedback> findByHelpTaskIdAndReviewerFamilyId(Long helpTaskId, Long reviewerFamilyId);

    boolean existsByHelpTaskIdAndReviewerFamilyId(Long helpTaskId, Long reviewerFamilyId);

    Sec103_1093910_Feedback update(Long id, Sec103_1093910_Feedback feedback);

    void deleteById(Long id);
}
