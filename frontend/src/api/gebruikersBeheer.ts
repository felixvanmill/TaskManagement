import { apiFetch } from './http'

import type {
    GebruikerBeheer,
    UpdateGebruikerToegangRequest,
} from '../types/auth'

interface ApiErrorResponse {
    message?: string
    veldFouten?: Record<string, string>
}

async function leesFoutmelding(
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
            ?? standaardMelding
    } catch {
        return standaardMelding
    }
}

export async function getGebruikersVoorBeheer():
    Promise<GebruikerBeheer[]> {
    const response = await apiFetch(
        '/api/gebruikers/beheer',
    )

    if (!response.ok) {
        throw new Error(
            await leesFoutmelding(
                response,
                `Gebruikers ophalen mislukt (${response.status})`,
            ),
        )
    }

    return response.json() as Promise<GebruikerBeheer[]>
}

export async function wijzigGebruikerToegang(
    gebruikerId: string,
    request: UpdateGebruikerToegangRequest,
): Promise<GebruikerBeheer> {
    const response = await apiFetch(
        `/api/gebruikers/${gebruikerId}/toegang`,
        {
            method: 'PUT',
            body: JSON.stringify(request),
        },
    )

    if (!response.ok) {
        throw new Error(
            await leesFoutmelding(
                response,
                `Gebruiker wijzigen mislukt (${response.status})`,
            ),
        )
    }

    return response.json() as Promise<GebruikerBeheer>
}