package com.jobseeker.service;

import com.jobseeker.dto.JobResponseDTO;
import com.jobseeker.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    /**
     * Send an email notification to a user with matched job listings.
     */
    public void sendJobMatchEmail(UserProfile user, List<JobResponseDTO> matchedJobs) {
        if (matchedJobs == null || matchedJobs.isEmpty()) {
            log.info("No matched jobs to notify user: {}", user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("🎯 New Job Matches Found For You!");
            message.setText(buildEmailBody(user, matchedJobs));

            mailSender.send(message);
            log.info("Job match email sent to: {} with {} jobs", user.getEmail(), matchedJobs.size());

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Build the email body with job details.
     */
    private String buildEmailBody(UserProfile user, List<JobResponseDTO> jobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(user.getFullName()).append(",\n\n");
        sb.append("We found ").append(jobs.size()).append(" new job matches for you!\n\n");
        sb.append("=".repeat(50)).append("\n\n");

        int rank = 1;
        for (JobResponseDTO job : jobs) {
            sb.append(String.format("#%d - %s\n", rank++, job.getTitle()));
            sb.append(String.format("   Company:  %s\n", job.getCompany()));
            sb.append(String.format("   Location: %s\n", job.getLocation()));
            if (job.getMatchScore() != null) {
                sb.append(String.format("   Match:    %.0f%%\n", job.getMatchScore()));
            }
            sb.append(String.format("   Apply:    %s\n", job.getJobUrl()));
            sb.append("\n");
        }

        sb.append("=".repeat(50)).append("\n");
        sb.append("\nGood luck with your applications!\n");
        sb.append("- Job Seeker Bot\n");

        return sb.toString();
    }
}
