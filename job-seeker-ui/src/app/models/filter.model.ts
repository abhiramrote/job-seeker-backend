export interface FilterRequest {
  keywords?: string;
  location?: string;
  experienceLevel?: string;
  companies?: string[];
  minMatchScore?: number;
}
