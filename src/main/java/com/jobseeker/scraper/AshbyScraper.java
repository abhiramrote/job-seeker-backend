package com.jobseeker.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AshbyScraper extends BaseScraper {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return "ASHBY";
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        List<Job> jobs = new ArrayList<>();

        String url = "https://api.ashbyhq.com/posting-api/job-board/"
                + companySlug
                + "?includeCompensation=true";

        try {
            log.info("Scraping Ashby jobs for company: {} at URL: {}", companySlug, url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isBlank()) {
                log.warn("Empty Ashby response for company: {}", companySlug);
                return jobs;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode jobsNode = root.path("jobs");

            if (!jobsNode.isArray()) {
                log.warn("Ashby response did not contain jobs array for company: {}", companySlug);
                return jobs;
            }

            for (JsonNode node : jobsNode) {
                String title = text(node, "title");
                String location = buildLocation(node);

                String description = firstNonBlank(
                        text(node, "descriptionPlain"),
                        stripHtml(text(node, "descriptionHtml"))
                );

                String jobUrl = firstNonBlank(
                        text(node, "jobUrl"),
                        text(node, "applyUrl")
                );

                if (jobUrl.isBlank()) {
                    continue;
                }

                Job job = Job.builder()
                        .title(title)
                        .company(companySlug)
                        .location(location)
                        .experienceLevel("")
                        .description(description)
                        .jobUrl(jobUrl)
                        .source("ASHBY")
                        .active(true)
                        .scrapedAt(LocalDateTime.now())
                        .matchScore(0.0)
                        .build();

                if (matchesAshbyFilters(job, filters)) {
                    jobs.add(job);
                }
            }

            log.info("Scraped {} jobs from Ashby for: {}", jobs.size(), companySlug);

        } catch (Exception e) {
            log.warn("Failed to scrape Ashby jobs for {}: {}", companySlug, e.getMessage());
        }

        return jobs;
    }

    private boolean matchesAshbyFilters(Job job, FilterRequestDTO filters) {
        return true;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private String buildLocation(JsonNode node) {
        String primary = text(node, "location");

        List<String> all = new ArrayList<>();

        if (!primary.isBlank()) {
            all.add(primary);
        }

        JsonNode secondaryLocations = node.path("secondaryLocations");
        if (secondaryLocations.isArray()) {
            for (JsonNode loc : secondaryLocations) {
                String value = text(loc, "location");

                if (!value.isBlank() && !all.contains(value)) {
                    all.add(value);
                }
            }
        }

        String workplaceType = text(node, "workplaceType");
        boolean isRemote = node.path("isRemote").asBoolean(false);

        if (isRemote && all.stream().noneMatch(v -> v.toLowerCase().contains("remote"))) {
            all.add(workplaceType.isBlank() ? "Remote" : workplaceType);
        }

        return String.join("; ", all);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return "";
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        return html
                .replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}