package com.jobseeker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    private String skills;
    private int experienceYears;
    private String preferredLocations;

    @NotBlank(message = "Resume text is required")
    private String resumeText;
}
