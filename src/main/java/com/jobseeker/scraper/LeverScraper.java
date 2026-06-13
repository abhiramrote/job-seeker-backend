package com.jobseeker.scraper;

import com.fasterxml.jackson.core.type.TypeReference;
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

    private static final String BASE_URL = "https://api.lever.co/v0/postings/";
    private static final String SOURCE = "LEVER";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        String url = BASE_URL + companySlug;
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

            if (postings.isArray()) {
                for (JsonNode posting : postings) {
                    try {
                        String title = getTextValue(posting, "text");
                        String jobUrl = getTextValue(posting, "hostedUrl");

                        // Location from categories
                        JsonNode categories = posting.get("categories");
                        String location = "Not specified";
                        String experienceLevel = "Not specified";
                        if (categories != null) {
                            location = getTextValue(categories, "location");
                            String commitment = getTextValue(categories, "commitment");
                            if (commitment != null && !commitment.isEmpty()) {
                                experienceLevel = commitment;
                            }
                        }

                        // Description from descriptionPlain
                        String description = getTextValue(posting, "descriptionPlain");

                        Job job = Job.builder()
                                .title(title)
                                .company(companySlug)
                                .location(location != null ? location : "Not specified")
                                .experienceLevel(experienceLevel)
                                .jobUrl(jobUrl)
                                .description(description)
                                .source(SOURCE)
                                .scrapedAt(LocalDateTime.now())
                                .active(true)
                                .build();

                        if (matchesCriteria(job, filters)) {
                            jobs.add(job);
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing Lever posting: {}", e.getMessage());
                    }
                }
            }

            log.info("Scraped {} jobs from Lever for company: {}", jobs.size(), companySlug);

        } catch (IOException | InterruptedException e) {
            log.error("Failed to scrape Lever for company {}: {}", companySlug, e.getMessage());
            Thread.currentThread().interrupt();
        }

        return jobs;
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }
}
