package com.abatye.family_help_uae.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for the FamilyHelpUAE platform.
 *
 * <h3>Strategy</h3>
 * <p>Caffeine is used as the in-memory cache provider (backed by Spring's cache
 * abstraction). Two named caches are configured with independent TTL and size policies
 * suited to their respective data characteristics:</p>
 *
 * <ul>
 *   <li><b>reputations</b> — stores per-family {@code Reputation} records keyed by
 *       {@code familyId}. Expires 10 minutes after last access; holds at most 500
 *       entries (one per active family). Evicted automatically whenever a feedback
 *       submission or task completion triggers {@code recalculate()}.</li>
 *   <li><b>globalMeanRating</b> — stores the single platform-wide mean rating used by
 *       the Bayesian algorithm as a prior. Refreshed every 5 minutes. A single entry
 *       suffices (max size = 1).</li>
 *   <li><b>helpCategories</b> — stores the full list of help categories (Tutoring,
 *       Transport, Childcare, etc.). Categories are essentially static reference data:
 *       TTL is 6 hours with a max of 1 list entry. Evicted on category create/update/delete.</li>
 * </ul>
 *
 * <h3>Scalability rationale</h3>
 * <p>Reputation reads ({@code GET /api/reputations/{id}}) and category list reads
 * ({@code GET /api/categories}) are the two highest-frequency read paths in the
 * platform. Without caching, every concurrent user browsing profiles or opening a
 * help-offer modal hits the database. With Caffeine, subsequent reads for the same
 * data are served from RAM in O(1), reducing database round-trips proportionally
 * to the cache hit rate.</p>
 */
@Configuration
public class Sec103_1093910_CacheConfig {

    /** Name of the per-family reputation cache. */
    public static final String CACHE_REPUTATIONS      = "reputations";

    /** Name of the single-entry global mean rating cache. */
    public static final String CACHE_GLOBAL_MEAN      = "globalMeanRating";

    /** Name of the help-categories list cache. */
    public static final String CACHE_HELP_CATEGORIES  = "helpCategories";

    /**
     * Registers a {@link CaffeineCacheManager} with three named caches, each
     * configured with its own Caffeine spec for TTL and maximum size.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Reputation cache: expire 10 min after last access, max 500 families
        manager.registerCustomCache(
                CACHE_REPUTATIONS,
                Caffeine.newBuilder()
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .maximumSize(500)
                        .recordStats()          // exposes hit/miss metrics for reporting
                        .build()
        );

        // Global mean cache: refresh every 5 min, only 1 entry ever needed
        manager.registerCustomCache(
                CACHE_GLOBAL_MEAN,
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .maximumSize(1)
                        .recordStats()
                        .build()
        );

        // Help-categories cache: 6-hour TTL, 1 list entry
        manager.registerCustomCache(
                CACHE_HELP_CATEGORIES,
                Caffeine.newBuilder()
                        .expireAfterWrite(6, TimeUnit.HOURS)
                        .maximumSize(1)
                        .recordStats()
                        .build()
        );

        return manager;
    }
}
