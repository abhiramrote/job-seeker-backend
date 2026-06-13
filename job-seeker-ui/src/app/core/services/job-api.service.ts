import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Job {
  id: number;
  title: string;
  company: string;
  location: string;
  matchScore: number;
  source: string;
  jobUrl: string;
}

export interface CompanyResult {
  name?: string;
  scraped: number;
  matched: number;
  savedOrUpdated?: number;
  tier?: number;
  platform: string;
  careerUrl?: string;
  status: string;
  error?: string;
}

export interface ScrapeSummary {
  message: string;
  priorityCompanies?: number;
  totalCompanies?: number;
  scrapableCompanies?: number;
  careerPageOnlyCompanies?: number;
  totalScraped: number;
  matchedJobs: number;
  whatsappJobs: number;
  companyDetails: Record<string, CompanyResult>;
  matchedJobDetails?: Job[];
}

@Injectable({
  providedIn: 'root'
})
export class JobApiService {
  private baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  scrapePriority(): Observable<ScrapeSummary> {
    return this.http.post<ScrapeSummary>(`${this.baseUrl}/scrape-priority`, {});
  }

  scrapeFull(): Observable<ScrapeSummary> {
    return this.http.post<ScrapeSummary>(`${this.baseUrl}/scrape`, {});
  }

  getMatchedJobs(): Observable<Job[]> {
    return this.http.get<Job[]>(`${this.baseUrl}/matched-jobs`);
  }
}
