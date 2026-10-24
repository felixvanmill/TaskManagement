import { apiFetch } from './http'

import type {
    AfspraakDetail,
} from '../types/afspraak'

interface ApiErrorResponse {
    message?: string
}

async function voerWorkflowActieUit(
    pad: string,
): Promise<AfspraakDetail> {
    const response = await apiFetch(
        pad,
        {
            method: 'PUT',
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