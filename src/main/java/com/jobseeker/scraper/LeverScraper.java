package com.jobseeker.scraper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LeverScraper extends BaseScraper {

    private static final String BASE_URL = "https://api.lever.co/v0/postings/%s?mode=json";
    private static final String SOURCE = "LEVER";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        String url = String.format(BASE_URL, companySlug);
        List<Job> jobs = new ArrayList<>();

        log.info("Scraping Lever jobs for company: {} at URL: {}", companySlug, url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Lever API returned status {} for company: {}", response.statusCode(), companySlug);
                return jobs;
            }

            JsonNode postings = objectMapper.readTree(response.body());

            if (!postings.isArray()) {
                log.warn("Lever API did not return an array for company: {}", companySlug);
                return jobs;
            }

            for (JsonNode posting : postings) {
                try {
                    String title = getText(posting, "text");
                    if (title == null || title.isBlank()) continue;

                    String jobUrl = getText(posting, "hostedUrl");
                    if (jobUrl == null) jobUrl = getText(posting, "applyUrl");

                    String location = "Not specified";
                    String experienceLevel = "Not specified";
                    JsonNode categories = posting.get("categories");
                    if (categories != null) {
                        String loc = getText(categories, "location");
                        if (loc != null && !loc.isBlank()) location = loc;
                        String commitment = getText(categories, "commitment");
                        if (commitment != null && !commitment.isBlank()) experienceLevel = commitment;
                    }

                    String description = getText(posting, "descriptionPlain");
                    if (description == null || description.isBlank()) {
                        String descHtml = getText(posting, "description");
                        if (descHtml != null) {
                            description = descHtml.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
                        }
                    }
                    if (description != null && description.length() > 5000) {
                        description = description.substring(0, 5000);
                    }

                    if ("Not specified".equals(experienceLevel)) {
                        experienceLevel = extractLevel(title);
                    }

                    Job job = Job.builder()
                            .title(title).company(companySlug).location(location)
                            .experienceLevel(experienceLevel).jobUrl(jobUrl)
                            .description(description).source(SOURCE)
                            .scrapedAt(LocalDateTime.now()).active(true).build();

                    if (matchesCriteria(job, filters)) jobs.add(job);
                } catch (Exception e) {
                    log.warn("Error parsing Lever posting: {}", e.getMessage());
                }
            }
            log.info("Scraped {} jobs from Lever for: {}", jobs.size(), companySlug);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to scrape Lever for {}: {}", companySlug, e.getMessage());
            Thread.currentThread().interrupt();
        }
        return jobs;
    }

    private String extractLevel(String title) {
        if (title == null) return "Not specified";
        String l = title.toLowerCase();
        if (l.contains("intern")) return "Intern";
        if (l.contains("junior") || l.contains("jr")) return "Junior";
        if (l.contains("senior") || l.contains("sr")) return "Senior";
        if (l.contains("staff")) return "Staff";
        if (l.contains("principal")) return "Principal";
        if (l.contains("lead")) return "Lead";
        if (l.contains("manager") || l.contains("director")) return "Manager";
        return "Not specified";
    }

    private String getText(JsonNode node, String field) {
        JsonNode f = node.get(field);
        return (f != null && !f.isNull()) ? f.asText() : null;
    }
}
