# Outokumpu Operational Agreements Management

A graduation project for managing operational agreements within Outokumpu's capacity management process. The application consists of a Java 17 / Spring Boot REST API, a React / TypeScript frontend and PostgreSQL.

It supports general agreements, capacity swaps and order relocations; assignments and approvers; read acknowledgements and decisions; process steps; comments; change history; search; and active/completed overviews. Administrators can change existing users' roles and active flags.

## Requirements

- Java 17 and Maven.
- PostgreSQL, with permission to create databases and manage their tables.
- Node.js 22.13 or later in the 22.x series, or Node.js 24 or later, with npm. These versions satisfy the installed Vite and ESLint engine requirements.
- Git.

Clone the repository and open its root directory:

```bash
git clone https://github.com/felixvanmill/TaskManagement.git
cd TaskManagement
```

Run backend commands from this directory so Spring can load the root `.env` file. Run frontend commands from `frontend/`.

## Database setup

Create separate development and test databases:

```sql
CREATE DATABASE afsprakenmanagement;
CREATE DATABASE afsprakenmanagement_test;
```

The development configuration uses Hibernate `ddl-auto=update`. Integration tests use the Spring `test` profile with `ddl-auto=create-drop`: test tables are created and dropped automatically. Keep the test database separate from any data you want to retain.

## Local configuration

Copy `.env.example` to `.env` only if you do not already have local configuration.

PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS/Linux:

```bash
cp .env.example .env
```

Set your local database credentials:

```env
DB_URL=jdbc:postgresql://localhost:5432/afsprakenmanagement
DB_USERNAME=postgres
DB_PASSWORD=your_local_database_password
```

Spring imports this file as Java properties; no separate dotenv tool is required. The test profile uses the same username and password but fixes the database URL to `jdbc:postgresql://localhost:5432/afsprakenmanagement_test`.

### Initial accounts

A new database has no users. Enable the optional administrator bootstrap for the first startup:

```env
BOOTSTRAP_ADMIN_ENABLED=true
BOOTSTRAP_ADMIN_NAME=Initial Admin
BOOTSTRAP_ADMIN_EMAIL=admin@example.com
BOOTSTRAP_ADMIN_PASSWORD=replace_with_a_unique_password
BOOTSTRAP_ADMIN_DEPARTMENT=Capacity Management
```

Passwords must contain at least 12 characters. The initializer creates the department if needed and leaves an existing account with the same email unchanged. After the account is created, set `BOOTSTRAP_ADMIN_ENABLED=false` and restart; the account remains available.

For manual testing with a regular user, the equivalent optional settings are:

```env
BOOTSTRAP_TEST_USER_ENABLED=true
BOOTSTRAP_TEST_USER_NAME=Test User
BOOTSTRAP_TEST_USER_EMAIL=test@example.com
BOOTSTRAP_TEST_USER_PASSWORD=replace_with_another_unique_password
BOOTSTRAP_TEST_USER_DEPARTMENT=Capacity Management
```

Disable `BOOTSTRAP_TEST_USER_ENABLED` after creation. Both initializers are excluded from the `test` profile. Bootstrap settings create missing accounts; they do not reset existing passwords or roles.

### URLs and session settings

| Variable | Purpose | Default |
| --- | --- | --- |
| `FRONTEND_URL` | Allowed frontend origin for credentialed CORS | `http://localhost:5173` |
| `SESSION_TIMEOUT` | Server-side session timeout | `30m` |
| `SESSION_COOKIE_SAME_SITE` | Session cookie SameSite policy | `lax` |
| `SESSION_COOKIE_SECURE` | Restrict the session cookie to HTTPS | `false` |

The frontend defaults to the backend at `http://localhost:8080`. To override it, create `frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Vite reads this value when it starts or builds. Restart the dev server or rebuild after changing it. If Vite selects a different port, make `FRONTEND_URL` match the actual frontend origin and restart the backend.

## Start the application

1. Start PostgreSQL and complete the database/account configuration above.
2. Start the backend from the project root:

   ```bash
   mvn spring-boot:run
   ```

   Alternatively, run `nl.outokumpu.afspraken.AfsprakenApplication` from IntelliJ with the project root as its working directory. The backend defaults to `http://localhost:8080`.

3. In a second terminal, install and start the frontend:

   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

4. Open `http://localhost:5173` (or the URL Vite prints) and log in with a configured account. Keep both servers running.

## Testing and build checks

With PostgreSQL running and the separate test database configured, run from the project root:

```bash
mvn test
```

This runs the existing Java unit and integration tests. Test database settings are in `src/test/resources/application-test.properties`; integration tests activate the test profile.

From `frontend/`, run:

```bash
npm run lint
npm run build
```

The build runs TypeScript project checks and writes a production bundle to `frontend/dist/`. There is currently no frontend automated test script. For a local preview of the built frontend, use `npm run preview`; the backend must still run separately and CORS must allow the preview origin. The frontend bundle is not automatically packaged into the Spring Boot application.

Also check the diff before committing:

```bash
git diff --check
```

## Project structure

```text
frontend/                 React application; see frontend/README.md
src/main/java/            Controllers, services, entities and infrastructure
src/main/resources/       Spring configuration
src/test/java/            Java unit and integration tests
src/test/resources/       Test profile configuration
.env.example              Backend configuration template
pom.xml                   Maven build and dependencies
```

## Authentication and access

Authentication uses a server-side HTTP session and cookies. The frontend includes credentials with API requests, obtains CSRF tokens from `/api/auth/csrf`, and refreshes its token after login establishes a new session. State-changing requests require CSRF protection except login. Roles and domain permissions are enforced on the backend; route guards only control the frontend interface.

Keep `.env` and local overrides out of Git, and do not put real credentials in `.env.example`. Use `SESSION_COOKIE_SECURE=true` for HTTPS deployments and configure CORS and cookie settings for the actual deployment origins. The default URLs and insecure cookie setting are for local development.

## Troubleshooting

- **Database connection failures:** check that PostgreSQL is running, both databases exist, and the configured account can connect and manage their tables.
- **Integration test failures:** check access to `afsprakenmanagement_test`; changing `DB_URL` does not override the test profile's database URL.
- **CORS errors:** make `FRONTEND_URL` exactly match the browser's origin, including the port, and restart the backend.
- **Frontend cannot reach the API:** check the backend process and `VITE_API_BASE_URL`, then restart Vite after changing frontend environment values.
- **Login fails on a fresh database:** configure the administrator bootstrap, start the backend once, and disable the bootstrap again after account creation.
