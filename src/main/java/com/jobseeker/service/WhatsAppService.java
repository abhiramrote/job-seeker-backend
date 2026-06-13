package com.jobseeker.service;

import com.jobseeker.config.CompanyRegistry.CompanyInfo;
import com.jobseeker.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class WhatsAppService {

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from:}")
    private String from;

    @Value("${twilio.to:}")
    private String to;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendJobAlert(List<Job> jobs, List<Integer> scores) {
        if (!twilioEnabled) {
            log.info("WhatsApp disabled. Job alert would include {} jobs.", jobs == null ? 0 : jobs.size());
            return;
        }

        if (jobs == null || jobs.isEmpty()) {
            log.info("No jobs to send on WhatsApp.");
            return;
        }

        int total = jobs.size();
        int chunkSize = 8;

        sendWhatsApp("Matched Jobs For You\n"
                + "Found " + total + " matched jobs based on your profile.\n"
                + "Sending all matched jobs in compact batches.");

        for (int start = 0; start < total; start += chunkSize) {
            int end = Math.min(start + chunkSize, total);

            StringBuilder msg = new StringBuilder();
            msg.append("Matched Jobs ")
                    .append(start + 1)
                    .append("-")
                    .append(end)
                    .append(" of ")
                    .append(total)
                    .append("\n\n");

            for (int i = start; i < end; i++) {
                Job job = jobs.get(i);
                int score = i < scores.size() ? scores.get(i) : 0;

                msg.append(i + 1).append(". ").append(safe(job.getTitle())).append("\n");
                msg.append("Company: ").append(capitalize(safe(job.getCompany()))).append("\n");
                msg.append("Location: ").append(safe(job.getLocation())).append("\n");
                msg.append("Match: ").append(score).append("%\n");

                if (job.getId() != null) {
                    msg.append("Details: ").append(buildJobDetailsUrl(job)).append("\n");
                }

                msg.append("\n");
            }

            boolean sent = sendWhatsApp(msg.toString());

            if (sent) {
                log.info("WhatsApp matched jobs batch sent: {}-{} of {}", start + 1, end, total);
            } else {
                log.warn("WhatsApp matched jobs batch failed: {}-{} of {}", start + 1, end, total);
            }
        }
    }

    public void sendDailySummary(int totalJobs, int matchedJobs, int savedOrUpdatedJobs) {
        if (!twilioEnabled) {
            log.info("WhatsApp disabled. Daily summary: totalJobs={}, matchedJobs={}, savedOrUpdatedJobs={}",
                    totalJobs, matchedJobs, savedOrUpdatedJobs);
            return;
        }

        String msg = "Daily Job Scrape Summary\n\n"
                + "Total scraped/saved: " + totalJobs + "\n"
                + "Matched jobs: " + matchedJobs + "\n"
                + "Saved/updated: " + savedOrUpdatedJobs + "\n";

        boolean sent = sendWhatsApp(msg);

        if (sent) {
            log.info("Daily summary WhatsApp sent");
        } else {
            log.warn("Daily summary WhatsApp failed");
        }
    }

    public void sendCareerLinks(List<CompanyInfo> companies) {
        if (!twilioEnabled) {
            log.info("WhatsApp disabled. Career links would include {} companies.",
                    companies == null ? 0 : companies.size());
            return;
        }

        if (companies == null || companies.isEmpty()) {
            log.info("No career links to send.");
            return;
        }

        int chunkSize = 8;
        int total = companies.size();

        for (int start = 0; start < total; start += chunkSize) {
            int end = Math.min(start + chunkSize, total);

            StringBuilder msg = new StringBuilder();
            msg.append("Priority Career Links ")
                    .append(start + 1)
                    .append("-")
                    .append(end)
                    .append(" of ")
                    .append(total)
                    .append("\n\n");

            msg.append("These companies are not auto-scrapable yet.\n");
            msg.append("Search: Java, Spring Boot, Backend, SDE, Software Engineer.\n\n");

            for (int i = start; i < end; i++) {
                CompanyInfo c = companies.get(i);

                msg.append(i + 1)
                        .append(". ")
                        .append(safe(c.getName()))
                        .append(" | Tier ")
                        .append(c.getTier())
                        .append("\n");

                msg.append(safe(c.getCareerUrl())).append("\n\n");
            }

            boolean sent = sendWhatsApp(msg.toString());

            if (sent) {
                log.info("Career links WhatsApp batch sent: {}-{} of {}", start + 1, end, total);
            } else {
                log.warn("Career links WhatsApp batch failed: {}-{} of {}", start + 1, end, total);
            }
        }
    }


    public boolean sendTestMessage() {
        String msg = "Test message from Job Seeker Backend.\n"
                + "WhatsApp integration is reachable.\n"
                + "If this message is delivered, Twilio config is working.";

        return sendWhatsApp(msg);
    }

    private boolean sendWhatsApp(String message) {
        if (!twilioEnabled) {
            log.info("WhatsApp disabled. Message would be:\n{}", message);
            return false;
        }

        if (isBlank(accountSid) || isBlank(authToken) || isBlank(from) || isBlank(to)) {
            log.warn("WhatsApp not configured. Missing Twilio accountSid/authToken/from/to.");
            return false;
        }

        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/"
                    + accountSid
                    + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("From", from);
            body.add("To", to);
            body.add("Body", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            boolean success = response.getStatusCode().is2xxSuccessful();

            if (success) {
                log.info("WhatsApp sent successfully");
            } else {
                log.warn("WhatsApp send returned status: {}", response.getStatusCode());
            }

            return success;

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Twilio daily/message limit reached: {}", e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Failed to send WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }

    private String buildJobDetailsUrl(Job job) {
        if (job == null || job.getId() == null) {
            return appBaseUrl;
        }

        String base = appBaseUrl == null || appBaseUrl.isBlank()
                ? "http://localhost:8080"
                : appBaseUrl;

        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        return base + "/job/" + job.getId();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String cleaned = value.trim();

        if (cleaned.length() == 1) {
            return cleaned.toUpperCase();
        }

        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }
}
