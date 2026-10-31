import { leesApiFout } from './errors'
import { apiFetch } from './http'

import type {
    Beslissing,
    Bevestiging,
} from '../types/afspraak'

export async function markeerAfspraakGezien(
    afspraakId: string,
): Promise<Bevestiging> {
    const response = await apiFetch(
        `/api/afspraken/${afspraakId}/bevestiging/gezien`,
        {
            method: 'PUT',
        },
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                `Gezien bevestigen mislukt (${response.status})`,
            ),
        )
    }

    return response.json() as Promise<Bevestiging>
}

export async function registreerBeslissing(
    afspraakId: string,
    beslissing: Exclude<Beslissing, 'GEEN'>,
): Promise<Bevestiging> {
    const response = await apiFetch(
        `/api/afspraken/${afspraakId}/bevestiging/beslissing`,
        {
            method: 'PUT',
            body: JSON.stringify({
                beslissing,
            }),
        },
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                `Beslissing registreren mislukt (${response.status})`,
            ),
        )
    }

    return response.json() as Promise<Bevestiging>
}