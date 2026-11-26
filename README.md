# JouleTracker Backend Platform

JouleTracker is an IoT energy monitoring backend built with **Spring Boot 4**, **Java 21 LTS**, **PostgreSQL (Supabase)**, and **Domain-Driven Design (DDD)** / **Hexagonal Architecture**.

---

## Bounded Contexts

```text
com.jouletracker.platform
├── iam              # Identity & Access Management (TS-01, TS-02, TS-03, US-04)
├── users            # User Management & Moderation (US-25, US-40, US-49)
├── devices          # Devices, Sensors, and Appliance Profiles
├── energy           # Energy Telemetry & Consumption Monitoring (TS-07, TS-10)
├── businessenergy   # Business Areas & MYPE Energy Dashboards (TS-17)
├── alerts           # Threshold Evaluation & Consumption Alerts (TS-14)
└── shared           # Shared Kernel (Result, ApplicationError, PasswordPolicy, i18n, ProblemDetail)
```

---

## Database & Environment Configuration

All database, JWT, frontend, and Brevo credentials are loaded exclusively from environment variables (or a local `.env` file via `spring.config.import=optional:file:.env[.properties]`). Never commit `.env` or real secrets to Git.

1. Copy [`.env.example`](.env.example) to `.env` (ignored by `.gitignore`).
2. Configure the required variables in `.env`:

| Variable | Description | Example Placeholder |
| :--- | :--- | :--- |
| `DB_URL` | Supabase PostgreSQL Session Pooler JDBC URL | `jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require` |
| `DB_USERNAME` | Supabase PostgreSQL username | `postgres.your_supabase_project_ref` |
| `DB_PASSWORD` | Supabase PostgreSQL password | `change_me_supabase_db_password` |
| `JWT_SECRET` | HS256 signing secret (min. 32 chars) | `change_me_super_secret_jwt_key_at_least_32_chars_long` |
| `FRONTEND_BASE_URL` | Base URL of the Angular frontend | `http://localhost:4200` |
| `BREVO_API_KEY` | Brevo v3 transactional email API key | `xkeysib-change_me_brevo_api_key` |
| `BREVO_SENDER_EMAIL` | Verified sender email in Brevo | `no-reply@jouletracker.com` |
| `BREVO_SENDER_NAME` | Sender display name | `JouleTracker Platform` |

---

## Running Locally & Testing

Start the backend (Windows PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Run the full unit and integration test suite (uses H2 in PostgreSQL compatibility mode):

```powershell
.\mvnw.cmd test
```

Swagger UI is available locally at `http://localhost:8081/swagger-ui.html` (or `${PORT}`).
