interface ApiErrorResponse {
    message?: string
    veldFouten?: Record<string, string>
}

interface ApiFoutOpties {
    toonVeldFouten?: boolean
    gebruikStandaardBijLegeMelding?: boolean
}

export async function leesApiFout(
    response: Response,
    standaardMelding: string,
    opties: ApiFoutOpties = {},
): Promise<string> {
    try {
        const fout =
            await response.json() as ApiErrorResponse

        if (
            opties.toonVeldFouten
            && fout.veldFouten
            && Object.keys(fout.veldFouten).length > 0
        ) {
            return Object.values(
                fout.veldFouten,
            ).join(', ')
        }

        return opties.gebruikStandaardBijLegeMelding
            ? fout.message || standaardMelding
            : fout.message ?? standaardMelding
    } catch {
        return standaardMelding
    }
}
