import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { JobDetailComponent } from './components/job-detail/job-detail.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'job/:id', component: JobDetailComponent },
  { path: '**', redirectTo: '' }
];
