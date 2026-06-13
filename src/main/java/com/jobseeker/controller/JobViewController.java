package com.jobseeker.controller;

import com.jobseeker.model.Job;
import com.jobseeker.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class JobViewController {

    private final JobRepository jobRepository;

    @GetMapping(value = "/job/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewJob(@PathVariable Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background: #0f172a;
                            color: #e5e7eb;
                            line-height: 1.6;
                        }
                        .container {
                            max-width: 900px;
                            margin: 0 auto;
                            padding: 24px;
                        }
                        .card {
                            background: #1e293b;
                            border: 1px solid #334155;
                            border-radius: 16px;
                            padding: 28px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.35);
                        }
                        h1 {
                            margin: 0 0 10px;
                            color: #ffffff;
                            font-size: 28px;
                        }
                        h2 {
                            color: #ffffff;
                            margin-top: 28px;
                        }
                        .company {
                            color: #60a5fa;
                            font-size: 18px;
                            margin-bottom: 18px;
                            text-transform: capitalize;
                        }
                        .meta {
                            display: flex;
                            flex-wrap: wrap;
                            gap: 10px;
                            margin: 18px 0;
                        }
                        .badge {
                            background: #0f172a;
                            border: 1px solid #334155;
                            color: #cbd5e1;
                            padding: 8px 12px;
                            border-radius: 999px;
                            font-size: 14px;
                        }
                        .score {
                            color: #10b981;
                            border-color: #10b981;
                        }
                        .apply {
                            display: inline-block;
                            margin: 22px 0;
                            background: #3b82f6;
                            color: white;
                            padding: 12px 22px;
                            border-radius: 10px;
                            text-decoration: none;
                            font-weight: bold;
                        }
                        .apply:hover {
                            background: #2563eb;
                        }
                        .desc {
                            white-space: pre-wrap;
                            color: #d1d5db;
                            background: #0f172a;
                            border-radius: 12px;
                            padding: 18px;
                            margin-top: 18px;
                        }
                        .back {
                            color: #93c5fd;
                            text-decoration: none;
                            display: inline-block;
                            margin-bottom: 16px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <a class="back" href="/api/personal/matched-jobs">← Back to matched jobs JSON</a>

                        <div class="card">
                            <h1>%s</h1>
                            <div class="company">%s</div>

                            <div class="meta">
                                <span class="badge">📍 %s</span>
                                <span class="badge">🏷️ %s</span>
                                <span class="badge">🔌 %s</span>
                                <span class="badge score">🎯 %s%% match</span>
                                <span class="badge">🆔 Job ID: %s</span>
                            </div>

                            <a class="apply" href="%s" target="_blank" rel="noopener">🚀 Apply Now</a>

                            <h2>Job Description</h2>
                            <div class="desc">%s</div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                escape(job.getTitle()),
                escape(job.getTitle()),
                escape(job.getCompany()),
                escape(job.getLocation()),
                escape(job.getExperienceLevel()),
                escape(job.getSource()),
                job.getMatchScore() == null ? "0" : String.valueOf(job.getMatchScore().intValue()),
                job.getId(),
                escape(job.getJobUrl()),
                escape(job.getDescription())
        );

        return ResponseEntity.ok(html);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
