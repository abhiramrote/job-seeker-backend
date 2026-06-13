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
public class GreenhouseScraper extends BaseScraper {

    private static final String API_URL = "https://boards-api.greenhouse.io/v1/boards/%s/jobs?content=true";
    private static final String SOURCE = "GREENHOUSE";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String getSourceName() {
        return SOURCE;
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        String url = String.format(API_URL, companySlug);
        List<Job> jobs = new ArrayList<>();

        log.info("Scraping Greenhouse jobs for company: {} via API", companySlug);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Greenhouse API returned status {} for company: {}", response.statusCode(), companySlug);
                return jobs;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode jobsArray = root.get("jobs");

            if (jobsArray == null || !jobsArray.isArray()) {
                log.warn("No jobs array found for company: {}", companySlug);
                return jobs;
            }

            for (JsonNode jobNode : jobsArray) {
                try {
                    String title = getText(jobNode, "title");
                    String jobUrl = getText(jobNode, "absolute_url");
                    String location = "Not specified";
                    JsonNode locNode = jobNode.get("location");
                    if (locNode != null) {
                        String locName = getText(locNode, "name");
                        if (locName != null && !locName.isBlank()) location = locName;
                    }
                    String description = null;
                    String contentHtml = getText(jobNode, "content");
                    if (contentHtml != null) {
                        description = contentHtml.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
                        if (description.length() > 5000) description = description.substring(0, 5000);
                    }
                    String experienceLevel = extractLevel(title);

                    Job job = Job.builder()
                            .title(title).company(companySlug).location(location)
                            .experienceLevel(experienceLevel).jobUrl(jobUrl)
                            .description(description).source(SOURCE)
                            .scrapedAt(LocalDateTime.now()).active(true).build();

                    if (matchesCriteria(job, filters)) jobs.add(job);
                } catch (Exception e) {
                    log.warn("Error parsing job: {}", e.getMessage());
                }
            }
            log.info("Scraped {} jobs from Greenhouse for: {}", jobs.size(), companySlug);
        } catch (IOException | InterruptedException e) {
            log.error("Failed to scrape Greenhouse for {}: {}", companySlug, e.getMessage());
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
