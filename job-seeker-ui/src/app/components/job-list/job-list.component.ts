import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { JobCardComponent } from '../job-card/job-card.component';
import { Job } from '../../models/job.model';

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [CommonModule, JobCardComponent],
  templateUrl: './job-list.component.html',
  styleUrl: './job-list.component.scss'
})
export class JobListComponent {
  @Input() jobs: Job[] = [];
  @Input() loading = false;

  visibleCount = 21;

  get visibleJobs(): Job[] {
    return this.jobs.slice(0, this.visibleCount);
  }

  get hasMore(): boolean {
    return this.visibleCount < this.jobs.length;
  }

  loadMore(): void {
    this.visibleCount += 21;
  }

  resetPagination(): void {
    this.visibleCount = 21;
  }
}
