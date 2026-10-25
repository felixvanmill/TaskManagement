import type {
    AuthUser,
    CsrfResponse,
    LoginRequest,
} from '../types/auth'

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL
    ?? 'http://localhost:8080'

let csrf: CsrfResponse | null = null

async function laadCsrfToken(): Promise<CsrfResponse> {
    const response = await fetch(
        `${API_BASE_URL}/api/auth/csrf`,
        {
            credentials: 'include',
        },
    )

    if (!response.ok) {
        throw new Error(
            `CSRF-token ophalen mislukt (${response.status})`,
        )
    }

    csrf = await response.json() as CsrfResponse

    return csrf
}

async function zorgVoorCsrfToken(): Promise<CsrfResponse> {
    if (csrf !== null) {
        return csrf
    }

    return laadCsrfToken()
}

export async function apiFetch(
    path: string,
    options: RequestInit = {},
): Promise<Response> {
    const method =
        (options.method ?? 'GET').toUpperCase()

    const headers =
        new Headers(options.headers)

    const heeftBody =
        options.body !== undefined
        && options.body !== null

    if (
        heeftBody
        && !headers.has('Content-Type')
    ) {
        headers.set(
            'Content-Type',
            'application/json',
        )
    }

    const vereistCsrf =
        !['GET', 'HEAD', 'OPTIONS']
            .includes(method)

    if (vereistCsrf) {
        const csrfToken =
            await zorgVoorCsrfToken()

        headers.set(
            csrfToken.headerNaam,
            csrfToken.token,
        )
    }

    return fetch(
        `${API_BASE_URL}${path}`,
        {
            ...options,
            headers,
            credentials: 'include',
        },
    )
}

export async function login(
    request: LoginRequest,
): Promise<AuthUser> {
    const response = await fetch(
        `${API_BASE_URL}/api/auth/login`,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(request),
        },
    )

    if (!response.ok) {
        throw new Error(
            response.status === 401
                ? 'E-mailadres of wachtwoord is onjuist'
                : `Inloggen mislukt (${response.status})`,
        )
    }

    const gebruiker =
        await response.json() as AuthUser

    /*
     * Login maakt aan backendzijde een nieuwe
     * sessie. Daarom halen we daarna opnieuw
     * een CSRF-token op voor die sessie.
     */
    csrf = null
    await laadCsrfToken()

    return gebruiker
}

export async function getCurrentUser():
    Promise<AuthUser | null> {
    const response = await fetch(
        `${API_BASE_URL}/api/auth/me`,
        {
            credentials: 'include',
        },
    )

    if (response.status === 401) {
        return null
    }

    if (!response.ok) {
        throw new Error(
            `Gebruiker ophalen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<AuthUser>
}

export async function logout(): Promise<void> {
    const response = await apiFetch(
        '/api/auth/logout',
        {
            method: 'POST',
        },
    )

    if (!response.ok) {
        throw new Error(
            `Uitloggen mislukt (${response.status})`,
        )
    }

    csrf = null
}