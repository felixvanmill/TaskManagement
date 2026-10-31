import { apiFetch } from './http'

import type {
    Afdeling,
} from '../types/afdeling'

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