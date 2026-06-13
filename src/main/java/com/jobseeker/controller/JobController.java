package com.jobseeker.controller;

import com.jobseeker.dto.FilterRequestDTO;
import com.jobseeker.dto.JobResponseDTO;
import com.jobseeker.model.Job;
import com.jobseeker.service.JobFilterService;
import com.jobseeker.service.ScraperService;
import com.opencsv.CSVWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job listing and export endpoints")
public class JobController {

    private final JobFilterService jobFilterService;
    private final ScraperService scraperService;

    @GetMapping
    @Operation(summary = "Get all jobs with optional filters")
    public ResponseEntity<List<JobResponseDTO>> getJobs(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) List<String> companies,
            @RequestParam(required = false) Double minMatchScore) {

        FilterRequestDTO filters = FilterRequestDTO.builder()
                .keywords(keywords)
                .location(location)
                .experienceLevel(experienceLevel)
                .companies(companies)
                .minMatchScore(minMatchScore)
                .build();

        List<JobResponseDTO> jobs = jobFilterService.getFilteredJobs(filters);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single job by ID")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {
        JobResponseDTO job = jobFilterService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/scrape")
    @Operation(summary = "Trigger a manual scrape of all configured portals")
    public ResponseEntity<Map<String, Object>> triggerScrape(
            @RequestBody(required = false) FilterRequestDTO filters) {
        List<Job> newJobs = scraperService.scrapeAllPortals(filters);
        return ResponseEntity.ok(Map.of(
                "message", "Scraping completed",
                "newJobsFound", newJobs.size()
        ));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export jobs to CSV format")
    public void exportToCsv(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            HttpServletResponse response) throws IOException {

        FilterRequestDTO filters = FilterRequestDTO.builder()
                .keywords(keywords)
                .location(location)
                .experienceLevel(experienceLevel)
                .build();

        List<JobResponseDTO> jobs = jobFilterService.getFilteredJobs(filters);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=jobs.csv");

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {

            // Header
            writer.writeNext(new String[]{
                    "ID", "Title", "Company", "Location", "Experience Level",
                    "Job URL", "Source", "Match Score", "Scraped At"
            });

            // Data rows
            for (JobResponseDTO job : jobs) {
                writer.writeNext(new String[]{
                        String.valueOf(job.getId()),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getExperienceLevel(),
                        job.getJobUrl(),
                        job.getSource(),
                        job.getMatchScore() != null ? String.valueOf(job.getMatchScore()) : "",
                        job.getScrapedAt() != null ? job.getScrapedAt().toString() : ""
                });
            }
        }
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export jobs to Excel format")
    public void exportToExcel(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            HttpServletResponse response) throws IOException {

        FilterRequestDTO filters = FilterRequestDTO.builder()
                .keywords(keywords)
                .location(location)
                .experienceLevel(experienceLevel)
                .build();

        List<JobResponseDTO> jobs = jobFilterService.getFilteredJobs(filters);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=jobs.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Jobs");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            String[] headers = {"ID", "Title", "Company", "Location", "Experience Level",
                                "Job URL", "Source", "Match Score", "Scraped At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (JobResponseDTO job : jobs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(job.getId() != null ? job.getId() : 0);
                row.createCell(1).setCellValue(job.getTitle() != null ? job.getTitle() : "");
                row.createCell(2).setCellValue(job.getCompany() != null ? job.getCompany() : "");
                row.createCell(3).setCellValue(job.getLocation() != null ? job.getLocation() : "");
                row.createCell(4).setCellValue(job.getExperienceLevel() != null ? job.getExperienceLevel() : "");
                row.createCell(5).setCellValue(job.getJobUrl() != null ? job.getJobUrl() : "");
                row.createCell(6).setCellValue(job.getSource() != null ? job.getSource() : "");
                row.createCell(7).setCellValue(job.getMatchScore() != null ? job.getMatchScore() : 0);
                row.createCell(8).setCellValue(job.getScrapedAt() != null ? job.getScrapedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}
