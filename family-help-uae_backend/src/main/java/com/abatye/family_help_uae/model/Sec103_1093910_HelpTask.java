package com.abatye.family_help_uae.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "help_task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sec103_1093910_HelpTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * XOR constraint: either help_request_id OR help_offer_id must be NOT NULL (not both, not neither).
     * Enforced at the application/DB level; both are nullable at the column level.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "help_request_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_HelpRequest helpRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "help_offer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_HelpOffer helpOffer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_HelpCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_family_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_Family requesterFamily;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "helper_family_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sec103_1093910_Family helperFamily;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "strated_at", nullable = false)
    private LocalDateTime stratedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 0;

    @PrePersist
    protected void onCreate() {
        if (stratedAt == null) {
            stratedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (version == null) {
            version = 0;
        }
    }
}
