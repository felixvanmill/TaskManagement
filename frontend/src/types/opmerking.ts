import type {
    AfspraakGebruiker,
} from './afspraak'

export interface Opmerking {
    id: string
    auteur: AfspraakGebruiker
    tekst: string
    aangemaaktOp: string
}