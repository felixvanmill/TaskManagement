
import { apiFetch } from './http'
import { leesApiFout } from './errors'

import type {
    Notificatie,
    NotificatieAantalResponse,
} from '../types/notificatie'

export async function getNotificaties():
    Promise<Notificatie[]> {

    const response = await apiFetch(
        '/api/notificaties',
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                'Notificaties ophalen mislukt',
            ),
        )
    }

    return response.json() as Promise<Notificatie[]>
}

export async function getOngelezenAantal():
    Promise<number> {

    const response = await apiFetch(
        '/api/notificaties/ongelezen-aantal',
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                'Aantal notificaties ophalen mislukt',
            ),
        )
    }

    const data =
        await response.json() as NotificatieAantalResponse

    return data.aantal
}

export async function markeerNotificatieGelezen(
    notificatieId: string,
): Promise<void> {

    const response = await apiFetch(
        `/api/notificaties/${notificatieId}/gelezen`,
        {
            method: 'PUT',
        },
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                'Notificatie als gelezen markeren mislukt',
            ),
        )
    }
}
