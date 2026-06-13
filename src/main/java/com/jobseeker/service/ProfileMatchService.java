package com.jobseeker.service;

import com.jobseeker.config.AbhiramProfile;
import com.jobseeker.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileMatchService {

    private final AbhiramProfile profile;

    public int scoreJob(Job job) {
        String title = normalize(job.getTitle());
        String desc = normalize(job.getDescription());
        String location = normalize(job.getLocation());
        String combined = title + " " + desc;

        if (!isTargetLocation(location)) return 0;
        if (isClearlyIrrelevant(title, combined)) return 0;
        if (isTooSenior(title, combined)) return 0;
        if (isTestingOnlyRole(title)) return 0;
        if (isMobileOnlyRole(title)) return 0;
        if (isFrontendOnlyRole(title)) return 0;
        if (isConsultingOrGrowthRole(title)) return 0;

        int score = 0;

        score += scoreRoleTitle(title);
        score += scoreLocation(location);
        score += scoreSkills(combined);
        score += scoreExperience(title, combined);

        return Math.min(Math.max(score, 0), 100);
    }

    public boolean isRelevant(Job job) {
        return scoreJob(job) >= 65;
    }

    private boolean isTargetLocation(String location) {
        if (location == null || location.isBlank()) return false;

        List<String> allowed = List.of(
                "india",
                "remote - india",
                "india remote",
                "remote india",
                "hyderabad",
                "bengaluru",
                "bangalore",
                "pune",
                "mumbai",
                "chennai",
                "gurugram",
                "gurgaon",
                "noida",
                "delhi",
                "karnataka",
                "telangana",
                "maharashtra",
                "tamil nadu"
        );

        for (String a : allowed) {
            if (location.contains(a)) return true;
        }

        return false;
    }

    private int scoreLocation(String location) {
        if (location.contains("hyderabad")) return 35;
        if (location.contains("bengaluru") || location.contains("bangalore")) return 35;
        if (location.contains("pune")) return 30;
        if (location.contains("chennai")) return 25;
        if (location.contains("mumbai")) return 22;
        if (location.contains("gurugram") || location.contains("gurgaon") || location.contains("noida") || location.contains("delhi")) return 20;
        if (location.contains("remote - india") || location.contains("remote india") || location.contains("india remote")) return 30;
        if (location.contains("india")) return 25;
        return 0;
    }

    private int scoreRoleTitle(String title) {
        int score = 0;

        if (title.contains("software engineer ii")) score += 48;
        if (title.contains("software engineer i")) score += 45;
        if (title.equals("software engineer")) score += 42;
        if (title.contains("software engineer,")) score += 40;

        if (title.contains("backend engineer")) score += 48;
        if (title.contains("backend developer")) score += 48;
        if (title.contains("backend")) score += 35;

        if (title.contains("sde ii")) score += 45;
        if (title.contains("sde 2")) score += 45;
        if (title.contains("sde i")) score += 42;
        if (title.contains("sde 1")) score += 42;

        if (title.contains("software development engineer ii")) score += 45;
        if (title.contains("software development engineer i")) score += 42;
        if (title.contains("software development engineer")) score += 35;

        if (title.contains("java developer")) score += 45;
        if (title.contains("java engineer")) score += 45;

        if (title.contains("ai engineer")) score += 40;
        if (title.contains("machine learning") && title.contains("software engineer")) score += 35;
        if (title.contains("data & ai")) score += 35;
        if (title.contains("genai")) score += 35;
        if (title.contains("llm")) score += 30;

        if (title.contains("full stack builder")) score += 45;
        if (title.contains("ai builder")) score += 35;

        if (title.contains("full stack")) score += 28;

        return score;
    }

    private int scoreSkills(String combined) {
        int score = 0;

        if (combined.contains("java")) score += 12;
        if (combined.contains("spring boot")) score += 16;
        if (combined.contains("spring")) score += 8;
        if (combined.contains("rest api") || combined.contains("restful")) score += 8;
        if (combined.contains("microservice")) score += 8;
        if (combined.contains("jpa") || combined.contains("hibernate")) score += 6;
        if (combined.contains("sql") || combined.contains("mysql") || combined.contains("postgres")) score += 5;

        if (combined.contains("python")) score += 5;
        if (combined.contains("docker")) score += 4;
        if (combined.contains("kafka")) score += 4;
        if (combined.contains("redis")) score += 4;

        if (combined.contains("llm")) score += 8;
        if (combined.contains("rag")) score += 8;
        if (combined.contains("genai")) score += 8;
        if (combined.contains("openai")) score += 6;
        if (combined.contains("vector database")) score += 6;
        if (combined.contains("semantic search")) score += 6;

        return Math.min(score, 35);
    }

    private int scoreExperience(String title, String combined) {
        int score = 0;

        if (combined.contains("1+ years")) score += 15;
        if (combined.contains("2+ years")) score += 15;
        if (combined.contains("1-3 years")) score += 20;
        if (combined.contains("1 to 3 years")) score += 20;
        if (combined.contains("2 to 4 years")) score += 12;
        if (combined.contains("0-3 years")) score += 15;
        if (combined.contains("early career")) score += 15;

        if (title.contains("associate")) score += 8;
        if (title.contains("junior")) score += 12;

        return score;
    }

    private boolean isClearlyIrrelevant(String title, String combined) {
        List<String> badTitleKeywords = List.of(
                "manager",
                "director",
                "sales",
                "finance",
                "pricing",
                "fraud investigator",
                "operations",
                "customer success",
                "account executive",
                "business development",
                "marketing",
                "recruiter",
                "hr",
                "legal",
                "counsel",
                "analyst",
                "business analyst",
                "data analyst",
                "financial analyst",
                "operations analyst",
                "product manager",
                "program manager",
                "project manager",
                "partnerships",
                "collections",
                "payroll",
                "benefits",
                "telecalling",
                "youtube",
                "videographer",
                "thumbnail",
                "public relations",
                "technical support engineer",
                "technical services engineer",
                "tech solution",
                "partner engineer",
                "partners"
        );

        for (String bad : badTitleKeywords) {
            if (title.contains(bad)) return true;
        }

        return false;
    }

    private boolean isTooSenior(String title, String combined) {
    List<String> seniorTitleKeywords = List.of(
            "staff",
            "senior staff",
            "staff backend engineer",
            "staff software engineer",
            "staff software development engineer",
            "principal",
            "principal engineer",
            "principal software engineer",
            "distinguished engineer",
            "architect",
            "engineering manager",
            "senior manager",
            "manager",
            "director",
            "lead software engineer",
            "lead engineer",
            "tech lead",
            "sde iii",
            "sde iv",
            "sde 3",
            "sde 4",
            "sr sde",
            "sr sde backend",
            "engineer iii",
            "engineer iv",
            "software development engineer iii",
            "software development engineer iv",
            "senior software engineer",
            "senior backend engineer",
            "senior backend software engineer",
            "senior ai engineer",
            "senior full stack",
            "senior engineer"
    );

    for (String keyword : seniorTitleKeywords) {
        if (title.contains(keyword)) {
            return true;
        }
    }

    List<String> irrelevantTitleKeywords = List.of(
            "sap",
            "abap",
            "sap s/4 hana",
            "data scientist",
            "research engineer",
            "analytics",
            "automation analytics",
            "technical support",
            "technical services",
            "solution engineer",
            "solutions engineer",
            "sales engineer"
    );

    for (String keyword : irrelevantTitleKeywords) {
        if (title.contains(keyword)) {
            return true;
        }
    }

    List<String> seniorExperienceKeywords = List.of(
            "7+ years",
            "8+ years",
            "9+ years",
            "10+ years",
            "11 years",
            "12 years",
            "minimum 7 years",
            "minimum 8 years",
            "minimum 10 years"
    );

    for (String keyword : seniorExperienceKeywords) {
        if (combined.contains(keyword)) {
            return true;
        }
    }

    return false;
}

    private boolean isTestingOnlyRole(String title) {
        return title.contains("test")
                || title.contains("sdet")
                || title.contains("qa")
                || title.contains("quality assurance")
                || title.contains("automation engineer")
                || title.contains("software development engineer in test");
    }

    private boolean isMobileOnlyRole(String title) {
        return title.contains("android")
                || title.contains("ios")
                || title.contains("mobile")
                || title.contains("react native");
    }

    private boolean isFrontendOnlyRole(String title) {
        return title.contains("frontend")
                || title.contains("front-end")
                || title.contains("ui engineer")
                || title.contains("ux engineer");
    }

    private boolean isConsultingOrGrowthRole(String title) {
        return title.contains("consultant")
                || title.contains("technical consultant")
                || title.contains("solution engineer")
                || title.contains("solutions engineer")
                || title.contains("technical solution")
                || title.contains("backline technical")
                || title.contains("growth")
                || title.contains("martech")
                || title.contains("intern");
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
