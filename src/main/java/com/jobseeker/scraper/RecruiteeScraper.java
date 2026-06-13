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
public class RecruiteeScraper extends BaseScraper {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return "RECRUITEE";
    }

    @Override
    public List<Job> scrape(String companySlug, FilterRequestDTO filters) {
        List<Job> jobs = new ArrayList<>();

        String url = "https://" + companySlug + ".recruitee.com/api/offers/";

        try {
            log.info("Scraping Recruitee jobs for company: {} at URL: {}", companySlug, url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isBlank()) {
                log.warn("Empty Recruitee response for company: {}", companySlug);
                return jobs;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode offersNode = root.path("offers");

            if (!offersNode.isArray()) {
                log.warn("Recruitee response did not contain offers array for company: {}", companySlug);
                return jobs;
            }

            for (JsonNode offerNode : offersNode) {
                Job job = mapToJob(companySlug, offerNode);
                if (job != null) {
                    jobs.add(job);
                }
            }

            log.info("Scraped {} jobs from Recruitee for: {}", jobs.size(), companySlug);

        } catch (Exception e) {
            log.warn("Failed to scrape Recruitee jobs for {}: {}", companySlug, e.getMessage());
        }

        return jobs;
    }

    private Job mapToJob(String companySlug, JsonNode node) {
        String title = firstNonBlank(text(node, "title"), text(node, "name"));
        String location = buildLocation(node);

        String description = firstNonBlank(
                text(node, "description"),
                firstNonBlank(
                        text(node, "description_html"),
                        firstNonBlank(
                                text(node, "requirements"),
                                firstNonBlank(text(node, "requirements_html"), text(node, "body"))
                        )
                )
        );

        String jobUrl = firstNonBlank(
                text(node, "careers_url"),
                firstNonBlank(
                        text(node, "career_url"),
                        firstNonBlank(
                                text(node, "url"),
                                firstNonBlank(text(node, "job_portal_url"), text(node, "apply_url"))
                        )
                )
        );

        String slug = firstNonBlank(text(node, "slug"), text(node, "id"));

        if (jobUrl.isBlank() && !slug.isBlank()) {
            jobUrl = "https://" + companySlug + ".recruitee.com/o/" + slug;
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
                .source("RECRUITEE")
                .active(true)
                .scrapedAt(LocalDateTime.now())
                .matchScore(0.0)
                .build();
    }

    private String buildLocation(JsonNode node) {
        String directLocation = text(node, "location");
        if (!directLocation.isBlank()) {
            return directLocation;
        }

        String city = text(node, "city");
        String country = text(node, "country");

        if (!city.isBlank() && !country.isBlank()) {
            return city + ", " + country;
        }

        if (!city.isBlank()) {
            return city;
        }

        if (!country.isBlank()) {
            return country;
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
                            firstNonBlank(
                                    text(loc, "city"),
                                    firstNonBlank(text(loc, "country"), text(loc, "country_code"))
                            )
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

        boolean remote = node.path("remote").asBoolean(false);
        return remote ? "Remote" : "";
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }

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
