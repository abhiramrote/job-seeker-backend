import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../components/search-bar/search-bar.component';
import { StatsBarComponent } from '../../components/stats-bar/stats-bar.component';
import { JobListComponent } from '../../components/job-list/job-list.component';
import { JobService } from '../../services/job.service';
import { NotificationService } from '../../services/notification.service';
import { Job } from '../../models/job.model';
import { FilterRequest } from '../../models/filter.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, SearchBarComponent, StatsBarComponent, JobListComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements OnInit {
  allJobs: Job[] = [];
  filteredJobs: Job[] = [];
  loading = true;

  @ViewChild(JobListComponent) jobList!: JobListComponent;

  constructor(
    private jobService: JobService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(filters?: FilterRequest): void {
    this.loading = true;
    this.jobService.getJobs(filters).subscribe({
      next: (jobs) => {
        if (!filters || Object.keys(filters).length === 0) {
          this.allJobs = jobs;
        }
        this.filteredJobs = jobs;
        this.loading = false;
        if (this.jobList) this.jobList.resetPagination();
      },
      error: (err) => {
        this.loading = false;
        this.notify.error('Failed to load jobs.');
      }
    });
  }

  onFilterChanged(filters: FilterRequest): void {
    this.loadJobs(filters);
  }
}
