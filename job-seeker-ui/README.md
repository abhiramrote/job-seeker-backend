# 🎨 JobSeeker UI — Angular Frontend

Modern Angular 17 frontend for the Job Seeker Backend.

## 🚀 Quick Start

### Prerequisites
- Node.js 18+ and npm

### Setup
```bash
cd job-seeker-ui
npm install
npm start
```

App runs at `http://localhost:4200` and proxies API calls to `http://localhost:8080`.

### Build for Production
```bash
npm run build:prod
```

Copy `dist/job-seeker-ui/browser/` contents to Spring Boot's `src/main/resources/static/` to serve from the backend.

## 🏗️ Architecture

- **Angular 17** with standalone components
- **Dark theme** with glassmorphism design
- **Responsive** — mobile-first grid layout
- **Debounced search** — 400ms delay
- **Lazy pagination** — loads 21 jobs at a time
- **Toast notifications** — success/error/info
- **Proxy config** — routes `/api/*` to backend

## 📁 Components

| Component | Purpose |
|---|---|
| `NavbarComponent` | Logo, scrape button, export buttons |
| `SearchBarComponent` | Keyword, location, level, company filters |
| `StatsBarComponent` | Total jobs, companies, sources, senior roles |
| `JobCardComponent` | Individual job card with badges |
| `JobListComponent` | Grid layout with pagination + loading skeleton |
| `JobDetailComponent` | Full job description page |
| `ToastComponent` | Notification popups |
| `HomeComponent` | Orchestrates all components |
