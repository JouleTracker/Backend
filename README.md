# JouleTracker Backend Platform

JouleTracker is an IoT B2C home electricity monitoring backend built with **Spring Boot 4**, **Java 25**, **PostgreSQL (Supabase)**, and strict **Domain-Driven Design (DDD)** / **Clean Hexagonal Architecture**.

---

## Architecture Overview

The backend is organized into strictly isolated Bounded Contexts with clean layer boundaries (`domain`, `application`, `infrastructure`, `interfaces.rest`):

```text
com.jouletracker.platform
├── iam                  # Identity & Access Management Bounded Context
│   ├── domain           # User Aggregate, EmailVerificationToken, PasswordResetToken, Value Objects, Events, Ports
│   ├── application      # Commands, Use Cases (SignUp, SignIn, VerifyEmail, ResendVerification, Forgot/ResetPassword)
│   ├── infrastructure   # PostgreSQL JPA Entities/Adapters, BCrypt, JJWT, Brevo Email Client, Spring Security
│   └── interfaces.rest  # AuthenticationController, OpenAPI DTOs, Assemblers
├── users                # User Management Bounded Context
│   ├── domain           # AccountModerationStatus, ManagedUserSnapshot, ACL Port
│   ├── application      # Commands & Use Cases (ChangePassword, DeactivateUser, UpdateUserStatus)
│   ├── infrastructure   # Anti-Corruption Layer (IamUserAccountAclAdapter)
│   └── interfaces.rest  # UsersController, OpenAPI DTOs, Assemblers
└── shared               # Shared Kernel (Result, ApplicationError, PasswordPolicy, ApplicationProperties, ProblemDetail)
```

---

## REST API Endpoints

Interactive OpenAPI / Swagger UI documentation is available locally at:
- **Swagger UI:** `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8081/v3/api-docs`

### Identity & Access Management (`/api/v1/authentication`)

| Method | Endpoint | Access | Story | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/authentication/sign-up` | Public | TS-01 | Registers a new user and dispatches a verification email via Brevo. |
| `POST` | `/api/v1/authentication/sign-in` | Public | TS-02 | Authenticates active verified users and returns a signed JWT Bearer token. |
| `GET` | `/api/v1/authentication/verify-email?token=...` | Public | US-04 | Confirms a user's email address with a single-use verification token. |
| `POST` | `/api/v1/authentication/resend-verification` | Public | US-04 | Issues a fresh verification token and resends the verification email via Brevo. |
| `POST` | `/api/v1/authentication/forgot-password` | Public | TS-03 | Generates an expiring, single-use password reset token and emails the reset link via Brevo. |
| `POST` | `/api/v1/authentication/reset-password` | Public | TS-03 | Validates the recovery token and hashes the new password using BCrypt. |

### User Management (`/api/v1/users`)

| Method | Endpoint | Access | Story | Description |
| :--- | :--- | :--- | :--- | :--- |
| `PUT` | `/api/v1/users/{id}/password` | Authenticated (Self) | US-25 | Verifies current password via BCrypt, enforces password policy, and updates the password hash. |
| `PATCH` | `/api/v1/users/{id}/deactivate` | Authenticated (Self) | US-40 | Soft-deactivates the user account (`status = INACTIVE`, records `deactivated_at`) and immediately revokes API access. |
| `PATCH` | `/api/v1/users/{id}/status` | `ROLE_ADMIN` Only | US-49 | Moderates a user's status (`ACTIVE`, `SUSPENDED`, `BANNED`, `INACTIVE`). Prevents admin self-ban and immediately blocks restricted accounts. |

---

## Database Persistence: Supabase (PostgreSQL)

This project uses **Supabase PostgreSQL** via the official `org.postgresql:postgresql` JDBC driver and Hibernate's `PostgreSQLDialect`. All connection strings and credentials are strictly externalized via environment variables and are never committed to version control.

An idempotent PostgreSQL reference schema migration script is included at [`src/main/resources/db/migration/V002__supabase_postgresql_schema.sql`](src/main/resources/db/migration/V002__supabase_postgresql_schema.sql).

---

## Local Setup

1. Copy [`.env.example`](.env.example) to a local `.env` file (ignored by `.gitignore`) and fill in your own Supabase, JWT, and Brevo values locally.
2. Start the backend:
   ```bash
   ./mvnw spring-boot:run
   ```
   On Windows PowerShell:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

> **Note:** Spring Boot automatically loads `.env` when present via `spring.config.import=optional:file:.env[.properties]`. `FRONTEND_BASE_URL` is validated on startup—missing or malformed HTTP/HTTPS URLs fail fast.

---

## Deploying to Render

Configure the following environment variables in the Render Dashboard (**Settings -> Environment**). Do **not** upload `.env`.

| Environment Variable | Required | Description |
| :--- | :---: | :--- |
| `SPRING_PROFILES_ACTIVE` | Yes | Set to `prod` |
| `PORT` | Yes | Assigned automatically by Render (or `8080`) |
| `DB_URL` | Yes | Supabase PostgreSQL Session Pooler JDBC URL (`jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require`) |
| `DB_USERNAME` | Yes | Supabase database username (`postgres.<project-ref>`) |
| `DB_PASSWORD` | Yes | Supabase database password (stored as a Render Secret) |
| `JPA_DDL_AUTO` | Optional | `update` for initial schema sync, or `validate` (production default) |
| `JWT_SECRET` | Yes | Cryptographically random secret (>= 32 bytes for HS256) |
| `JWT_EXPIRATION_DAYS` | Optional | Token lifetime in days (default: `7`) |
| `BREVO_API_KEY` | Yes | Brevo transactional email API key |
| `BREVO_SENDER_EMAIL` | Yes | Verified sender address in Brevo |
| `BREVO_SENDER_NAME` | Yes | Display name for outgoing emails |
| `FRONTEND_BASE_URL` | Yes | Deployed frontend base URL (e.g., `https://app.jouletracker.com`) |
| `IAM_VERIFICATION_TOKEN_VALIDITY_HOURS` | Optional | Email verification link TTL in hours (default: `24`) |
| `IAM_PASSWORD_RESET_TOKEN_VALIDITY_MINUTES` | Optional | Password reset link TTL in minutes (default: `30`) |

---

## Running Tests

Automated tests run against an in-memory H2 database configured in **PostgreSQL compatibility mode** (`MODE=PostgreSQL`), requiring no live database credentials:

```bash
./mvnw test
```
On Windows PowerShell:
```powershell
.\mvnw.cmd test
```
