package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.exception.Sec103_1093910_InvalidOperationException;
import com.abatye.family_help_uae.exception.Sec103_1093910_ResourceNotFoundException;
import com.abatye.family_help_uae.model.Sec103_1093910_Feedback;
import com.abatye.family_help_uae.repository.Sec103_1093910_FeedbackRepository;
import com.abatye.family_help_uae.service.Sec103_1093910_FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for feedback and trust submission.
 *
 * <h3>Business rules enforced:</h3>
 * <ul>
 *   <li>Rating must be between 1 and 5 (inclusive).</li>
 *   <li>A reviewer can submit only one feedback entry per task (UQ constraint).</li>
 *   <li>A reviewer cannot review themselves (reviewer ≠ target).</li>
 *   <li>After saving, the target family's reputation is automatically recalculated.</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_FeedbackServiceImpl implements Sec103_1093910_FeedbackService {

    private final Sec103_1093910_FeedbackRepository feedbackRepository;
    private final Sec103_1093910_ReputationServiceImpl reputationService;

    @Override
    public Sec103_1093910_Feedback save(Sec103_1093910_Feedback feedback) {
        validateFeedback(feedback);

        // Enforce unique-per-reviewer-per-task (mirrors DB UQ constraint)
        if (feedbackRepository.existsByHelpTaskIdAndReviewerFamilyId(
                feedback.getHelpTask().getId(),
                feedback.getReviewerFamily().getId())) {
            throw new Sec103_1093910_InvalidOperationException(
                    "Reviewer family " + feedback.getReviewerFamily().getId()
                            + " has already submitted feedback for task " + feedback.getHelpTask().getId());
        }

        Sec103_1093910_Feedback saved = feedbackRepository.save(feedback);

        // Automatically update the target family's trust score
        reputationService.recalculate(feedback.getTargetFamily().getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Feedback> findByHelpTaskId(Long helpTaskId) {
        return feedbackRepository.findByHelpTaskId(helpTaskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Feedback> findByTargetFamilyId(Long targetFamilyId) {
        return feedbackRepository.findByTargetFamilyId(targetFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sec103_1093910_Feedback> findByReviewerFamilyId(Long reviewerFamilyId) {
        return feedbackRepository.findByReviewerFamilyId(reviewerFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sec103_1093910_Feedback> findByHelpTaskIdAndReviewerFamilyId(
            Long helpTaskId, Long reviewerFamilyId) {
        return feedbackRepository.findByHelpTaskIdAndReviewerFamilyId(helpTaskId, reviewerFamilyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByHelpTaskIdAndReviewerFamilyId(Long helpTaskId, Long reviewerFamilyId) {
        return feedbackRepository.existsByHelpTaskIdAndReviewerFamilyId(helpTaskId, reviewerFamilyId);
    }

    @Override
    public Sec103_1093910_Feedback update(Long id, Sec103_1093910_Feedback updated) {
        Sec103_1093910_Feedback existing = feedbackRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Feedback not found with id: " + id));

        // Only the rating and comment are editable after submission
        if (updated.getRating() < 1 || updated.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        existing.setRating(updated.getRating());
        existing.setComment(updated.getComment());

        Sec103_1093910_Feedback saved = feedbackRepository.save(existing);

        // Recalculate reputation after edit
        reputationService.recalculate(existing.getTargetFamily().getId());

        return saved;
    }

    @Override
    public void deleteById(Long id) {
        Sec103_1093910_Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException("Feedback not found with id: " + id));
        Long targetFamilyId = feedback.getTargetFamily().getId();

        feedbackRepository.delete(feedback);

        // Recalculate reputation after removal
        reputationService.recalculate(targetFamilyId);
    }

    // Private helpers

    private void validateFeedback(Sec103_1093910_Feedback feedback) {
        if (feedback.getRating() == null || feedback.getRating() < 1 || feedback.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be an integer between 1 and 5.");
        }
        if (feedback.getHelpTask() == null || feedback.getHelpTask().getId() == null) {
            throw new IllegalArgumentException("Feedback must be associated with a help task.");
        }
        if (feedback.getReviewerFamily() == null || feedback.getReviewerFamily().getId() == null) {
            throw new IllegalArgumentException("Feedback must have a reviewer family.");
        }
        if (feedback.getTargetFamily() == null || feedback.getTargetFamily().getId() == null) {
            throw new IllegalArgumentException("Feedback must have a target family.");
        }
        // A family cannot review itself
        if (feedback.getReviewerFamily().getId().equals(feedback.getTargetFamily().getId())) {
            throw new IllegalArgumentException("A family cannot submit feedback about itself.");
        }
    }
}
