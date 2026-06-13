import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Job } from '../models/job.model';
import { FilterRequest } from '../models/filter.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class JobService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getJobs(filters?: FilterRequest): Observable<Job[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.keywords) params = params.set('keywords', filters.keywords);
      if (filters.location) params = params.set('location', filters.location);
      if (filters.experienceLevel) params = params.set('experienceLevel', filters.experienceLevel);
      if (filters.companies && filters.companies.length > 0) {
        filters.companies.forEach(c => { params = params.append('companies', c); });
      }
      if (filters.minMatchScore) params = params.set('minMatchScore', filters.minMatchScore.toString());
    }
    return this.http.get<Job[]>(`${this.apiUrl}/api/jobs`, { params });
  }

  getJobById(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.apiUrl}/api/jobs/${id}`);
  }

  scrapeJobs(): Observable<any> {
    return this.http.post(`${this.apiUrl}/api/jobs/scrape`, {});
  }

  exportCsv(): void {
    window.open(`${this.apiUrl}/api/jobs/export/csv`, '_blank');
  }

  exportExcel(): void {
    window.open(`${this.apiUrl}/api/jobs/export/excel`, '_blank');
  }
}
