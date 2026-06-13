package com.jobseeker.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDTO {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String experienceLevel;
    private String jobUrl;
    private String description;
    private String source;
    private Double matchScore;
    private LocalDateTime scrapedAt;
    private boolean active;

    public static JobResponseDTO fromEntity(com.jobseeker.model.Job job) {
        return JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .experienceLevel(job.getExperienceLevel())
                .jobUrl(job.getJobUrl())
                .description(job.getDescription())
                .source(job.getSource())
                .matchScore(job.getMatchScore())
                .scrapedAt(job.getScrapedAt())
                .active(job.isActive())
                .build();
    }
}
