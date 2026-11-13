# IAM Module Technical Documentation

## Overview

The `iam` bounded context owns user registration, password authentication, JWT access tokens, and email verification. It follows the project’s layered DDD convention: domain contracts and rules are isolated from application orchestration, infrastructure adapters, and REST delivery.

## Package structure and responsibilities

| Package | Responsibility |
| --- | --- |
| `iam.domain.model` | `User` aggregate, verification-token entity, value objects, and domain events. No Spring, JPA, REST, or Brevo types are imported. |
| `iam.domain.repositories`, `iam.domain.services` | Ports for persistence, password hashing, JWTs, and email. |
| `iam.application` | Commands, use-case interfaces/results, and transactional use-case orchestration. |
| `iam.infrastructure` | BCrypt/JJWT/Brevo/JPA/Spring Security adapters and configuration. |
| `iam.interfaces.rest` | HTTP resources, request validation, assemblers, controller, and OpenAPI metadata. |

The `User` aggregate is the IAM aggregate root. `EmailAddress`, `PasswordHash`, `UserId`, and `Role` are value objects. `UserRegisteredEvent` and `UserVerifiedEvent` are domain events. JPA entities and persistence mappers keep database annotations outside the domain.

## Workflows

### Registration

1. `POST /api/v1/authentication/sign-up` validates the request.
2. `SignUpUseCaseImpl` validates a normalized email and the password policy, checks the repository port, hashes the password through BCrypt, and saves an unverified `User`.
3. The user ID assignment emits `UserRegisteredEvent`; `UserRepositoryAdapter` publishes it after persistence.
4. A 256-bit CSPRNG verification token is generated. Its SHA-256 digest—not the raw token—is stored in `email_verification_tokens`.
5. Brevo receives a synchronous transactional-email request. If it cannot accept the request, an exception rolls back the database transaction and the API must not report registration success.

### Login

`POST /api/v1/authentication/sign-in` looks up the normalized email, verifies BCrypt credentials, rejects unverified accounts, and returns a signed HS256 JWT. The token contains the email subject, user ID, display name, roles, issue time, and expiry.

### Email verification

`GET /api/v1/authentication/verify-email?token=...` hashes the submitted token before lookup. A token must exist, be unexpired, and have no `used_at` value. The transaction marks it used and verifies the user. Replays and expired tokens produce an application failure.

## API

| Method and path | Public | Success | Notes |
| --- | --- | --- | --- |
| `POST /api/v1/authentication/sign-up` | Yes | `201 Created` | Password: 8–64 chars, uppercase, lowercase, digit, and `@$!%*?&`. |
| `POST /api/v1/authentication/sign-in` | Yes | `200 OK` | Returns JWT only for verified users. |
| `GET /api/v1/authentication/verify-email?token=` | Yes | `200 OK` | Token is single-use and expires after 24 hours. |
| Other paths | No | — | Require `Authorization: Bearer <jwt>`. |

Registration request:

```json
{
  "email": "jane@example.com",
  "password": "SecurePass123!",
  "firstName": "Jane",
  "lastName": "Doe"
}
```

Login response:

```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "user": { "id": 1, "email": "jane@example.com", "firstName": "Jane", "lastName": "Doe", "roles": ["ROLE_USER"], "emailVerified": true }
}
```

Failures use RFC 9457 `ProblemDetail` JSON. Passwords, JWT secrets, Brevo keys, and verification tokens are never returned.

## Persistence

`users` has a database-enforced unique `email` constraint and email index. `user_roles` stores roles with a foreign key to `users`. `email_verification_tokens` has a unique token-digest column plus token and user-ID indexes; it tracks `expires_at` and `used_at`. The token table currently stores `user_id` as a scalar column and has no physical foreign-key constraint—this should be added through a migration before production deployment.

Sign-up and verification use cases are transactional. Delivery is intentionally synchronous: a Brevo failure makes sign-up fail rather than create an unusable account. This is still not an outbox: if the process/database commit fails after Brevo accepts the HTTP request, an email may exist for an account that did not commit. A transactional outbox plus retry worker is the durable solution when message delivery guarantees are required.

## Environment variables

Use `.env.example` as a template for a local `.env`; Spring Boot itself does not automatically load `.env`. Configure these values as process/IDE environment variables or use an approved dotenv loader outside this repository. `.env` is ignored by Git.

| Variable | Required in production | Purpose / source |
| --- | --- | --- |
| `DB_URL` | Yes | Full JDBC URL, including MySQL host, port, and database/schema. |
| `DB_USERNAME` | Yes | Least-privileged MySQL application user. |
| `DB_PASSWORD` | Yes | Password for that MySQL user; use a secret store. |
| `JWT_SECRET` | Yes | Random secret of at least 32 bytes for HS256. Generate with a password manager or `openssl rand -base64 48`; never reuse it. |
| `JWT_EXPIRATION_DAYS` | Yes | Positive access-token lifetime in days. |
| `BREVO_API_KEY` | Yes | Brevo dashboard → SMTP & API → API Keys. |
| `BREVO_SENDER_EMAIL` | Yes | A sender verified in Brevo. |
| `BREVO_SENDER_NAME` | Yes | Display name for the verified sender. |
| `FRONTEND_BASE_URL` | Yes | Exact public frontend origin; it is also the CORS allow-list entry. |
| `EMAIL_VERIFICATION_URL` | Yes | Public frontend route which accepts `?token=`. |
| `PORT` | No | HTTP port; defaults to `8080`. |
| `BREVO_API_URL` | No | Brevo endpoint; defaults to the official v3 SMTP endpoint. |
| `BREVO_CONNECT_TIMEOUT_SECONDS` | No | Connect timeout; defaults to `5`. |
| `BREVO_READ_TIMEOUT_SECONDS` | No | Response timeout; defaults to `10`. |
| `IAM_VERIFICATION_TOKEN_VALIDITY_HOURS` | No | Verification token validity lifetime in hours; defaults to `24`. |

Production startup deliberately fails if its database, JWT, or Brevo required values are missing. JWT configuration also rejects a secret below 32 bytes or a non-positive expiry.

### IntelliJ IDEA on Windows

Open **Run → Edit Configurations → JouleTrackerBackend**, set **Active profiles** to `dev` or `prod`, and enter semicolon-separated `NAME=value` entries under **Environment variables**. Add `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `JWT_EXPIRATION_DAYS`; add the `BREVO_*` and frontend variables when using email functionality. Enter your MySQL password locally in `DB_PASSWORD`; do not save real credentials to shared run configurations or source files.

### Windows PowerShell

Set variables only for the current shell before launch:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/jouletracker_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8'
$env:DB_USERNAME = 'root'
$securePassword = Read-Host 'MySQL local password' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
$secureJwtSecret = Read-Host 'JWT secret' -AsSecureString
$env:JWT_SECRET = [System.Net.NetworkCredential]::new('', $secureJwtSecret).Password
.\mvnw.cmd spring-boot:run
```

For production, provide every required value using the deployment platform’s secret mechanism rather than persisting secrets in a PowerShell profile.

## Running

The project uses Java 25 and Spring Boot 4.1.1 as declared in `pom.xml`. Start MySQL, set the environment variables, then run:

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean verify
```

## Security considerations and remaining limits

- BCrypt uses work factor 12; only hashes are persisted.
- JWT validation verifies the HS256 signature and expiry. A secret cannot be silently padded.
- CORS accepts only `FRONTEND_BASE_URL`; credentials are allowed only for that exact origin.
- Login returns the same HTTP error for unknown email and wrong password, reducing login enumeration. Registration still returns `409 Conflict` for an existing email, which can disclose account existence; changing that requires an explicit product decision about generic registration responses and resend-verification behavior.
- Tokens are hashed at rest, one-time, and expire after 24 hours. Raw tokens may still appear in browser history, referrer headers, or frontend logs because verification currently uses a GET query parameter. A future POST exchange or frontend fragment flow would reduce that exposure.
- No retry/outbox exists for Brevo. Synchronous delivery prevents false success but cannot provide exactly-once email delivery.
- Add rate limiting, account lockout/backoff, secret rotation, migration-managed DDL, and monitoring before a public production rollout.

## Conceptos de Programación Aplicados

- **Herencia:** `User` hereda de `AbstractDomainAggregateRoot` en `iam/domain/model/aggregates/User.java`; las entidades JPA heredan auditoría en `shared/infrastructure/persistence/jpa/entities/AuditableAbstractPersistenceEntity.java`.
- **Abstracción:** los contratos `UserRepository`, `EmailVerificationTokenRepository`, `PasswordHashingService`, `TokenService` y `EmailService` definen capacidades sin depender de su tecnología.
- **Encapsulamiento:** `User.confirmEmail()`, `User.assignId()` y `EmailVerificationToken.markAsUsed()` protegen las transiciones de estado; los campos no se exponen para mutación directa.

## Conceptos aplicados a la configuración de base de datos

- **Inyección de dependencias:** Spring construye el `DataSource` y lo integra con JPA/Hibernate usando las propiedades configuradas, sin crear conexiones dentro de los servicios del dominio.
- **Separación de responsabilidades:** los perfiles `dev` y `prod` ajustan el comportamiento del entorno, mientras las credenciales se suministran externamente y no se incorporan al código.
- **Configuración externa:** `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` comparten los mismos nombres en los perfiles. Spring Boot no carga `.env` por sí solo; IntelliJ debe recibir estos valores como variables de entorno.
- **Persistencia mediante JPA:** `ddl-auto=validate` comprueba el esquema existente sin crear, actualizar ni eliminar tablas; la estrategia física personalizada se conserva.
- **Polimorfismo:** `BCryptPasswordHashingService`, `JwtTokenService` y `BrevoEmailService` son implementaciones intercambiables de sus interfaces de dominio.
- **Interfaces:** ver `iam/domain/services/*.java` y `iam/domain/repositories/*.java`; los casos de uso también se exponen como interfaces en `iam/application/usecases`.
- **Principios SOLID:** responsabilidad única por capa; inversión de dependencias en los casos de uso (`SignUpUseCaseImpl.java`, `SignInUseCaseImpl.java`, `VerifyEmailUseCaseImpl.java`); extensibilidad mediante adaptadores sin modificar el dominio.
- **Dependency Injection:** Spring inyecta los puertos en los constructores de los casos de uso y adaptadores, por ejemplo `SignUpUseCaseImpl` y `UserRepositoryAdapter`.
- **Repository Pattern:** `UserRepositoryAdapter.java` y `EmailVerificationTokenRepositoryAdapter.java` implementan los repositorios del dominio sobre Spring Data JPA.
- **Adapter Pattern:** BCrypt, JWT, Brevo, JPA y Spring Security se encuentran bajo `iam/infrastructure` y adaptan frameworks a puertos del dominio.
- **Data Mapper / Assembler Pattern:** `UserPersistenceMapper.java`, `EmailVerificationTokenPersistenceMapper.java` y los assemblers REST en `iam/interfaces/rest/transform` convierten entre modelos.
- **Domain-Driven Design:** el paquete `iam` constituye el bounded context; `User` es Aggregate Root y sus repositorios son los límites de persistencia.
- **Value Objects:** `EmailAddress`, `PasswordHash`, `UserId` y `Role` están en `iam/domain/model/valueobjects` y expresan invariantes del dominio.
- **Aggregate Root:** `User.java` controla identidad, contraseña, roles y verificación de la cuenta.
- **Domain Events:** `UserRegisteredEvent.java` y `UserVerifiedEvent.java` se registran desde el agregado y `DomainEventPublisher.java` los publica después de la persistencia.
