package com.jobseeker.controller;

import com.jobseeker.dto.JobResponseDTO;
import com.jobseeker.dto.ResumeUploadDTO;
import com.jobseeker.model.UserProfile;
import com.jobseeker.repository.UserProfileRepository;
import com.jobseeker.service.JobMatchingService;
import com.jobseeker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "Resume upload and job matching endpoints")
public class ResumeController {

    private final UserProfileRepository userProfileRepository;
    private final JobMatchingService jobMatchingService;
    private final NotificationService notificationService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a user resume/profile")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @Valid @RequestBody ResumeUploadDTO dto) {

        // Check if user already exists (update) or create new
        UserProfile userProfile = userProfileRepository.findByEmail(dto.getEmail())
                .map(existing -> {
                    existing.setFullName(dto.getFullName());
                    existing.setSkills(dto.getSkills());
                    existing.setExperienceYears(dto.getExperienceYears());
                    existing.setPreferredLocations(dto.getPreferredLocations());
                    existing.setResumeText(dto.getResumeText());
                    return existing;
                })
                .orElse(UserProfile.builder()
                        .fullName(dto.getFullName())
                        .email(dto.getEmail())
                        .skills(dto.getSkills())
                        .experienceYears(dto.getExperienceYears())
                        .preferredLocations(dto.getPreferredLocations())
                        .resumeText(dto.getResumeText())
                        .build());

        UserProfile saved = userProfileRepository.save(userProfile);

        log.info("User profile saved/updated for: {}", saved.getEmail());

        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded successfully",
                "userId", saved.getId(),
                "email", saved.getEmail()
        ));
    }

    @PostMapping("/match")
    @Operation(summary = "Match resume against active job listings")
    public ResponseEntity<Map<String, Object>> matchResume(
            @RequestParam String email,
            @RequestParam(defaultValue = "false") boolean notify) {

        UserProfile user = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with email: " + email + ". Please upload your resume first."));

        List<JobResponseDTO> matchedJobs = jobMatchingService.matchResumeToJobs(user);

        // Optionally send email notification
        if (notify && !matchedJobs.isEmpty()) {
            notificationService.sendJobMatchEmail(user, matchedJobs);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Matching completed",
                "totalMatches", matchedJobs.size(),
                "matches", matchedJobs
        ));
    }
}
