export type AfspraakType =
    | 'ALGEMEEN'
    | 'CAPACITEITSWISSEL'
    | 'ORDERVERPLAATSING'

export type AfspraakStatus =
    | 'OPEN'
    | 'IN_BEHANDELING'
    | 'AFGEROND'

export interface AfspraakGebruiker {
    id: string
    naam: string
    email: string
    afdeling: {
        id: string
        naam: string
    } | null
}

export interface AfspraakSummary {
    id: string
    titel: string
    type: AfspraakType
    status: AfspraakStatus
    ingangsdatum: string
    deadline: string
    initiatiefnemer: AfspraakGebruiker
    laatstGewijzigdOp: string
}