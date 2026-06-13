package com.jobseeker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "profile")
public class AbhiramProfile {

    private String name;
    private String phone;        // WhatsApp number: +91XXXXXXXXXX
    private String email;
    private List<String> skills;
    private List<String> targetRoles;
    private List<String> locations;
    private List<String> keywords;
    private int minExperience;
    private int maxExperience;
}
