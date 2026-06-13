# Job Seeker UI

Angular frontend for the Job Seeker Spring Boot backend.

## Run locally in GitHub Codespaces

```bash
cd /workspaces/job-seeker-ui
npm install
npm start
```

Open forwarded port `4200`.

Backend expected at:

```text
http://localhost:8080/api/personal
```

If backend is deployed later, update `src/environments/environment.ts`.

## Important backend endpoints used

```text
POST /api/personal/scrape-priority
POST /api/personal/scrape
GET  /api/personal/matched-jobs
```
