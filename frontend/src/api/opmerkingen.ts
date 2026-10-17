import { apiFetch } from './http'

import type {
    Opmerking,
} from '../types/opmerking'

export async function getOpmerkingen(
    afspraakId: string,
): Promise<Opmerking[]> {
    const response = await apiFetch(
        `/api/afspraken/${afspraakId}/opmerkingen`,
    )

    if (!response.ok) {
        throw new Error(
            `Opmerkingen ophalen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<Opmerking[]>
}

export async function voegOpmerkingToe(
    afspraakId: string,
    tekst: string,
): Promise<Opmerking> {
    const response = await apiFetch(
        `/api/afspraken/${afspraakId}/opmerkingen`,
        {
            method: 'POST',
            body: JSON.stringify({
                tekst,
            }),
        },
    )

    if (!response.ok) {
        throw new Error(
            `Opmerking toevoegen mislukt (${response.status})`,
        )
    }

    return response.json() as Promise<Opmerking>
}