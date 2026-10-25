import { apiFetch } from './http'

import type {
    Afdeling,
    AfspraakGebruiker,
} from '../types/afspraak'

export async function getGebruikers():
    Promise<AfspraakGebruiker[]> {
    const response = await apiFetch(
        '/api/gebruikers',
    )

    if (!response.ok) {
        throw new Error(
            `Gebruikers ophalen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<AfspraakGebruiker[]>
}

export async function getAfdelingen():
    Promise<Afdeling[]> {
    const response = await apiFetch(
        '/api/afdelingen',
    )

    if (!response.ok) {
        throw new Error(
            `Afdelingen ophalen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<Afdeling[]>
}