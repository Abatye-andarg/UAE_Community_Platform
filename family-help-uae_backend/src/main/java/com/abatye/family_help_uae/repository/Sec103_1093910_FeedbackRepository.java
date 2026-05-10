package com.abatye.family_help_uae.repository;

import com.abatye.family_help_uae.model.Sec103_1093910_Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Sec103_1093910_FeedbackRepository extends JpaRepository<Sec103_1093910_Feedback, Long> {

    List<Sec103_1093910_Feedback> findByHelpTaskId(Long helpTaskId);

    List<Sec103_1093910_Feedback> findByTargetFamilyId(Long targetFamilyId);

    List<Sec103_1093910_Feedback> findByReviewerFamilyId(Long reviewerFamilyId);

    void deleteByReviewerFamilyIdOrTargetFamilyId(Long reviewerId, Long targetId);

    // Reflects the UQ constraint: one review per reviewer per task
    Optional<Sec103_1093910_Feedback> findByHelpTaskIdAndReviewerFamilyId(Long helpTaskId, Long reviewerFamilyId);

    boolean existsByHelpTaskIdAndReviewerFamilyId(Long helpTaskId, Long reviewerFamilyId);

    /**
     * Returns the arithmetic mean of ALL ratings across every family on the platform.
     *
     * <p>This is the global mean ({@code m}) required by the Bayesian Average formula.
     * {@code COALESCE} ensures a non-null fallback is returned during the cold-start
     * period when no feedback has been submitted yet (returns 3.0 in that case,
     * matching {@link com.abatye.family_help_uae.reputation.BayesianReputationAlgorithm#DEFAULT_GLOBAL_MEAN}).</p>
     *
     * @return platform-wide mean rating in [1.0, 5.0], or 3.0 if the table is empty
     */
    @Query("SELECT COALESCE(AVG(f.rating), 3.0) FROM Sec103_1093910_Feedback f")
    double getGlobalMeanRating();
}
