import { apiFetch } from './http'

import type {
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