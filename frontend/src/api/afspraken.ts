import { apiFetch } from './http'

import type {
    AfspraakSummary,
} from '../types/afspraak'

export async function getAfspraken():
    Promise<AfspraakSummary[]> {
    const response = await apiFetch(
        '/api/afspraken',
    )

    if (!response.ok) {
        throw new Error(
            `Afspraken ophalen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<AfspraakSummary[]>
}