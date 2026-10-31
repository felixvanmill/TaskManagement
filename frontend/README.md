# Frontend

The React / TypeScript interface for Outokumpu operational agreement management.

See the [main project README](../README.md) for requirements, database and account setup, environment variables, startup, and test/build instructions. That document is the single source for full application installation.

## npm scripts

Run these commands from this directory:

| Command | Purpose |
| --- | --- |
| `npm ci` | Install the dependency versions in `package-lock.json` |
| `npm run dev` | Start the Vite development server |
| `npm run lint` | Check JavaScript and TypeScript with ESLint |
| `npm run build` | Run TypeScript checks and create `dist/` |
| `npm run preview` | Preview an existing production build locally |

There is no frontend automated test script. Lint and build checks complement the Java tests and manual verification of the user flows.

## Source layout

- `src/main.tsx` mounts React, the router and the authentication provider.
- `src/App.tsx` declares the login, agreement and administrator routes.
- `src/pages/` contains screens; `src/components/` contains shared UI sections and route guards.
- `src/api/` contains API calls, session/CSRF transport and shared error parsing.
- `src/auth/` contains authentication state and its React hook.
- `src/types/` describes API data, including the shared department type.
- `src/utils/` contains shared date formatting, agreement labels and selection helpers.
- `src/index.css` contains the application styles.

## API integration

`src/api/http.ts` uses `VITE_API_BASE_URL`, with `http://localhost:8080` as the default. API requests carry session cookies. Login refreshes the CSRF token; logout clears it. Keep this transport behavior in the shared module.

Shared error parsing preserves each API module's existing treatment of validation errors and empty messages. Likewise, the agreement helpers preserve the distinction between an active process step and a default assignment selection.

Frontend route guards do not replace backend authorization. API response types describe the contract; their TypeScript assertions do not perform runtime validation.
