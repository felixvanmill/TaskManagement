export type GebruikersRol =
    | 'GEBRUIKER'
    | 'BEHEERDER'

export interface Afdeling {
    id: string
    naam: string
}

export interface AuthUser {
    id: string
    naam: string
    email: string
    rol: GebruikersRol
    afdeling: Afdeling | null
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