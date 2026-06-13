package com.jobseeker.scheduler;

import com.jobseeker.service.PersonalScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalJobScheduler {

    private final PersonalScraperService personalScraperService;

    /**
     * Runs every 6 hours — scrapes all companies and sends WhatsApp alerts.
     * Cron: 0 0 0,6,12,18 * * * (midnight, 6am, noon, 6pm)
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void scheduledPersonalScrape() {
        log.info("⏰ Scheduled personal scrape triggered");
        try {
            var result = personalScraperService.scrapeAndNotify();
            log.info("⏰ Scheduled scrape done. Matched: {}", result.get("matchedJobs"));
        } catch (Exception e) {
            log.error("⏰ Scheduled scrape failed: {}", e.getMessage(), e);
        }
    }
}
