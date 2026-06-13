package com.jobseeker.scraper;

import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GreenhouseScraper extends BaseScraper {

    private static final String BASE_URL = "https://boards.greenhouse.io/";
    private static final String SOURCE = "GREENHOUSE";

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        String url = BASE_URL + companySlug;
        List<Job> jobs = new ArrayList<>();

        log.info("Scraping Greenhouse jobs for company: {} at URL: {}", companySlug, url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                               "AppleWebKit/537.36 (KHTML, like Gecko) " +
                               "Chrome/120.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .get();

            // Greenhouse boards typically list openings in <div class="opening">
            Elements openings = doc.select("div.opening");

            for (Element opening : openings) {
                try {
                    Element linkElement = opening.selectFirst("a");
                    if (linkElement == null) continue;

                    String title = linkElement.text().trim();
                    String jobUrl = linkElement.attr("abs:href");

                    Element locationElement = opening.selectFirst("span.location");
                    String location = (locationElement != null) ? locationElement.text().trim() : "Not specified";

                    // Attempt to extract experience level from title
                    String experienceLevel = extractExperienceLevel(title);

                    Job job = Job.builder()
                            .title(title)
                            .company(companySlug)
                            .location(location)
                            .experienceLevel(experienceLevel)
                            .jobUrl(jobUrl)
                            .source(SOURCE)
                            .scrapedAt(LocalDateTime.now())
                            .active(true)
                            .build();

                    if (matchesCriteria(job, filters)) {
                        jobs.add(job);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing individual job opening: {}", e.getMessage());
                }
            }

            log.info("Scraped {} jobs from Greenhouse for company: {}", jobs.size(), companySlug);

        } catch (IOException e) {
            log.error("Failed to scrape Greenhouse for company {}: {}", companySlug, e.getMessage());
        }

        return jobs;
    }

    /**
     * Attempts to extract experience level from the job title.
     */
    private String extractExperienceLevel(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("intern")) return "Intern";
        if (lower.contains("junior") || lower.contains("jr") || lower.contains("entry")) return "Junior";
        if (lower.contains("mid") || lower.contains("intermediate")) return "Mid";
        if (lower.contains("senior") || lower.contains("sr")) return "Senior";
        if (lower.contains("staff")) return "Staff";
        if (lower.contains("principal")) return "Principal";
        if (lower.contains("lead")) return "Lead";
        if (lower.contains("manager") || lower.contains("director")) return "Manager";
        return "Not specified";
    }
}
