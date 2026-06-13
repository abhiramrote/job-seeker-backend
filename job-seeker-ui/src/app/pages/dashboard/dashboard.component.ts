import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { JobApiService, ScrapeSummary } from '../../core/services/job-api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  loading = false;
  summary?: ScrapeSummary;
  error = '';

  constructor(private api: JobApiService) {}

  runPriorityScrape() {
    this.loading = true;
    this.error = '';

    this.api.scrapePriority().subscribe({
      next: (res) => {
        this.summary = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Priority scrape failed';
        this.loading = false;
      }
    });
  }

  runFullScrape() {
    this.loading = true;
    this.error = '';

    this.api.scrapeFull().subscribe({
      next: (res) => {
        this.summary = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Full scrape failed';
        this.loading = false;
      }
    });
  }

  get companyRows() {
    if (!this.summary?.companyDetails) return [];

    return Object.entries(this.summary.companyDetails).map(([name, value]) => ({
      name,
      ...value
    }));
  }

  get matchedJobRows() {
    return this.summary?.matchedJobDetails || [];
  }
}
