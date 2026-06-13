package com.jobseeker.scraper;

import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.model.Job;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class BaseScraper {

    /**
     * Scrape jobs from a specific company portal.
     *
     * @param companyIdentifier the company slug or identifier for the portal
     * @param filters           optional filters to apply during scraping
     * @return list of scraped Job entities
     */
    public abstract List<Job> scrape(String companyIdentifier, FilterRequestDTO filters);

    /**
     * Returns the name of the source portal (e.g., GREENHOUSE, LEVER).
     */
    public abstract String getSourceName();

    /**
     * Check if a job matches the given filter criteria.
     */
    protected boolean matchesCriteria(Job job, FilterRequestDTO filters) {
        if (filters == null) {
            return true;
        }

        // Filter by location
        if (filters.getLocation() != null && !filters.getLocation().isBlank()) {
            if (job.getLocation() == null ||
                !job.getLocation().toLowerCase().contains(filters.getLocation().toLowerCase())) {
                return false;
            }
        }

        // Filter by experience level
        if (filters.getExperienceLevel() != null && !filters.getExperienceLevel().isBlank()) {
            if (job.getExperienceLevel() == null ||
                !job.getExperienceLevel().toLowerCase().contains(filters.getExperienceLevel().toLowerCase())) {
                return false;
            }
        }

        // Filter by keywords in title or description
        if (filters.getKeywords() != null && !filters.getKeywords().isBlank()) {
            String keyword = filters.getKeywords().toLowerCase();
            boolean titleMatch = job.getTitle() != null &&
                                 job.getTitle().toLowerCase().contains(keyword);
            boolean descMatch = job.getDescription() != null &&
                                job.getDescription().toLowerCase().contains(keyword);
            if (!titleMatch && !descMatch) {
                return false;
            }
        }

        return true;
    }
}
