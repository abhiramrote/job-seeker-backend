# 📱 Abhiram's Personal Job Scraper — Setup Guide

## 🎯 What This Does

Automatically scrapes **70+ product companies** across 4 tiers, filters jobs matching
your profile (Java Backend, Spring Boot, GenAI/RAG, 2 yrs exp), and sends you
**WhatsApp alerts** with the best matches!

## 📋 Your Company Tiers

| Tier | Focus | Companies |
|------|-------|-----------|
| **1** | Top Product (₹12-25+ LPA) | Google, Microsoft, Atlassian, Uber, Stripe, Datadog... (20) |
| **2** | Finance/Enterprise | PayPal, Walmart, JPMorgan, Goldman Sachs, Visa... (20) |
| **3** | Indian Products (Best Fit!) | Razorpay, PhonePe, Juspay, Groww, CRED, Swiggy... (20) |
| **4** | AI/SaaS (GenAI match) | Glean, Cohere, Pinecone, Elastic, MongoDB... (10) |

## 🚀 Quick Start

### 1. Copy Files
Copy all files from this zip to your `job-seeker-backend/` project.

### 2. Rebuild & Run
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

### 3. Trigger Your First Personal Scrape
```bash
curl -X POST http://localhost:8080/api/personal/scrape
```

### 4. View Matched Jobs
```bash
curl http://localhost:8080/api/personal/matched-jobs | python3 -m json.tool | head -100
```

## 📱 WhatsApp Setup (Twilio — FREE)

### Step 1: Create Twilio Account
1. Go to https://www.twilio.com/try-twilio
2. Sign up (free trial, no credit card needed)
3. Copy your **Account SID** and **Auth Token** from the dashboard

### Step 2: Activate WhatsApp Sandbox
1. Go to **Messaging → Try it out → Send a WhatsApp message**
2. Send this from YOUR WhatsApp to the number shown:
   ```
   join <your-sandbox-word>
   ```
3. You'll get a confirmation message ✅

### Step 3: Update application.yml
```yaml
twilio:
  enabled: true
  account-sid: ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
  auth-token: your_auth_token_here
  whatsapp-from: "whatsapp:+14155238886"
```

### Step 4: Test It!
```bash
curl -X POST http://localhost:8080/api/personal/test-whatsapp
```

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/personal/scrape` | Scrape all 70 companies + WhatsApp alert |
| `GET` | `/api/personal/matched-jobs` | Get jobs matching your profile |
| `GET` | `/api/personal/profile` | View your profile config |
| `GET` | `/api/personal/companies` | View all tracked companies |
| `POST` | `/api/personal/test-whatsapp` | Send a test WhatsApp message |

## 🎛️ Customization

### Add More Companies
Edit `CompanyRegistry.java` and add:
```java
add("NewCompany", "slug-name", "GREENHOUSE", 3);
```

### Update Your Profile
Edit `application.yml` under the `profile:` section.

### Change Scrape Frequency
Edit `PersonalJobScheduler.java`:
```java
@Scheduled(cron = "0 0 */3 * * *")  // Every 3 hours
```

## ⏰ Auto Schedule
The scraper runs automatically at: **12:00 AM, 6:00 AM, 12:00 PM, 6:00 PM**