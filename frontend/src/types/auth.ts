import type {
    Afdeling,
} from './afdeling'

export type GebruikersRol =
    | 'GEBRUIKER'
    | 'BEHEERDER'

export interface AuthUser {
    id: string
    naam: string
    email: string
    rol: GebruikersRol
    afdeling: Afdeling | null
}

export interface GebruikerBeheer {
    id: string
    naam: string
    email: string
    rol: GebruikersRol
    actief: boolean
    lokaleToegang: boolean
    afdeling: Afdeling | null
}

export interface UpdateGebruikerToegangRequest {
    rol: GebruikersRol
    actief: boolean
}

export interface LoginRequest {
    email: string
    wachtwoord: string
}

export interface CsrfResponse {
    headerNaam: string
    parameterNaam: string
    token: string
}