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
public class SmartRecruitersScraper extends BaseScraper {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return "SMARTRECRUITERS";
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        List<Job> jobs = new ArrayList<>();

        int limit = 100;
        int offset = 0;
        int totalFound = Integer.MAX_VALUE;

        while (offset < totalFound) {
            String url = "https://api.smartrecruiters.com/v1/companies/"
        + companySlug
        + "/postings?limit="
        + limit
        + "&offset="
        + offset
        + "&country=in";

            try {
                log.info("Scraping SmartRecruiters jobs for company: {} at URL: {}", companySlug, url);

                String response = restTemplate.getForObject(url, String.class);

                if (response == null || response.isBlank()) {
                    log.warn("Empty SmartRecruiters response for company: {}", companySlug);
                    break;
                }

                JsonNode root = objectMapper.readTree(response);

                totalFound = root.path("totalFound").asInt(0);
                JsonNode content = root.path("content");

                if (!content.isArray()) {
                    log.warn("SmartRecruiters response did not contain content array for company: {}", companySlug);
                    break;
                }

                if (content.isEmpty()) {
                    break;
                }

                for (JsonNode node : content) {
                    Job job = mapToJob(companySlug, node);

                    if (job != null) {
                        jobs.add(job);
                    }
                }

                offset += limit;

            } catch (Exception e) {
                log.warn("Failed to scrape SmartRecruiters jobs for {}: {}", companySlug, e.getMessage());
                break;
            }
        }

        log.info("Scraped {} jobs from SmartRecruiters for: {}", jobs.size(), companySlug);
        return jobs;
    }

    private Job mapToJob(String companySlug, JsonNode node) {
        String title = firstNonBlank(text(node, "name"), text(node, "title"));

        String location = buildLocation(node);

        String jobUrl = firstNonBlank(
                text(node, "postingUrl"),
                firstNonBlank(text(node, "applyUrl"), text(node, "ref"))
        );

        if (jobUrl.isBlank()) {
            String id = text(node, "id");
            if (!id.isBlank()) {
                jobUrl = "https://jobs.smartrecruiters.com/" + companySlug + "/" + id;
            }
        }

        if (title.isBlank() || jobUrl.isBlank()) {
            return null;
        }

        String description = "";
        // String ref = text(node, "ref");

        // if (!ref.isBlank()) {
        //     description = fetchDescription(ref);
        // }

        return Job.builder()
                .title(title)
                .company(companySlug)
                .location(location)
                .experienceLevel(text(node.path("experienceLevel"), "label"))
                .description(description)
                .jobUrl(jobUrl)
                .source("SMARTRECRUITERS")
                .active(true)
                .scrapedAt(LocalDateTime.now())
                .matchScore(0.0)
                .build();
    }

    private String fetchDescription(String refUrl) {
        try {
            String response = restTemplate.getForObject(refUrl, String.class);

            if (response == null || response.isBlank()) {
                return "";
            }

            JsonNode root = objectMapper.readTree(response);

            StringBuilder sb = new StringBuilder();

            appendIfPresent(sb, root.path("jobAd").path("sections").path("jobDescription").path("text").asText(""));
            appendIfPresent(sb, root.path("jobAd").path("sections").path("qualifications").path("text").asText(""));
            appendIfPresent(sb, root.path("jobAd").path("sections").path("additionalInformation").path("text").asText(""));

            return stripHtml(sb.toString());

        } catch (Exception e) {
            return "";
        }
    }

    private void appendIfPresent(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            sb.append(value).append("\n");
        }
    }

    private String buildLocation(JsonNode node) {
        JsonNode location = node.path("location");

        List<String> parts = new ArrayList<>();

        String city = text(location, "city");
        String region = text(location, "region");
        String country = text(location, "country");

        if (!city.isBlank()) parts.add(city);
        if (!region.isBlank()) parts.add(region);
        if (!country.isBlank()) parts.add(country);

        boolean remote = location.path("remote").asBoolean(false);
        if (remote) {
            parts.add("Remote");
        }

        return String.join(", ", parts);
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