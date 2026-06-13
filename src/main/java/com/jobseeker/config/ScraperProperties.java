package com.jobseeker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@Component
@ConfigurationProperties(prefix = "scraper")
public class ScraperProperties {

    private Map<String, List<String>> targetCompanies = new HashMap<>();

    public List<String> getCompaniesForSource(String source) {
        return targetCompanies.getOrDefault(source.toLowerCase(), new ArrayList<>());
    }
}
