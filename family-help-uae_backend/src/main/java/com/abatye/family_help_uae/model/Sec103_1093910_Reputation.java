package com.abatye.family_help_uae.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "reputation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sec103_1093910_Reputation {

    @Id
    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    @JoinColumn(name = "family_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_Family family;

    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private java.math.BigDecimal avgRating;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews;

    @Column(name = "completed_tasks", nullable = false)
    private Integer completedTasks;

    @Column(name = "reliability_score", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal reliabilityScore;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
