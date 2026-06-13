import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Job } from '../../models/job.model';

@Component({
  selector: 'app-stats-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stats-bar.component.html',
  styleUrl: './stats-bar.component.scss'
})
export class StatsBarComponent {
  @Input() jobs: Job[] = [];

  get totalJobs(): number { return this.jobs.length; }

  get uniqueCompanies(): number {
    return new Set(this.jobs.map(j => j.company)).size;
  }

  get sources(): string[] {
    return [...new Set(this.jobs.map(j => j.source))];
  }

  get seniorJobs(): number {
    return this.jobs.filter(j => j.experienceLevel === 'Senior').length;
  }
}
