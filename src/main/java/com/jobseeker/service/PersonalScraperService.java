package com.jobseeker.service;

import com.jobseeker.config.AbhiramProfile;
import com.jobseeker.config.CompanyRegistry;
import com.jobseeker.config.CompanyRegistry.CompanyInfo;
import com.jobseeker.model.Job;
import com.jobseeker.repository.JobRepository;
import com.jobseeker.scraper.BaseScraper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonalScraperService {

    private final CompanyRegistry companyRegistry;
    private final List<BaseScraper> scrapers;
    private final JobRepository jobRepository;
    private final ProfileMatchService matchService;
    private final WhatsAppService whatsAppService;
    private final AbhiramProfile profile;
    private static final Set<String> PRIORITY_COMPANIES = Set.of(
            "Postman",
            "Razorpay",
            "Tekion",
            "Glean",
            "Notion",
            "Databricks",
            "Ramp",
            "ServiceNow",
            "Visa",
            "Nexthink",
            "Wise",
            "PhonePe",
            "CRED",
            "Groww",
            "Meesho",
            "Zeta",
            "MindTickle",
            "Observe.AI",
            "InMobi",
            "Druva",
            "Freshworks",
            "BrowserStack",
            "Chargebee",
            "Whatfix",
            "MoEngage",
            "WebEngage",
            "Darwinbox",
            "Keka",
            "Rocketlane",
            "SpotDraft",
            "TrueFoundry",
            "Hasura",
            "LambdaTest",
            "Cashfree",
            "Smallcase",
            "Slice",
            "Navi",
            "Juspay",
            "Pine Labs",
            "CoinSwitch"
    );

    public PersonalScraperService(CompanyRegistry companyRegistry,
                                  List<BaseScraper> scrapers,
                                  JobRepository jobRepository,
                                  ProfileMatchService matchService,
                                  WhatsAppService whatsAppService,
                                  AbhiramProfile profile) {
        this.companyRegistry = companyRegistry;
        this.scrapers = scrapers;
        this.jobRepository = jobRepository;
        this.matchService = matchService;
        this.whatsAppService = whatsAppService;
        this.profile = profile;
    }

    public Map<String, Object> scrapeAndNotify() {
        log.info("🚀 Personal scrape started for {}", profile.getName());

        List<CompanyInfo> companies = companyRegistry.getAllCompanies();

        List<Job> allPersistedJobs = new ArrayList<>();
        List<Job> matchedJobs = new ArrayList<>();
        List<Integer> matchScores = new ArrayList<>();
        Map<String, Object> companyDetails = new LinkedHashMap<>();

        for (CompanyInfo company : companies) {
            try {
                if (!isScrapable(company)) {
                    companyDetails.put(company.getName(), Map.of(
                            "scraped", 0,
                            "matched", 0,
                            "savedOrUpdated", 0,
                            "tier", company.getTier(),
                            "platform", company.getPlatform(),
                            "careerUrl", company.getCareerUrl(),
                            "status", "career_page_only"
                    ));
                    continue;
                }

                List<Job> scrapedJobs = scrapeCompany(company);
                List<Job> persistedJobs = saveWithDedup(scrapedJobs);
                allPersistedJobs.addAll(persistedJobs);

                int matchCount = 0;

                for (Job job : persistedJobs) {
                    int score = matchService.scoreJob(job);
                    job.setMatchScore((double) score);

                    Job updated = jobRepository.save(job);

                    if (score >= 65) {
                        matchedJobs.add(updated);
                        matchScores.add(score);
                        matchCount++;
                    }
                }

                companyDetails.put(company.getName(), Map.of(
                        "scraped", scrapedJobs.size(),
                        "matched", matchCount,
                        "savedOrUpdated", persistedJobs.size(),
                        "tier", company.getTier(),
                        "platform", company.getPlatform(),
                        "careerUrl", company.getCareerUrl(),
                        "status", scrapedJobs.isEmpty() ? "no_jobs_found" : "scraped"
                ));

                if (!scrapedJobs.isEmpty()) {
                    log.info("✅ {} => scraped: {}, matched: {}, saved/updated: {}",
                            company.getName(), scrapedJobs.size(), matchCount, persistedJobs.size());
                }

            } catch (Exception e) {
                log.warn("❌ {} scrape failed: {}", company.getName(), e.getMessage());

                companyDetails.put(company.getName(), Map.of(
                        "scraped", 0,
                        "matched", 0,
                        "savedOrUpdated", 0,
                        "tier", company.getTier(),
                        "platform", company.getPlatform(),
                        "careerUrl", company.getCareerUrl(),
                        "status", "error",
                        "error", e.getMessage()
                ));
            }
        }

        List<Integer> sortedIndices = new ArrayList<>();
        for (int i = 0; i < matchedJobs.size(); i++) {
            sortedIndices.add(i);
        }

        sortedIndices.sort((a, b) -> matchScores.get(b) - matchScores.get(a));

        List<Job> sortedJobs = sortedIndices.stream()
                .map(matchedJobs::get)
                .toList();

        List<Integer> sortedScores = sortedIndices.stream()
                .map(matchScores::get)
                .toList();

        List<Job> whatsappJobs = new ArrayList<>();
        List<Integer> whatsappScores = new ArrayList<>();

        // Send all matched jobs to WhatsApp, not only top 5.
        for (int i = 0; i < sortedJobs.size(); i++) {
            if (sortedScores.get(i) >= 65) {
                whatsappJobs.add(sortedJobs.get(i));
                whatsappScores.add(sortedScores.get(i));
            }
        }

        if (!whatsappJobs.isEmpty()) {
            whatsAppService.sendJobAlert(whatsappJobs, whatsappScores);
        } else {
            log.info("No WhatsApp jobs found with score >= 65");
        }

        whatsAppService.sendDailySummary(
                allPersistedJobs.size(),
                matchedJobs.size(),
                allPersistedJobs.size()
        );

        sendPriorityCareerLinks(companies);

        long scrapableCompanies = companies.stream()
                .filter(this::isScrapable)
                .count();

        long careerPageOnlyCompanies = companies.stream()
                .filter(c -> "CAREERS_PAGE".equals(c.getPlatform()))
                .count();

        return Map.of(
                "message", "Personal scrape completed",
                "totalCompanies", companies.size(),
                "scrapableCompanies", scrapableCompanies,
                "careerPageOnlyCompanies", careerPageOnlyCompanies,
                "totalScraped", allPersistedJobs.size(),
                "matchedJobs", matchedJobs.size(),
                "whatsappJobs", whatsappJobs.size(),
                "companyDetails", companyDetails
        );
    }

    private void sendPriorityCareerLinks(List<CompanyInfo> companies) {
        try {
            List<CompanyInfo> careerOnlyPriorityCompanies = companies.stream()
                    .filter(c -> "CAREERS_PAGE".equals(c.getPlatform()))
                    .filter(c -> c.getTier() <= 3)
                    .limit(12)
                    .toList();

            whatsAppService.sendCareerLinks(careerOnlyPriorityCompanies);
        } catch (Exception e) {
            log.warn("Could not send priority career links: {}", e.getMessage());
        }
    }

    private boolean isScrapable(CompanyInfo company) {
    return "GREENHOUSE".equals(company.getPlatform())
            || "LEVER".equals(company.getPlatform())
            || "ASHBY".equals(company.getPlatform())
            || "WORKABLE".equals(company.getPlatform())
            || "SMARTRECRUITERS".equals(company.getPlatform())
            || "RECRUITEE".equals(company.getPlatform());
}

    private List<Job> scrapeCompany(CompanyInfo company) {
        List<Job> allJobs = new ArrayList<>();

        List<BaseScraper> selectedScrapers = getScrapersBySource(company.getPlatform());

        for (BaseScraper scraper : selectedScrapers) {
            try {
                List<Job> jobs = scraper.scrape(company.getSlug(), null);

                if (jobs != null && !jobs.isEmpty()) {
                    allJobs.addAll(jobs);
                    break;
                }

            } catch (Exception e) {
                log.debug("Scraper {} failed for {}: {}",
                        scraper.getSourceName(), company.getName(), e.getMessage());
            }
        }

        return allJobs;
    }

    private List<BaseScraper> getScrapersBySource(String source) {
        return scrapers.stream()
                .filter(s -> s.getSourceName().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    }

    private List<Job> saveWithDedup(List<Job> jobs) {
        List<Job> persisted = new ArrayList<>();

        if (jobs == null || jobs.isEmpty()) {
            return persisted;
        }

        for (Job job : jobs) {
            if (job.getJobUrl() == null || job.getJobUrl().isBlank()) {
                continue;
            }

            Optional<Job> existing = jobRepository.findByJobUrl(job.getJobUrl());

            if (existing.isEmpty()) {
                Job saved = jobRepository.save(job);
                persisted.add(saved);
            } else {
                Job ex = existing.get();

                ex.setTitle(job.getTitle());
                ex.setCompany(job.getCompany());
                ex.setLocation(job.getLocation());
                ex.setExperienceLevel(job.getExperienceLevel());
                ex.setDescription(job.getDescription());
                ex.setSource(job.getSource());
                ex.setActive(true);
                ex.setScrapedAt(job.getScrapedAt());

                Job updated = jobRepository.save(ex);
                persisted.add(updated);
            }
        }

        return persisted;
    }
    public Map<String, Object> scrapePriorityAndNotify() {
        log.info("🚀 Priority personal scrape started for {}", profile.getName());

        List<CompanyInfo> priorityCompanies = companyRegistry.getAllCompanies().stream()
                .filter(company -> PRIORITY_COMPANIES.contains(company.getName()))
                .filter(this::isScrapable)
                .toList();

        List<Job> allPersistedJobs = new ArrayList<>();
        List<Job> matchedJobs = new ArrayList<>();
        List<Integer> matchScores = new ArrayList<>();
        Map<String, Object> companyDetails = new LinkedHashMap<>();

        for (CompanyInfo company : priorityCompanies) {
            try {
                List<Job> scrapedJobs = scrapeCompany(company);
                List<Job> persistedJobs = saveWithDedup(scrapedJobs);
                allPersistedJobs.addAll(persistedJobs);

                int matchCount = 0;

                for (Job job : persistedJobs) {
                    int score = matchService.scoreJob(job);
                    job.setMatchScore((double) score);

                    Job updated = jobRepository.save(job);

                    if (score >= 65) {
                        matchedJobs.add(updated);
                        matchScores.add(score);
                        matchCount++;
                    }
                }

                companyDetails.put(company.getName(), Map.of(
                        "scraped", scrapedJobs.size(),
                        "matched", matchCount,
                        "savedOrUpdated", persistedJobs.size(),
                        "tier", company.getTier(),
                        "platform", company.getPlatform(),
                        "careerUrl", company.getCareerUrl(),
                        "status", scrapedJobs.isEmpty() ? "no_jobs_found" : "scraped"
                ));

                log.info("✅ PRIORITY {} => scraped: {}, matched: {}, saved/updated: {}",
                        company.getName(), scrapedJobs.size(), matchCount, persistedJobs.size());

            } catch (Exception e) {
                log.warn("❌ PRIORITY {} scrape failed: {}", company.getName(), e.getMessage());

                companyDetails.put(company.getName(), Map.of(
                        "scraped", 0,
                        "matched", 0,
                        "savedOrUpdated", 0,
                        "tier", company.getTier(),
                        "platform", company.getPlatform(),
                        "careerUrl", company.getCareerUrl(),
                        "status", "error",
                        "error", e.getMessage()
                ));
            }
        }

        List<Integer> sortedIndices = new ArrayList<>();

        for (int i = 0; i < matchedJobs.size(); i++) {
            sortedIndices.add(i);
        }

        sortedIndices.sort((a, b) -> matchScores.get(b) - matchScores.get(a));

        List<Job> sortedJobs = sortedIndices.stream()
                .map(matchedJobs::get)
                .toList();

        List<Integer> sortedScores = sortedIndices.stream()
                .map(matchScores::get)
                .toList();

        List<Job> whatsappJobs = new ArrayList<>();
        List<Integer> whatsappScores = new ArrayList<>();

        for (int i = 0; i < sortedJobs.size(); i++) {
            if (sortedScores.get(i) >= 65) {
                whatsappJobs.add(sortedJobs.get(i));
                whatsappScores.add(sortedScores.get(i));
            }
        }

        if (!whatsappJobs.isEmpty()) {
            whatsAppService.sendJobAlert(whatsappJobs, whatsappScores);
        } else {
            log.info("No priority WhatsApp jobs found with score >= 65");
        }

        whatsAppService.sendDailySummary(
                allPersistedJobs.size(),
                matchedJobs.size(),
                allPersistedJobs.size()
        );

        return Map.of(
                "message", "Priority personal scrape completed",
                "priorityCompanies", priorityCompanies.size(),
                "totalScraped", allPersistedJobs.size(),
                "matchedJobs", matchedJobs.size(),
                "whatsappJobs", whatsappJobs.size(),
                "companyDetails", companyDetails
        );
    }

    public List<Job> getMatchedJobs() {
        return jobRepository.findByActiveTrue().stream()
                .filter(j -> j.getMatchScore() != null && j.getMatchScore() >= 65)
                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .collect(Collectors.toList());
    }
}
