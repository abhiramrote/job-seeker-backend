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
public class WorkableScraper extends BaseScraper {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return "WORKABLE";
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        List<Job> jobs = new ArrayList<>();

        List<String> urls = List.of(
                "https://www.workable.com/api/accounts/" + companySlug + "?details=true",
                "https://apply.workable.com/api/v1/widget/accounts/" + companySlug
        );

        for (String url : urls) {
            try {
                log.info("Scraping Workable jobs for company: {} at URL: {}", companySlug, url);

                String response = restTemplate.getForObject(url, String.class);

                if (response == null || response.isBlank()) {
                    log.warn("Empty Workable response for company: {}", companySlug);
                    continue;
                }

                JsonNode root = objectMapper.readTree(response);
                JsonNode jobsNode = findJobsNode(root);

                if (!jobsNode.isArray()) {
                    log.warn("Workable response did not contain jobs array for company: {}", companySlug);
                    continue;
                }

                for (JsonNode node : jobsNode) {
                    Job job = mapToJob(companySlug, node);

                    if (job != null) {
                        jobs.add(job);
                    }
                }

                if (!jobs.isEmpty()) {
                    log.info("Scraped {} jobs from Workable for: {}", jobs.size(), companySlug);
                    return jobs;
                }

            } catch (Exception e) {
                log.warn("Failed Workable URL for {}: {} | {}", companySlug, url, e.getMessage());
            }
        }

        log.info("Scraped 0 jobs from Workable for: {}", companySlug);
        return jobs;
    }

    private JsonNode findJobsNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return objectMapper.createArrayNode();
        }

        if (root.path("jobs").isArray()) {
            return root.path("jobs");
        }

        if (root.path("results").isArray()) {
            return root.path("results");
        }

        if (root.path("data").path("jobs").isArray()) {
            return root.path("data").path("jobs");
        }

        if (root.path("account").path("jobs").isArray()) {
            return root.path("account").path("jobs");
        }

        return objectMapper.createArrayNode();
    }

    private Job mapToJob(String companySlug, JsonNode node) {
        String title = firstNonBlank(
                text(node, "title"),
                text(node, "name")
        );

        String location = buildLocation(node);

        String description = firstNonBlank(
                text(node, "description"),
                firstNonBlank(
                        text(node, "full_description"),
                        firstNonBlank(
                                text(node, "requirements"),
                                text(node, "benefits")
                        )
                )
        );

        String shortcode = firstNonBlank(
                text(node, "shortcode"),
                text(node, "code")
        );

        String jobUrl = firstNonBlank(
                text(node, "url"),
                firstNonBlank(
                        text(node, "shortlink"),
                        firstNonBlank(
                                text(node, "application_url"),
                                firstNonBlank(
                                        text(node, "apply_url"),
                                        text(node, "hosted_url")
                                )
                        )
                )
        );

        if (jobUrl.isBlank() && !shortcode.isBlank()) {
            jobUrl = "https://apply.workable.com/" + companySlug + "/j/" + shortcode + "/";
        }

        if (title.isBlank() || jobUrl.isBlank()) {
            return null;
        }

        return Job.builder()
                .title(title)
                .company(companySlug)
                .location(location)
                .experienceLevel("")
                .description(stripHtml(description))
                .jobUrl(jobUrl)
                .source("WORKABLE")
                .active(true)
                .scrapedAt(LocalDateTime.now())
                .matchScore(0.0)
                .build();
    }

    private String buildLocation(JsonNode node) {
        JsonNode locationNode = node.path("location");

        if (locationNode.isTextual()) {
            return locationNode.asText("");
        }

        if (locationNode.isObject()) {
            List<String> parts = new ArrayList<>();

            String city = firstNonBlank(
                    text(locationNode, "city"),
                    text(locationNode, "name")
            );

            String region = firstNonBlank(
                    text(locationNode, "region"),
                    text(locationNode, "state")
            );

            String country = text(locationNode, "country");

            if (!city.isBlank()) parts.add(city);
            if (!region.isBlank()) parts.add(region);
            if (!country.isBlank()) parts.add(country);

            return String.join(", ", parts);
        }

        JsonNode locationsNode = node.path("locations");

        if (locationsNode.isArray()) {
            List<String> locations = new ArrayList<>();

            for (JsonNode loc : locationsNode) {
                String value = "";

                if (loc.isTextual()) {
                    value = loc.asText("");
                } else if (loc.isObject()) {
                    value = firstNonBlank(
                            text(loc, "name"),
                            firstNonBlank(text(loc, "city"), text(loc, "country"))
                    );
                }

                if (!value.isBlank() && !locations.contains(value)) {
                    locations.add(value);
                }
            }

            if (!locations.isEmpty()) {
                return String.join("; ", locations);
            }
        }

        String workplace = firstNonBlank(
                text(node, "workplace"),
                text(node, "workplace_type")
        );

        return workplace;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
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