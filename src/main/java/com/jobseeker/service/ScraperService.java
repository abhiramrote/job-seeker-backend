package com.jobseeker.service;

import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.model.Job;
import com.jobseeker.repository.JobRepository;
import com.jobseeker.scraper.BaseScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ScraperService {

    private final JobRepository jobRepository;
    private final List<BaseScraper> scrapers;

    @Value("${scraper.target-companies.greenhouse:#{T(java.util.Collections).emptyList()}}")
    private List<String> greenhouseCompanies;

    @Value("${scraper.target-companies.lever:#{T(java.util.Collections).emptyList()}}")
    private List<String> leverCompanies;

    @Value("${scraper.target-companies.workday:#{T(java.util.Collections).emptyList()}}")
    private List<String> workdayCompanies;

    public ScraperService(JobRepository jobRepository, List<BaseScraper> scrapers) {
        this.jobRepository = jobRepository;
        this.scrapers = scrapers;
    }

    /**
     * Scrape all configured portals and save jobs to the database.
     */
    public List<Job> scrapeAllPortals() {
        return scrapeAllPortals(null);
    }

    /**
     * Scrape all configured portals with optional filters.
     */
    public List<Job> scrapeAllPortals(FilterRequestDTO filters) {
        List<Job> allJobs = new ArrayList<>();

        for (BaseScraper scraper : scrapers) {
            List<String> companies = getCompaniesForScraper(scraper.getSourceName());
            for (String company : companies) {
                try {
                    List<Job> jobs = scraper.scrape(company, filters);
                    List<Job> savedJobs = saveJobsWithDeduplication(jobs);
                    allJobs.addAll(savedJobs);
                    log.info("Saved {} new jobs from {} for company: {}",
                             savedJobs.size(), scraper.getSourceName(), company);
                } catch (Exception e) {
                    log.error("Error scraping {} for company {}: {}",
                              scraper.getSourceName(), company, e.getMessage());
                }
            }
        }

        log.info("Total new jobs scraped and saved: {}", allJobs.size());
        return allJobs;
    }

    /**
     * Save jobs with deduplication based on jobUrl.
     */
    private List<Job> saveJobsWithDeduplication(List<Job> jobs) {
        List<Job> newJobs = new ArrayList<>();
        for (Job job : jobs) {
            Optional<Job> existing = jobRepository.findByJobUrl(job.getJobUrl());
            if (existing.isEmpty()) {
                Job saved = jobRepository.save(job);
                newJobs.add(saved);
            } else {
                // Update existing job's active status and scrapedAt timestamp
                Job existingJob = existing.get();
                existingJob.setActive(true);
                existingJob.setScrapedAt(job.getScrapedAt());
                jobRepository.save(existingJob);
            }
        }
        return newJobs;
    }

    /**
     * Get the list of target companies for a given scraper source.
     */
    private List<String> getCompaniesForScraper(String sourceName) {
        return switch (sourceName.toUpperCase()) {
            case "GREENHOUSE" -> greenhouseCompanies;
            case "LEVER" -> leverCompanies;
            case "WORKDAY" -> workdayCompanies;
            default -> Collections.emptyList();
        };
    }
}
