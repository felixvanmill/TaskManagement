import { apiFetch } from './http'

import type {
    AfspraakDetail,
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

export async function getAfspraak(
    id: string,
): Promise<AfspraakDetail> {
    const response = await apiFetch(
        `/api/afspraken/${id}`,
    )

    if (response.status === 404) {
        throw new Error(
            'Afspraak niet gevonden',
        )
    }

    if (!response.ok) {
        throw new Error(
            `Afspraak ophalen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<AfspraakDetail>
}