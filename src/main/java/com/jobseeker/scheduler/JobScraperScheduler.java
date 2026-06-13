package com.jobseeker.scheduler;

import com.jobseeker.service.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScraperScheduler {

    private final ScraperService scraperService;

    /**
     * Runs every 6 hours to scrape all configured company portals.
     * Cron: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledScrape() {
        log.info("========================================");
        log.info("Starting scheduled job scraping...");
        log.info("========================================");

        try {
            var newJobs = scraperService.scrapeAllPortals();
            log.info("Scheduled scraping completed. New jobs found: {}", newJobs.size());
        } catch (Exception e) {
            log.error("Scheduled scraping failed: {}", e.getMessage(), e);
        }

        log.info("========================================");
        log.info("Scheduled job scraping finished.");
        log.info("========================================");
    }
}
