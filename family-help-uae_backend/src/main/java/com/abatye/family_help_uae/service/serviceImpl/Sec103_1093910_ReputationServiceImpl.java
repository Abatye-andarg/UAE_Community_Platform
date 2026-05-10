package com.abatye.family_help_uae.service.serviceImpl;

import com.abatye.family_help_uae.config.Sec103_1093910_CacheConfig;
import com.abatye.family_help_uae.exception.Sec103_1093910_InvalidOperationException;
import com.abatye.family_help_uae.exception.Sec103_1093910_ResourceNotFoundException;
import com.abatye.family_help_uae.model.Sec103_1093910_Feedback;
import com.abatye.family_help_uae.model.Sec103_1093910_Reputation;
import com.abatye.family_help_uae.repository.Sec103_1093910_FeedbackRepository;
import com.abatye.family_help_uae.repository.Sec103_1093910_HelpTaskRepository;
import com.abatye.family_help_uae.repository.Sec103_1093910_ReputationRepository;
import com.abatye.family_help_uae.repository.Sec103_1093910_FamilyRepository;
import com.abatye.family_help_uae.reputation.BayesianReputationAlgorithm;
import com.abatye.family_help_uae.service.Sec103_1093910_ReputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
/**
 * Service implementation for the family trust and reputation system.
 *
 * <h3>Scoring Algorithm</h3>
 * <p>Scoring is fully delegated to
 * {@link com.abatye.family_help_uae.reputation.BayesianReputationAlgorithm}.
 * See that class for the complete formula, parameter definitions, and design rationale.</p>
 *
 * <p>This service is responsible only for:</p>
 * <ol>
 *   <li>Fetching the raw data needed by the algorithm (feedback list, task list, global mean).</li>
 *   <li>Persisting the result back to the {@code reputation} table.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class Sec103_1093910_ReputationServiceImpl implements Sec103_1093910_ReputationService {

    private final Sec103_1093910_ReputationRepository reputationRepository;
    private final Sec103_1093910_FeedbackRepository feedbackRepository;
    private final Sec103_1093910_HelpTaskRepository helpTaskRepository;
    private final Sec103_1093910_FamilyRepository familyRepository;

    @Override
    public Sec103_1093910_Reputation save(Sec103_1093910_Reputation reputation) {
        if (reputationRepository.existsByFamilyId(reputation.getFamilyId())) {
            throw new Sec103_1093910_InvalidOperationException(
                    "Reputation record already exists for family id: " + reputation.getFamilyId());
        }
        return reputationRepository.save(reputation);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = Sec103_1093910_CacheConfig.CACHE_REPUTATIONS, key = "#familyId")
    public Optional<Sec103_1093910_Reputation> findByFamilyId(Long familyId) {
        return reputationRepository.findByFamilyId(familyId);
    }

    @Override
    public Sec103_1093910_Reputation update(Long familyId, Sec103_1093910_Reputation updated) {
        Sec103_1093910_Reputation existing = reputationRepository.findByFamilyId(familyId)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException(
                        "Reputation record not found for family id: " + familyId));

        existing.setAvgRating(updated.getAvgRating());
        existing.setTotalReviews(updated.getTotalReviews());
        existing.setCompletedTasks(updated.getCompletedTasks());
        existing.setReliabilityScore(updated.getReliabilityScore());
        existing.setLastUpdated(LocalDateTime.now());

        return reputationRepository.save(existing);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_REPUTATIONS,   key = "#familyId"),
        @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_GLOBAL_MEAN,   allEntries = true)
    })
    public void deleteByFamilyId(Long familyId) {
        Sec103_1093910_Reputation reputation = reputationRepository.findByFamilyId(familyId)
                .orElseThrow(() -> new Sec103_1093910_ResourceNotFoundException(
                        "Reputation record not found for family id: " + familyId));
        reputationRepository.delete(reputation);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFamilyId(Long familyId) {
        return reputationRepository.existsByFamilyId(familyId);
    }

    /**
     * Returns the cached platform-wide mean rating used as the Bayesian prior.
     *
     * <p>Wraps {@link Sec103_1093910_FeedbackRepository#getGlobalMeanRating()} with a
     * 5-minute in-memory cache (see {@link Sec103_1093910_CacheConfig#CACHE_GLOBAL_MEAN}).
     * The key {@code "global"} is a fixed string because there is only one global mean
     * for the entire platform.</p>
     *
     * <p>The cache is evicted by {@link #recalculate} and {@link #deleteByFamilyId}
     * so that a fresh aggregate is fetched after any feedback or reputation change.</p>
     */
    @Cacheable(value = Sec103_1093910_CacheConfig.CACHE_GLOBAL_MEAN, key = "'global'")
    public double getCachedGlobalMeanRating() {
        return feedbackRepository.getGlobalMeanRating();
    }

    /**
     * Recalculates and persists the Bayesian reliability score for the given family.
     *
     * <p>Data flow:</p>
     * <ol>
     *   <li>Fetch all feedback where this family is the <em>target</em> → derive {@code n} and {@code avgRating}.</li>
     *   <li>Fetch the platform-wide mean rating ({@code m}) from cache (or DB on miss).</li>
     *   <li>Count COMPLETED tasks where this family is the <em>helper</em>.</li>
     *   <li>Delegate score computation to {@link BayesianReputationAlgorithm#compute}.</li>
     *   <li>Persist the updated {@link Sec103_1093910_Reputation} record.</li>
     *   <li>Evict the now-stale reputation and global mean cache entries.</li>
     * </ol>
     *
     * <p>Called automatically after every feedback submission, feedback update,
     * feedback deletion, and task completion.</p>
     */
    @Caching(evict = {
        @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_REPUTATIONS, key = "#familyId"),
        @CacheEvict(value = Sec103_1093910_CacheConfig.CACHE_GLOBAL_MEAN, allEntries = true)
    })
    public Sec103_1093910_Reputation recalculate(Long familyId) {

        // ── 1. Aggregate this family's received ratings ────────────────────────
        List<Sec103_1093910_Feedback> feedbackList =
                feedbackRepository.findByTargetFamilyId(familyId);

        int    totalReviews = feedbackList.size();
        double avgRatingRaw = feedbackList.stream()
                .mapToInt(Sec103_1093910_Feedback::getRating)
                .average()
                .orElse(0.0);

        // ── 2. Get the platform-wide mean rating (global mean m) via cache ────
        //    Falls back to BayesianReputationAlgorithm.DEFAULT_GLOBAL_MEAN (3.0)
        //    if the feedback table is empty (cold-start). Cache TTL = 5 min.
        double globalMean = getCachedGlobalMeanRating();

        // ── 3. Count this family's COMPLETED helper tasks ─────────────────────
        long completedTasks = helpTaskRepository.findByHelperFamilyId(familyId).stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .count();

        // ── 4. Delegate scoring to the standalone algorithm ───────────────────
        BigDecimal reliabilityScore = BayesianReputationAlgorithm.compute(
                totalReviews,
                avgRatingRaw,
                globalMean,
                completedTasks
        );

        BigDecimal avgRating = totalReviews > 0
                ? BigDecimal.valueOf(avgRatingRaw).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // ── 5. Persist ────────────────────────────────────────────────────────
        Sec103_1093910_Reputation reputation = reputationRepository.findByFamilyId(familyId)
                .orElseGet(() -> {
                    Sec103_1093910_Reputation r = new Sec103_1093910_Reputation();
                    com.abatye.family_help_uae.model.Sec103_1093910_Family family =
                            familyRepository.findById(familyId).orElseThrow(() ->
                            new Sec103_1093910_ResourceNotFoundException("Family not found"));
                    r.setFamily(family);
                    return r;
                });

        reputation.setAvgRating(avgRating);
        reputation.setTotalReviews(totalReviews);
        reputation.setCompletedTasks((int) completedTasks);
        reputation.setReliabilityScore(reliabilityScore);
        reputation.setLastUpdated(LocalDateTime.now());

        return reputationRepository.save(reputation);
    }
}
