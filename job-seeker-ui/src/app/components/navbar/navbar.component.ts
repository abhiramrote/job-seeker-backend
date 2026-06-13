import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { JobService } from '../../services/job.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  isScraping = false;

  constructor(
    private jobService: JobService,
    private notify: NotificationService
  ) {}

  scrapeJobs(): void {
    this.isScraping = true;
    this.notify.info('Scraping jobs from all portals...');

    this.jobService.scrapeJobs().subscribe({
      next: (res) => {
        this.isScraping = false;
        this.notify.success(`Scraping complete! ${res.newJobsFound} new jobs found.`);
        window.location.reload();
      },
      error: (err) => {
        this.isScraping = false;
        this.notify.error('Scraping failed. Check the backend logs.');
      }
    });
  }

  exportCsv(): void { this.jobService.exportCsv(); }
  exportExcel(): void { this.jobService.exportExcel(); }
}
