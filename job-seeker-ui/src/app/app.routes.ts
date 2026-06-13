import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { MatchedJobsComponent } from './pages/matched-jobs/matched-jobs.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'matched-jobs', component: MatchedJobsComponent },
  { path: '**', redirectTo: '' }
];
