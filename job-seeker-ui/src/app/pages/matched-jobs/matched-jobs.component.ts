import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { JobApiService, Job } from '../../core/services/job-api.service';

@Component({
  selector: 'app-matched-jobs',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './matched-jobs.component.html',
  styleUrl: './matched-jobs.component.scss'
})
export class MatchedJobsComponent implements OnInit {
  jobs: Job[] = [];
  loading = true;
  error = '';

  constructor(private api: JobApiService) {}

  ngOnInit(): void {
    this.api.getMatchedJobs().subscribe({
      next: (jobs) => {
        this.jobs = jobs;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Failed to load matched jobs';
        this.loading = false;
      }
    });
  }
}
