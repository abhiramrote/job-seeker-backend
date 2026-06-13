export interface Job {
  id: number;
  title: string;
  company: string;
  location: string;
  experienceLevel: string;
  jobUrl: string;
  description: string;
  source: string;
  matchScore: number | null;
  scrapedAt: string;
  active: boolean;
}
