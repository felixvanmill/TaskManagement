# Local Setup

## Requirements

Make sure the following tools are installed:

* Java 17
* Maven
* PostgreSQL

## Database Setup

Create two PostgreSQL databases:

```text
afsprakenmanagement
afsprakenmanagement_test
```

The databases are used as follows:

* `afsprakenmanagement` — used for normal application development.
* `afsprakenmanagement_test` — used for automated tests.

## Environment Configuration

Copy the example environment file:

```text
.env.example
```

and rename it to:

```text
.env
```

Then add your local PostgreSQL credentials:

```env
DB_URL=jdbc:postgresql://localhost:5432/afsprakenmanagement
DB_USERNAME=postgres
DB_PASSWORD=<your local PostgreSQL password>
```

> The `.env` file is ignored by Git and must **not** be committed.

## Run the Application

Start the application from your IDE by running:

```text
AfsprakenApplication
```

The application will automatically use the local `.env` configuration.

## Run Tests

Tests use the Spring `test` profile and connect to:

```text
afsprakenmanagement_test
```

Run the tests from IntelliJ, or use Maven:

```bash
mvn test
```

## Suggested Commit Message

```text
chore: finalize backend and test database configuration
```
