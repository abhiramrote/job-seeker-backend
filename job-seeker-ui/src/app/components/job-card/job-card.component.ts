import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Job } from '../../models/job.model';

@Component({
  selector: 'app-job-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './job-card.component.html',
  styleUrl: './job-card.component.scss'
})
export class JobCardComponent {
  @Input() job!: Job;
  @Input() index = 0;

  get sourceClass(): string {
    return this.job.source?.toLowerCase() || '';
  }

  get levelClass(): string {
    return this.job.experienceLevel?.toLowerCase().replace(/\s+/g, '-') || '';
  }

  get timeAgo(): string {
    if (!this.job.scrapedAt) return '';
    const now = new Date();
    const scraped = new Date(this.job.scrapedAt);
    const diffMs = now.getTime() - scraped.getTime();
    const diffHrs = Math.floor(diffMs / (1000 * 60 * 60));
    if (diffHrs < 1) return 'Just now';
    if (diffHrs < 24) return `${diffHrs}h ago`;
    const diffDays = Math.floor(diffHrs / 24);
    if (diffDays === 1) return '1 day ago';
    if (diffDays < 30) return `${diffDays} days ago`;
    return `${Math.floor(diffDays / 30)}mo ago`;
  }

  get companyInitial(): string {
    return this.job.company?.charAt(0).toUpperCase() || '?';
  }
}
