import { leesApiFout } from './errors'
import { apiFetch } from './http'

import type {
    GebruikerBeheer,
    UpdateGebruikerToegangRequest,
} from '../types/auth'

export async function getGebruikersVoorBeheer():
    Promise<GebruikerBeheer[]> {
    const response = await apiFetch(
        '/api/gebruikers/beheer',
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                `Gebruikers ophalen mislukt (${response.status})`,
                {
                    toonVeldFouten: true,
                },
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
            await leesApiFout(
                response,
                `Gebruiker wijzigen mislukt (${response.status})`,
                {
                    toonVeldFouten: true,
                },
            ),
        )
    }

    return response.json() as Promise<GebruikerBeheer>
}