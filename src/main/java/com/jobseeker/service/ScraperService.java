package com.jobseeker.service;

import com.jobseeker.config.ScraperProperties;
import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.model.Job;
import com.jobseeker.repository.JobRepository;
import com.jobseeker.scraper.BaseScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ScraperService {

    private final JobRepository jobRepository;
    private final List<BaseScraper> scrapers;
    private final ScraperProperties scraperProperties;

    public ScraperService(JobRepository jobRepository, List<BaseScraper> scrapers,
                          ScraperProperties scraperProperties) {
        this.jobRepository = jobRepository;
        this.scrapers = scrapers;
        this.scraperProperties = scraperProperties;
    }

    public List<Job> scrapeAllPortals() {
        return scrapeAllPortals(null);
    }

    public List<Job> scrapeAllPortals(FilterRequestDTO filters) {
        List<Job> allJobs = new ArrayList<>();

        log.info("=== Starting scrape. Available scrapers: {} ===", scrapers.size());

        for (BaseScraper scraper : scrapers) {
            String sourceName = scraper.getSourceName().toLowerCase();
            List<String> companies = scraperProperties.getCompaniesForSource(sourceName);

            log.info("Scraper [{}] has {} target companies: {}", 
                     scraper.getSourceName(), companies.size(), companies);

            for (String company : companies) {
                try {
                    log.info(">>> Scraping {} for company: {}", scraper.getSourceName(), company);
                    List<Job> jobs = scraper.scrape(company, filters);
                    log.info("<<< Got {} jobs from {} for {}", jobs.size(), scraper.getSourceName(), company);

                    List<Job> savedJobs = saveJobsWithDeduplication(jobs);
                    allJobs.addAll(savedJobs);

                    log.info("Saved {} new jobs from {} for company: {}",
                             savedJobs.size(), scraper.getSourceName(), company);
                } catch (Exception e) {
                    log.error("Error scraping {} for company {}: {}",
                              scraper.getSourceName(), company, e.getMessage(), e);
                }
            }
        }

        log.info("=== Total new jobs scraped and saved: {} ===", allJobs.size());
        return allJobs;
    }

    private List<Job> saveJobsWithDeduplication(List<Job> jobs) {
        List<Job> newJobs = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getJobUrl() == null || job.getJobUrl().isBlank()) {
                log.warn("Skipping job with null/empty URL: {}", job.getTitle());
                continue;
            }
            Optional<Job> existing = jobRepository.findByJobUrl(job.getJobUrl());
            if (existing.isEmpty()) {
                Job saved = jobRepository.save(job);
                newJobs.add(saved);
            } else {
                Job existingJob = existing.get();
                existingJob.setActive(true);
                existingJob.setScrapedAt(job.getScrapedAt());
                jobRepository.save(existingJob);
            }
        }
        return newJobs;
    }
}
