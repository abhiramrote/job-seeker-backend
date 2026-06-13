# 🔍 Job Seeker Backend

A Spring Boot backend service that **automatically scrapes company career portals** and helps job seekers find relevant positions based on their experience, skills, and preferences.

## 🌟 Features

- **Multi-portal scraping** — Greenhouse, Lever, Workday (extensible)
- **Smart filtering** — By keywords, location, experience level, company
- **AI-powered matching** — Uses Azure OpenAI (GPT-4o) to match resumes against job descriptions
- **Export support** — CSV and Excel exports of job listings
- **Email notifications** — Get notified when matching jobs are found
- **Scheduled scraping** — Automatically scrapes every 6 hours
- **Deduplication** — Avoids storing duplicate job listings
- **Swagger UI** — Interactive API documentation

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3.2.x |
| Language | Java 17 |
| Database | PostgreSQL |
| Scraping | Jsoup + Selenium |
| AI Matching | Azure OpenAI (GPT-4o) |
| Export | Apache POI (Excel) + OpenCSV |
| Email | Spring Mail |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |

## 📋 Prerequisites

- **Java 17+** installed
- **Maven 3.8+** installed
- **PostgreSQL** running locally (or update `application.yml` with your DB URL)
- **Azure OpenAI** API key (for AI matching feature)

## 🚀 Quick Start

### 1. Clone and configure

```bash
cd job-seeker-backend
```

Edit `src/main/resources/application.yml`:
- Set your **PostgreSQL** credentials
- Set your **Azure OpenAI** API key and endpoint
- Set your **email** credentials (for notifications)
- Add **target companies** to scrape

### 2. Create the database

```sql
CREATE DATABASE jobseeker_db;
```

### 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### 4. Access Swagger UI

Open your browser and navigate to:
```
http://localhost:8080/swagger-ui.html
```

## 📡 API Endpoints

### Jobs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/jobs` | List jobs with optional filters |
| `GET` | `/api/jobs/{id}` | Get a specific job |
| `POST` | `/api/jobs/scrape` | Trigger manual scraping |
| `GET` | `/api/jobs/export/csv` | Export jobs as CSV |
| `GET` | `/api/jobs/export/excel` | Export jobs as Excel |

**Query Parameters for `GET /api/jobs`:**
- `keywords` — Search in title and description
- `location` — Filter by location
- `experienceLevel` — Filter by experience (e.g., Junior, Senior)
- `companies` — Filter by company name(s)
- `minMatchScore` — Minimum AI match score (0-100)

### Resume & Matching

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/resume/upload` | Upload user profile/resume |
| `POST` | `/api/resume/match` | Match resume against jobs |

**Example: Upload Resume**
```json
POST /api/resume/upload
{
    "fullName": "Abhiram Rote",
    "email": "abhiram@example.com",
    "skills": "Java, Spring Boot, Python, React, AWS",
    "experienceYears": 2,
    "preferredLocations": "Hyderabad, Bangalore, Remote",
    "resumeText": "Experienced software developer with 2 years..."
}
```

**Example: Match Resume**
```
POST /api/resume/match?email=abhiram@example.com&notify=true
```

## 🏗️ Adding New Scrapers

1. Create a new class in `com.jobseeker.scraper` extending `BaseScraper`
2. Implement `scrape()` and `getSourceName()` methods
3. Annotate with `@Component`
4. Add target companies in `application.yml` under `scraper.target-companies`

```java
@Component
public class MyNewScraper extends BaseScraper {
    
    @Override
    public String getSourceName() {
        return "MY_PORTAL";
    }
    
    @Override
    public List<Job> scrape(String companyId, FilterRequestDTO filters) {
        // Your scraping logic here
    }
}
```

## 📁 Project Structure

```
src/main/java/com/jobseeker/
├── JobSeekerApplication.java      # Main entry point
├── config/                         # Configuration classes
├── controller/                     # REST API controllers
├── dto/                            # Data Transfer Objects
├── model/                          # JPA Entities
├── repository/                     # Database repositories
├── scheduler/                      # Scheduled tasks
├── scraper/                        # Portal-specific scrapers
└── service/                        # Business logic services
```

## 📄 License

This project is for educational and personal use.

---

Built with ❤️ to help job seekers find their next opportunity!
