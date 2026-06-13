package com.jobseeker.service;

import com.jobseeker.dto.JobResponseDTO;
import com.jobseeker.model.Job;
import com.jobseeker.model.UserProfile;
import com.jobseeker.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JobMatchingService {

    private final WebClient azureOpenAiWebClient;
    private final JobRepository jobRepository;

    @Value("${azure.openai.deployment-name}")
    private String deploymentName;

    @Value("${azure.openai.api-version}")
    private String apiVersion;

    public JobMatchingService(@Qualifier("azureOpenAiWebClient") WebClient azureOpenAiWebClient,
                              JobRepository jobRepository) {
        this.azureOpenAiWebClient = azureOpenAiWebClient;
        this.jobRepository = jobRepository;
    }

    /**
     * Match a user's resume against all active jobs and return ranked results.
     */
    public List<JobResponseDTO> matchResumeToJobs(UserProfile userProfile) {
        List<Job> activeJobs = jobRepository.findByActiveTrue();
        List<JobResponseDTO> matchedJobs = new ArrayList<>();

        for (Job job : activeJobs) {
            try {
                double score = getMatchScore(userProfile.getResumeText(), job);
                job.setMatchScore(score);
                jobRepository.save(job);

                JobResponseDTO dto = JobResponseDTO.fromEntity(job);
                matchedJobs.add(dto);
            } catch (Exception e) {
                log.error("Error matching job {} with resume: {}", job.getId(), e.getMessage());
            }
        }

        // Sort by match score descending
        matchedJobs.sort((a, b) -> Double.compare(
                b.getMatchScore() != null ? b.getMatchScore() : 0,
                a.getMatchScore() != null ? a.getMatchScore() : 0
        ));

        return matchedJobs;
    }

    /**
     * Call Azure OpenAI to get a match score between resume and job.
     */
    private double getMatchScore(String resumeText, Job job) {
        String prompt = String.format(
                "You are a job matching expert. Rate the match between this resume and job posting " +
                "on a scale of 0 to 100. Consider skills, experience level, and job requirements. " +
                "Return ONLY a number between 0 and 100, nothing else.\n\n" +
                "RESUME:\n%s\n\n" +
                "JOB TITLE: %s\n" +
                "COMPANY: %s\n" +
                "LOCATION: %s\n" +
                "DESCRIPTION:\n%s",
                resumeText,
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription() != null ? job.getDescription() : "No description available"
        );

        String requestBody = String.format(
                "{\"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], " +
                "\"max_tokens\": 10, \"temperature\": 0.1}",
                prompt.replace("\\", "\\\\")
                      .replace("\"", "\\\"")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t")
        );

        try {
            String response = azureOpenAiWebClient.post()
                    .uri("/openai/deployments/{deployment}/chat/completions?api-version={version}",
                         deploymentName, apiVersion)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse the score from the response
            // Response contains JSON with choices[0].message.content
            if (response != null) {
                int contentStart = response.indexOf("\"content\":\"") + 11;
                int contentEnd = response.indexOf("\"", contentStart);
                String scoreStr = response.substring(contentStart, contentEnd).trim();
                return Double.parseDouble(scoreStr);
            }
        } catch (Exception e) {
            log.error("Error calling Azure OpenAI for match score: {}", e.getMessage());
        }

        return 0.0;
    }
}
