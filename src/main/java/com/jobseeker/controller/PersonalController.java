package com.jobseeker.controller;

import com.jobseeker.config.AbhiramProfile;
import com.jobseeker.config.CompanyRegistry;
import com.jobseeker.dto.JobResponseDTO;
import com.jobseeker.model.Job;
import com.jobseeker.service.PersonalScraperService;
import com.jobseeker.service.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
@Tag(name = "Personal", description = "Abhiram\'s personalized job scraper endpoints")
public class PersonalController {

    private final PersonalScraperService personalScraperService;
    private final WhatsAppService whatsAppService;
    private final AbhiramProfile profile;
    private final CompanyRegistry companyRegistry;

    @PostMapping("/scrape")
    @Operation(summary = "Trigger personal scrape — scrapes all companies and sends WhatsApp alerts")
    public ResponseEntity<Map<String, Object>> triggerScrape() {
        Map<String, Object> result = personalScraperService.scrapeAndNotify();
        return ResponseEntity.ok(result);
    }
    @PostMapping("/scrape-priority")
    public Map<String, Object> scrapePriority() {
        return personalScraperService.scrapePriorityAndNotify();
    }

    @GetMapping("/matched-jobs")
    @Operation(summary = "Get all jobs matching Abhiram\'s profile, sorted by match score")
    public ResponseEntity<List<JobResponseDTO>> getMatchedJobs() {
        List<Job> jobs = personalScraperService.getMatchedJobs();
        List<JobResponseDTO> dtos = jobs.stream()
                .map(JobResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/profile")
    @Operation(summary = "View current profile configuration")
    public ResponseEntity<Map<String, Object>> getProfile() {
        return ResponseEntity.ok(Map.of(
                "name", profile.getName(),
                "email", profile.getEmail(),
                "skills", profile.getSkills(),
                "targetRoles", profile.getTargetRoles(),
                "locations", profile.getLocations(),
                "experience", profile.getMinExperience() + "-" + profile.getMaxExperience() + " years",
                "totalCompanies", companyRegistry.totalCompanies(),
                "tiers", Map.of(
                        "tier1", companyRegistry.getByTier(1).size(),
                        "tier2", companyRegistry.getByTier(2).size(),
                        "tier3", companyRegistry.getByTier(3).size(),
                        "tier4", companyRegistry.getByTier(4).size()
                )
        ));
    }

    @GetMapping("/companies")
    @Operation(summary = "View all tracked companies")
    public ResponseEntity<List<Map<String, Object>>> getCompanies() {
        return ResponseEntity.ok(
            companyRegistry.getAllCompanies().stream()
                .map(c -> Map.<String, Object>of(
                    "name", c.getName(),
                    "slug", c.getSlug(),
                    "platform", c.getPlatform(),
                    "tier", c.getTier()
                ))
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/test-whatsapp")
    @Operation(summary = "Send a test WhatsApp message")
    public ResponseEntity<Map<String, Object>> testWhatsApp() {
        boolean success = whatsAppService.sendTestMessage();
        return ResponseEntity.ok(Map.of(
            "message", success ? "Test message sent!" : "Failed — check Twilio config",
            "success", success,
            "phone", profile.getPhone()
        ));
    }
}
