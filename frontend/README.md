# Outokumpu Operational Agreements Management

A web application for centrally managing and following up operational agreements within Outokumpu's capacity management process.

The application consists of:

- a Spring Boot REST API;
- a React + TypeScript frontend;
- a PostgreSQL database;
- session-based authentication with CSRF protection.

## Main Features

The application supports:

- creating operational agreements;
- multiple agreement types:
  - general operational agreement;
  - capacity swap;
  - order relocation;
- assigning responsible users and involved departments;
- managing approvers;
- recording read confirmations and approval decisions;
- workflow and process-step management;
- agreement status management;
- comments and feedback;
- change history;
- searching, filtering and archiving agreements;
- administrator user management.

## Technology Stack

### Backend

- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Maven

### Frontend

- React
- TypeScript
- Vite
- React Router
- npm

## Prerequisites

Install the following software before starting the application:

- Git
- Java 17
- Maven
- PostgreSQL
- Node.js with npm

A current Node.js LTS release is recommended.

You can verify the installations with:

```bash
git --version
java -version
mvn -version
psql --version
node --version
npm --version
```

## 1. Clone the Repository

Clone the repository and open the project directory:

```bash
git clone https://github.com/felixvanmill/TaskManagement.git
cd TaskManagement
```

All backend commands in this README are executed from the project root unless stated otherwise.

## 2. Create the PostgreSQL Databases

Two local PostgreSQL databases are used:

- `afsprakenmanagement` for normal application use;
- `afsprakenmanagement_test` for automated backend tests.

Create both databases in PostgreSQL:

```sql
CREATE DATABASE afsprakenmanagement;
CREATE DATABASE afsprakenmanagement_test;
```

The PostgreSQL user configured in `.env` must have sufficient permissions to connect to both databases and create, update and remove tables.

The normal application uses Hibernate with:

```text
ddl-auto=update
```

This means the application creates or updates the required tables automatically when the backend starts.

The test profile uses:

```text
ddl-auto=create-drop
```

The test schema is therefore created and removed automatically during the test run.

## 3. Configure the Backend Environment

The backend reads local configuration from a `.env` file in the project root.

Copy:

```text
.env.example
```

to:

```text
.env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

On macOS or Linux:

```bash
cp .env.example .env
```

Update at least the PostgreSQL credentials:

```env
DB_URL=jdbc:postgresql://localhost:5432/afsprakenmanagement
DB_USERNAME=postgres
DB_PASSWORD=your_postgresql_password
```

The `.env` file is ignored by Git and must not be committed.

### Default Local URLs

The default local setup is:

```text
Backend:  http://localhost:8080
Frontend: http://localhost:5173
```

The backend allows requests from the URL configured with:

```env
FRONTEND_URL=http://localhost:5173
```

If Vite starts the frontend on another port, update `FRONTEND_URL` in the root `.env` file and restart the backend.

## 4. Create the Initial Administrator

A new database does not contain any users. The application therefore includes an optional administrator bootstrap.

For the first local startup, configure the following values in `.env`:

```env
BOOTSTRAP_ADMIN_ENABLED=true
BOOTSTRAP_ADMIN_NAME=Initial Admin
BOOTSTRAP_ADMIN_EMAIL=admin@example.com
BOOTSTRAP_ADMIN_PASSWORD=change_this_password
BOOTSTRAP_ADMIN_DEPARTMENT=Capacity Management
```

The bootstrap password must contain at least 12 characters.

When the backend starts, the administrator is created only if a user with the configured email address does not already exist.

After the administrator has been created successfully, it is recommended to change:

```env
BOOTSTRAP_ADMIN_ENABLED=false
```

The existing administrator remains in the database.

Do not store real passwords in `.env.example` or commit passwords to Git.

## 5. Optional Test User

A second normal user can optionally be created for manual testing of workflows, assignments and approvals.

Configure:

```env
BOOTSTRAP_TEST_USER_ENABLED=true
BOOTSTRAP_TEST_USER_NAME=Test User
BOOTSTRAP_TEST_USER_EMAIL=test@example.com
BOOTSTRAP_TEST_USER_PASSWORD=change_this_password
BOOTSTRAP_TEST_USER_DEPARTMENT=Capacity Management
```

The password must contain at least 12 characters.

After the user has been created, the bootstrap can be disabled again:

```env
BOOTSTRAP_TEST_USER_ENABLED=false
```

This bootstrap is optional and is not required for normal application startup.

## 6. Start the Backend

From the project root, run:

```bash
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

Alternatively, the backend can be started from an IDE by running the `AfsprakenApplication` class.

Keep this process running while using the frontend.

## 7. Install the Frontend Dependencies

Open a second terminal and move into the frontend directory:

```bash
cd frontend
```

Install the dependencies from `package-lock.json`:

```bash
npm ci
```

## 8. Start the Frontend

From the `frontend` directory, run:

```bash
npm run dev
```

Vite normally starts the application on:

```text
http://localhost:5173
```

Open this address in a browser and log in with the administrator or another configured user.

### Custom Backend URL

The frontend uses:

```text
http://localhost:8080
```

as its default API URL.

If the backend is running on another URL or port, create:

```text
frontend/.env.local
```

with:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Replace the value with the actual backend URL.

Using `.env.local` is recommended for local frontend overrides because `*.local` files are ignored by the frontend Git configuration.

## 9. Run the Backend Tests

Make sure the PostgreSQL test database exists:

```text
afsprakenmanagement_test
```

The tests use the same `DB_USERNAME` and `DB_PASSWORD` values from the root `.env` file, but connect to the separate test database.

From the project root, run:

```bash
mvn test
```

The test configuration is located in:

```text
src/test/resources/application-test.properties
```

## 10. Check the Frontend

From the `frontend` directory, run the linter:

```bash
npm run lint
```

Create a production build:

```bash
npm run build
```

A successful build is generated in:

```text
frontend/dist
```

## 11. Recommended Local Startup Order

For a normal development session:

1. Start PostgreSQL.
2. Start the backend from the project root:

   ```bash
   mvn spring-boot:run
   ```

3. Open a second terminal.
4. Start the frontend:

   ```bash
   cd frontend
   npm run dev
   ```

5. Open:

   ```text
   http://localhost:5173
   ```

## Environment Variables

### Database

| Variable | Purpose | Typical local value |
| --- | --- | --- |
| `DB_URL` | Main PostgreSQL database connection | `jdbc:postgresql://localhost:5432/afsprakenmanagement` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | local PostgreSQL password |

### Frontend and Session

| Variable | Purpose | Default/local value |
| --- | --- | --- |
| `FRONTEND_URL` | Allowed frontend origin for backend CORS | `http://localhost:5173` |
| `SESSION_TIMEOUT` | HTTP session timeout | `30m` |
| `SESSION_COOKIE_SAME_SITE` | SameSite cookie setting | `lax` |
| `SESSION_COOKIE_SECURE` | Require HTTPS for the session cookie | `false` locally |

### Administrator Bootstrap

| Variable | Purpose |
| --- | --- |
| `BOOTSTRAP_ADMIN_ENABLED` | Enables initial administrator creation |
| `BOOTSTRAP_ADMIN_NAME` | Administrator display name |
| `BOOTSTRAP_ADMIN_EMAIL` | Administrator login email |
| `BOOTSTRAP_ADMIN_PASSWORD` | Administrator password, minimum 12 characters |
| `BOOTSTRAP_ADMIN_DEPARTMENT` | Administrator department |

### Optional Test User Bootstrap

| Variable | Purpose |
| --- | --- |
| `BOOTSTRAP_TEST_USER_ENABLED` | Enables test-user creation |
| `BOOTSTRAP_TEST_USER_NAME` | Test-user display name |
| `BOOTSTRAP_TEST_USER_EMAIL` | Test-user login email |
| `BOOTSTRAP_TEST_USER_PASSWORD` | Test-user password, minimum 12 characters |
| `BOOTSTRAP_TEST_USER_DEPARTMENT` | Test-user department |

## Project Structure

```text
TaskManagement/
├── frontend/                  React + TypeScript frontend
├── src/
│   ├── main/
│   │   ├── java/             Spring Boot backend
│   │   └── resources/        Backend configuration
│   └── test/
│       ├── java/             Backend tests
│       └── resources/        Test configuration
├── .env.example              Example local backend configuration
├── pom.xml                   Maven configuration
└── README.md
```

## Troubleshooting

### The backend cannot connect to PostgreSQL

Check:

- PostgreSQL is running;
- both databases exist;
- `DB_USERNAME` and `DB_PASSWORD` are correct;
- `DB_URL` points to the correct host, port and database.

### The frontend receives CORS errors

Check that the frontend URL exactly matches:

```env
FRONTEND_URL=http://localhost:5173
```

If the frontend is running on another port, update the value and restart the backend.

### The frontend cannot reach the backend

The frontend expects the backend on:

```text
http://localhost:8080
```

If the backend uses another address, configure `VITE_API_BASE_URL` in `frontend/.env.local`.

### Login is not possible on a new database

A new database has no users.

Temporarily enable `BOOTSTRAP_ADMIN_ENABLED`, configure the administrator values, start the backend once and then disable the bootstrap again.

### Backend tests fail because of the database

Verify that:

```text
afsprakenmanagement_test
```

exists and that the PostgreSQL account from `.env` has access to it.

## Security Notes

- Never commit `.env`.
- Never commit real passwords or credentials.
- Use `SESSION_COOKIE_SECURE=true` when the application is deployed behind HTTPS.
- The local defaults in `.env.example` are intended for development only.
