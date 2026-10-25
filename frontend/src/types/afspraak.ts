export type AfspraakType =
    | 'ALGEMEEN'
    | 'CAPACITEITSWISSEL'
    | 'ORDERVERPLAATSING'

export type AfspraakStatus =
    | 'OPEN'
    | 'IN_BEHANDELING'
    | 'AFGEROND'

export type ProcesstapStatus =
    | 'NIET_GESTART'
    | 'IN_UITVOERING'
    | 'AFGEROND'

export type Beslissing =
    | 'GEEN'
    | 'GOEDGEKEURD'
    | 'AFGEWEZEN'

export type UitvoeringsStatus =
    | 'NIET_UITGEVOERD'
    | 'IN_UITVOERING'
    | 'UITGEVOERD'

export interface Afdeling {
    id: string
    naam: string
}

export interface AfspraakGebruiker {
    id: string
    naam: string
    email: string
    afdeling: Afdeling | null
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

export interface Processtap {
    id: string
    naam: string
    volgorde: number
    status: ProcesstapStatus
    deadline: string
    gestartOp: string | null
    afgerondOp: string | null
    verantwoordelijke: AfspraakGebruiker
}

export interface Bevestiging {
    id: string
    gebruiker: AfspraakGebruiker
    gezien: boolean
    gezienOp: string | null
    beslissing: Beslissing
    beslistOp: string | null
}

export interface Wijziging {
    id: string
    gewijzigdDoor: AfspraakGebruiker
    gewijzigdOp: string
    onderdeel: string
    oudeWaarde: string | null
    nieuweWaarde: string | null
}

export interface CapaciteitswisselData {
    fabriek: string
    finishType: string
    grade: string
    capaciteitVan: number
    capaciteitNaar: number
    capaciteitEenheid: string
    periodeVan: string
    periodeTot: string
}

export interface OrderverplaatsingData {
    vanFabriek: string
    naarFabriek: string
    finishType: string
    totaalVolumeTons: number
    gewensteVerplaatsingsdatum: string
    uitvoeringsstatus: UitvoeringsStatus | null
}

export interface AfspraakDetail {
    id: string
    titel: string
    beschrijving: string
    reden: string | null
    achtergrond: string | null
    aannames: string | null
    type: AfspraakType
    status: AfspraakStatus
    ingangsdatum: string
    deadline: string
    aangemaaktOp: string
    laatstGewijzigdOp: string
    afgerondOp: string | null
    initiatiefnemer: AfspraakGebruiker
    betrokkenAfdelingen: Afdeling[]
    betrokkenGebruikers: AfspraakGebruiker[]
    goedkeurders: AfspraakGebruiker[]
    processtappen: Processtap[]
    bevestigingen: Bevestiging[]
    wijzigingen: Wijziging[]
    capaciteitswissel: CapaciteitswisselData | null
    orderverplaatsing: OrderverplaatsingData | null
}

export interface CapaciteitswisselInput {
    fabriek: string
    finishType: string
    grade: string
    capaciteitVan: number
    capaciteitNaar: number
    capaciteitEenheid: string
    periodeVan: string
    periodeTot: string
}

export interface OrderverplaatsingInput {
    vanFabriek: string
    naarFabriek: string
    finishType: string
    totaalVolumeTons: number
    gewensteVerplaatsingsdatum: string
    uitvoeringsstatus: null
}

export interface CreateAfspraakRequest {
    titel: string
    beschrijving: string
    reden: string
    achtergrond: string
    aannames: string
    type: AfspraakType
    ingangsdatum: string
    deadline: string
    verantwoordelijkeId: string
    eersteProcesstapNaam: string
    eersteProcesstapDeadline: string
    betrokkeneIds: string[]
    goedkeurderIds: string[]
    afdelingIds: string[]
    capaciteitswissel: CapaciteitswisselInput | null
    orderverplaatsing: OrderverplaatsingInput | null
}