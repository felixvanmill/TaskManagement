import { apiFetch } from './http'

import type {
    AfspraakDetail,
    AfspraakSummary,
    CreateAfspraakRequest,
} from '../types/afspraak'

interface ApiErrorResponse {
    message?: string
    veldFouten?: Record<string, string>
}

async function leesApiFout(
    response: Response,
    standaardMelding: string,
) {
    try {
        const fout =
            await response.json() as ApiErrorResponse

        if (
            fout.veldFouten
            && Object.keys(fout.veldFouten).length > 0
        ) {
            return Object.values(
                fout.veldFouten,
            ).join(', ')
        }

        return fout.message
            || standaardMelding
    } catch {
        return standaardMelding
    }
}

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

export async function createAfspraak(
    request: CreateAfspraakRequest,
): Promise<AfspraakDetail> {
    const response = await apiFetch(
        '/api/afspraken',
        {
            method: 'POST',
            body: JSON.stringify(request),
        },
    )

    if (!response.ok) {
        const melding =
            await leesApiFout(
                response,
                `Afspraak aanmaken mislukt (${response.status})`,
            )

        throw new Error(melding)
    }

    return response.json() as Promise<AfspraakDetail>
}