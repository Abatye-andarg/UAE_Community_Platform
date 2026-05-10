package com.abatye.family_help_uae.reputation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standalone Bayesian Reputation Algorithm (Bayesian Rating + Task Bonus).
 *
 * <h2>Why Bayesian Average?</h2>
 * <p>A plain arithmetic mean is unreliable for families with very few ratings.
 * A family with 1 review rated 5.0 would rank higher than a family with 50 reviews
 * averaging 4.8 — which is clearly unfair and easily gamed.</p>
 *
 * <p>The Bayesian Average solves this by "pulling" a family's score toward the
 * platform-wide mean ({@code m}) when they have few reviews. The more reviews they
 * accumulate, the more their own ratings dominate the score.</p>
 *
 * <h2>Formula</h2>
 * <pre>
 *   bayesianRating = (C × m + n × avgRating) / (C + n)
 *
 *   taskBonus      = min(completedTasks / TASK_BONUS_THRESHOLD, 1.0) × MAX_TASK_BONUS
 *
 *   reliabilityScore = clamp(bayesianRating + taskBonus, 0, MAX_SCORE)
 * </pre>
 *
 * <h2>Parameters</h2>
 * <ul>
 *   <li><b>n</b> – number of ratings this family has received.</li>
 *   <li><b>avgRating</b> – arithmetic mean of this family's received ratings (1–5).</li>
 *   <li><b>m</b> – global mean rating across ALL families on the platform.
 *       Supplied by the caller from the database. Falls back to
 *       {@link #DEFAULT_GLOBAL_MEAN} when no global data is available yet.</li>
 *   <li><b>completedTasks</b> – number of tasks this family completed as a helper.</li>
 *   <li><b>C</b> ({@link #CONFIDENCE_WEIGHT}) – minimum number of reviews required
 *       before a family's own ratings are fully trusted. With {@code C = 5}, a new
 *       family with 0 reviews gets exactly {@code m} (the global mean). At 5 reviews
 *       their score is 50/50 between {@code m} and their own average. At 50 reviews
 *       their own average dominates (91% weight). Increase C to make the system more
 *       conservative; decrease it to trust reviews faster.</li>
 *   <li><b>MAX_TASK_BONUS</b> ({@link #MAX_TASK_BONUS}) – maximum extra points
 *       awarded for task completion history. Capped at {@code 0.5} to prevent
 *       task-farming from fully compensating poor ratings.</li>
 *   <li><b>TASK_BONUS_THRESHOLD</b> ({@link #TASK_BONUS_THRESHOLD}) – number of
 *       completed tasks required to earn the full task bonus.</li>
 * </ul>
 *
 * <h2>Score Range</h2>
 * <p>Output is always in [0.0, 5.0], returned as a {@link BigDecimal} rounded to
 * 2 decimal places.</p>
 *
 * <h2>Design decision — standalone class</h2>
 * <p>This class is intentionally framework-agnostic (no Spring, no JPA). It takes
 * only plain numbers as inputs and returns a {@link BigDecimal}. This makes it:</p>
 * <ul>
 *   <li>Unit-testable without a Spring context or database.</li>
 *   <li>Reusable if the scoring is ever needed outside the reputation service.</li>
 *   <li>Easy to swap or tune without touching persistence logic.</li>
 * </ul>
 */
public final class BayesianReputationAlgorithm {

    // ── Configuration constants ────────────────────────────────────────────────

    /**
     * Confidence weight (C).
     *
     * <p>Represents the "virtual" number of reviews at the global mean that every
     * family starts with. A higher value means the platform trusts new families less
     * and requires more reviews before their own ratings carry full weight.</p>
     *
     * <p>Example effect at C = 5:</p>
     * <pre>
     *   n = 0   → score = m              (no reviews, pure global mean)
     *   n = 5   → score = (m + avgRating) / 2   (50/50 split)
     *   n = 50  → score ≈ 0.91 × avgRating + 0.09 × m  (own ratings dominate)
     * </pre>
     */
    public static final double CONFIDENCE_WEIGHT = 5.0;

    /**
     * Maximum bonus points added for task completion history.
     *
     * <p>Capped so that a prolific helper who received no ratings cannot reach the
     * top of the leaderboard purely through task volume.</p>
     */
    public static final double MAX_TASK_BONUS = 0.5;

    /**
     * Number of completed tasks required to earn the full {@link #MAX_TASK_BONUS}.
     * Families with fewer tasks receive a proportionally smaller bonus.
     */
    public static final int TASK_BONUS_THRESHOLD = 10;

    /**
     * Fallback global mean used when no feedback exists on the platform yet
     * (cold-start scenario). Set to the midpoint of the 1–5 scale.
     */
    public static final double DEFAULT_GLOBAL_MEAN = 3.0;

    /** Maximum possible reliability score (mirrors the 5-star rating scale). */
    public static final double MAX_SCORE = 5.0;

    // ── Private constructor — utility class, not instantiable ─────────────────

    private BayesianReputationAlgorithm() {
        throw new UnsupportedOperationException("Utility class — do not instantiate.");
    }

   
    /**
     * Computes the Bayesian reliability score for a family.
     *
     * @param n              number of ratings this family has received (≥ 0)
     * @param avgRating      arithmetic mean of this family's received ratings (1–5),
     *                       ignored when {@code n == 0}
     * @param globalMean     platform-wide average rating across all families (1–5).
     *                       Pass {@link #DEFAULT_GLOBAL_MEAN} if unavailable.
     * @param completedTasks number of tasks completed as a helper (≥ 0)
     * @return reliability score in [0.00, 5.00], rounded to 2 decimal places
     * @throws IllegalArgumentException if any numeric input is out of its valid range
     */
    public static BigDecimal compute(int n, double avgRating, double globalMean, long completedTasks) {
        validateInputs(n, avgRating, globalMean, completedTasks);

        // ── Step 1: Bayesian Average of ratings ───────────────────────────────
        //
        //   bayesianRating = (C × m + n × avgRating) / (C + n)
        //
        // When n = 0 this reduces to m (the global mean).
        // As n grows, the family's own avgRating increasingly dominates.
        double bayesianRating = (CONFIDENCE_WEIGHT * globalMean + n * avgRating)
                                / (CONFIDENCE_WEIGHT + n);

        // ── Step 2: Task Completion Bonus ─────────────────────────────────────
        //
        //   taskBonus = min(completedTasks / TASK_BONUS_THRESHOLD, 1.0) × MAX_TASK_BONUS
        //
        // Linearly scales from 0 (zero tasks) to MAX_TASK_BONUS (TASK_BONUS_THRESHOLD+
        // tasks). Rewards active helpers without letting task volume fully substitute
        // for community ratings.
        double taskBonusFraction = Math.min((double) completedTasks / TASK_BONUS_THRESHOLD, 1.0);
        double taskBonus = taskBonusFraction * MAX_TASK_BONUS;

        // ── Step 3: Combine and clamp to [0, MAX_SCORE] ───────────────────────
        double rawScore = Math.min(bayesianRating + taskBonus, MAX_SCORE);
        rawScore = Math.max(rawScore, 0.0); // defensive lower clamp

        return BigDecimal.valueOf(rawScore).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private static void validateInputs(int n, double avgRating, double globalMean, long completedTasks) {
        if (n < 0) {
            throw new IllegalArgumentException("Review count n must be ≥ 0, got: " + n);
        }
        if (n > 0 && (avgRating < 1.0 || avgRating > 5.0)) {
            throw new IllegalArgumentException(
                    "avgRating must be in [1, 5] when n > 0, got: " + avgRating);
        }
        if (globalMean < 1.0 || globalMean > 5.0) {
            throw new IllegalArgumentException(
                    "globalMean must be in [1, 5], got: " + globalMean);
        }
        if (completedTasks < 0) {
            throw new IllegalArgumentException("completedTasks must be ≥ 0, got: " + completedTasks);
        }
    }
}
