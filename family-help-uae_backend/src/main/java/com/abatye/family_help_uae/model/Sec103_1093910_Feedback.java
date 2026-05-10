package com.abatye.family_help_uae.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "feedback",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "UQ_feedback_task_reviewer",
            columnNames = {"help_task_id", "reviewer_family_id"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sec103_1093910_Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "help_task_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // accepted on POST input, excluded from response output
    private Sec103_1093910_HelpTask helpTask;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_family_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_Family reviewerFamily;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_family_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_Family targetFamily;

    /**
     * Rating must be BETWEEN 1 AND 5 (enforced via @Check or service layer).
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
