import { leesApiFout } from './errors'
import { apiFetch } from './http'

import type {
    AfspraakDetail,
    AfspraakStatus,
    AfspraakSummary,
    AfspraakType,
    CapaciteitswisselInput,
    CreateAfspraakRequest,
    OrderverplaatsingInput,
} from '../types/afspraak'

export interface AfspraakFilterParams {
    type?: AfspraakType
    status?: AfspraakStatus
    verantwoordelijkeId?: string
    afdelingId?: string
    fabriek?: string
    periodeVan?: string
    periodeTot?: string
}

export interface UpdateAfspraakRequest {
    titel: string
    beschrijving: string
    reden: string
    achtergrond: string
    aannames: string
    ingangsdatum: string
    deadline: string
    capaciteitswissel: CapaciteitswisselInput | null
    orderverplaatsing: OrderverplaatsingInput | null
}

export interface UpdateGoedkeurdersRequest {
    goedkeurderIds: string[]
}

async function leesAfspraken(
    pad: string,
): Promise<AfspraakSummary[]> {
    const response = await apiFetch(
        pad,
    )

    if (!response.ok) {
        throw new Error(
            await leesApiFout(
                response,
                `Afspraken ophalen mislukt (${response.status})`,
                {
                    toonVeldFouten: true,
                    gebruikStandaardBijLegeMelding: true,
                },
            ),
        )
    }

    return response.json() as Promise<AfspraakSummary[]>
}

export async function getActieveAfspraken():
    Promise<AfspraakSummary[]> {
    return leesAfspraken(
        '/api/afspraken/actief',
    )
}

export async function getArchiefAfspraken():
    Promise<AfspraakSummary[]> {
    return leesAfspraken(
        '/api/afspraken/archief',
    )
}

export async function zoekAfspraken(
    zoekterm: string,
): Promise<AfspraakSummary[]> {
    const params =
        new URLSearchParams({
            zoekterm,
        })

    return leesAfspraken(
        `/api/afspraken/zoeken?${params.toString()}`,
    )
}

export async function filterAfspraken(
    filters: AfspraakFilterParams,
): Promise<AfspraakSummary[]> {
    const params =
        new URLSearchParams()

    if (filters.type) {
        params.set(
            'type',
            filters.type,
        )
    }

    if (filters.status) {
        params.set(
            'status',
            filters.status,
        )
    }

    if (filters.verantwoordelijkeId) {
        params.set(
            'verantwoordelijkeId',
            filters.verantwoordelijkeId,
        )
    }

    if (filters.afdelingId) {
        params.set(
            'afdelingId',
            filters.afdelingId,
        )
    }

    if (filters.fabriek?.trim()) {
        params.set(
            'fabriek',
            filters.fabriek.trim(),
        )
    }

    if (filters.periodeVan) {
        params.set(
            'periodeVan',
            filters.periodeVan,
        )
    }

    if (filters.periodeTot) {
        params.set(
            'periodeTot',
            filters.periodeTot,
        )
    }

    const query =
        params.toString()

    return leesAfspraken(
        query
            ? `/api/afspraken/filter?${query}`
            : '/api/afspraken/filter',
    )
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
            await leesApiFout(
                response,
                `Afspraak ophalen mislukt (${response.status})`,
                {
                    toonVeldFouten: true,
                    gebruikStandaardBijLegeMelding: true,
                },
            ),
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
                {
                    toonVeldFouten: true,
                    gebruikStandaardBijLegeMelding: true,
                },
            )

        throw new Error(melding)
    }

    return response.json() as Promise<AfspraakDetail>
}

export async function updateAfspraak(
    id: string,
    request: UpdateAfspraakRequest,
): Promise<AfspraakDetail> {
    const response = await apiFetch(
        `/api/afspraken/${id}`,
        {
            method: 'PUT',
            body: JSON.stringify(request),
        },
    )

    if (!response.ok) {
        const melding =
            await leesApiFout(
                response,
                `Afspraak bijwerken mislukt (${response.status})`,
                {
                    toonVeldFouten: true,
                    gebruikStandaardBijLegeMelding: true,
                },
            )

        throw new Error(melding)
    }

    return response.json() as Promise<AfspraakDetail>
}

export async function wijzigGoedkeurders(
    id: string,
    request: UpdateGoedkeurdersRequest,
): Promise<AfspraakDetail> {
    const response = await apiFetch(
        `/api/afspraken/${id}/goedkeurders`,
        {
            method: 'PUT',
            body: JSON.stringify(request),
        },
    )

    if (!response.ok) {
        const melding =
            await leesApiFout(
                response,
                `Goedkeurders wijzigen mislukt (${response.status})`,
                {
                    toonVeldFouten: true,
                    gebruikStandaardBijLegeMelding: true,
                },
            )

        throw new Error(melding)
    }

    return response.json() as Promise<AfspraakDetail>
}