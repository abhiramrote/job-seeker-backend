package com.jobseeker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_job_url", columnList = "jobUrl", unique = true),
        @Index(name = "idx_company", columnList = "company"),
        @Index(name = "idx_experience_level", columnList = "experienceLevel")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    private String experienceLevel;

    @Column(nullable = false, unique = true, length = 1024)
    private String jobUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String source; // e.g., GREENHOUSE, LEVER, WORKDAY

    private Double matchScore;

    @Column(nullable = false)
    private LocalDateTime scrapedAt;

    @Builder.Default
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (scrapedAt == null) {
            scrapedAt = LocalDateTime.now();
        }
    }
}
