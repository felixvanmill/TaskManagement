
export type NotificatieType =
    | 'TAAK_TOEGEWEZEN'
    | 'LEESVERZOEK'
    | 'GOEDKEURING_GEVRAAGD'

export interface Notificatie {
    id: string
    afspraakId: string
    type: NotificatieType
    bericht: string
    aangemaaktOp: string
    gelezenOp: string | null
}

export interface NotificatieAantalResponse {
    aantal: number
}
