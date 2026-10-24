import { apiFetch } from './http'

import type {
    AfspraakDetail,
    AfspraakStatus,
} from '../types/afspraak'

interface ApiErrorResponse {
    message?: string
}

async function voerWorkflowActieUit(
    pad: string,
    body?: unknown,
): Promise<AfspraakDetail> {
    const response = await apiFetch(
        pad,
        {
            method: 'PUT',
            ...(body !== undefined
                ? {
                    body: JSON.stringify(body),
                }
                : {}),
        },
    )

    if (!response.ok) {
        let melding =
            `Workflowactie mislukt (${response.status})`

        try {
            const fout =
                await response.json() as ApiErrorResponse

            if (fout.message) {
                melding = fout.message
            }
        } catch {
            // Gebruik standaardmelding.
        }

        throw new Error(melding)
    }

    return response.json() as Promise<AfspraakDetail>
}

export async function startProcesstap(
    processtapId: string,
): Promise<AfspraakDetail> {
    return voerWorkflowActieUit(
        `/api/processtappen/${processtapId}/starten`,
    )
}

export async function rondProcesstapAf(
    processtapId: string,
): Promise<AfspraakDetail> {
    return voerWorkflowActieUit(
        `/api/processtappen/${processtapId}/afronden`,
    )
}

export async function wijzigAfspraakStatus(
    afspraakId: string,
    status: AfspraakStatus,
): Promise<AfspraakDetail> {
    return voerWorkflowActieUit(
        `/api/afspraken/${afspraakId}/status`,
        {
            status,
        },
    )
}