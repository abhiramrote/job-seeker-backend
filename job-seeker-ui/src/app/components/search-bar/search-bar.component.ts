import { Component, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { FilterRequest } from '../../models/filter.model';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent implements OnInit, OnDestroy {
  @Output() filterChanged = new EventEmitter<FilterRequest>();

  keywords = '';
  location = '';
  experienceLevel = '';
  company = '';

  experienceLevels = ['', 'Intern', 'Junior', 'Mid', 'Senior', 'Staff', 'Lead', 'Manager', 'Principal'];

  private searchSubject = new Subject<void>();

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => this.emitFilters());
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  onInputChange(): void {
    this.searchSubject.next();
  }

  emitFilters(): void {
    const filters: FilterRequest = {};
    if (this.keywords.trim()) filters.keywords = this.keywords.trim();
    if (this.location.trim()) filters.location = this.location.trim();
    if (this.experienceLevel) filters.experienceLevel = this.experienceLevel;
    if (this.company.trim()) filters.companies = [this.company.trim()];
    this.filterChanged.emit(filters);
  }

  clearFilters(): void {
    this.keywords = '';
    this.location = '';
    this.experienceLevel = '';
    this.company = '';
    this.filterChanged.emit({});
  }
}
