package com.jobseeker.service;

import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.dto.JobResponseDTO;
import com.jobseeker.model.Job;
import com.jobseeker.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobFilterService {

    private final JobRepository jobRepository;

    /**
     * Get all active jobs with optional filters.
     */
    public List<JobResponseDTO> getFilteredJobs(FilterRequestDTO filters) {
        List<Job> jobs;

        if (filters == null || isEmptyFilter(filters)) {
            jobs = jobRepository.findByActiveTrue();
        } else {
            // Use the first company from the list if provided
            String company = (filters.getCompanies() != null && !filters.getCompanies().isEmpty())
                    ? filters.getCompanies().get(0) : null;

            jobs = jobRepository.findByFilters(
                    company,
                    filters.getLocation(),
                    filters.getExperienceLevel(),
                    filters.getKeywords()
            );

            // Filter by multiple companies if more than one provided
            if (filters.getCompanies() != null && filters.getCompanies().size() > 1) {
                List<String> companiesLower = filters.getCompanies().stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
                jobs = jobs.stream()
                        .filter(j -> companiesLower.contains(j.getCompany().toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filter by minimum match score
            if (filters.getMinMatchScore() != null) {
                jobs = jobs.stream()
                        .filter(j -> j.getMatchScore() != null && j.getMatchScore() >= filters.getMinMatchScore())
                        .collect(Collectors.toList());
            }
        }

        return jobs.stream()
                .map(JobResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single job by ID.
     */
    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        return JobResponseDTO.fromEntity(job);
    }

    private boolean isEmptyFilter(FilterRequestDTO filters) {
        return (filters.getKeywords() == null || filters.getKeywords().isBlank()) &&
               (filters.getLocation() == null || filters.getLocation().isBlank()) &&
               (filters.getExperienceLevel() == null || filters.getExperienceLevel().isBlank()) &&
               (filters.getCompanies() == null || filters.getCompanies().isEmpty()) &&
               filters.getMinMatchScore() == null;
    }
}
