import {
    useState,
} from 'react'

import {
    markeerAfspraakGezien,
    registreerBeslissing,
} from '../api/bevestigingen'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    AfspraakDetail,
    Bevestiging,
} from '../types/afspraak'

interface BevestigingActiesSectionProps {
    afspraak: AfspraakDetail
    onAfspraakGewijzigd: (
        afspraak: AfspraakDetail,
    ) => void
}

function formatteerDatumTijd(
    datum: string,
) {
    return new Intl.DateTimeFormat(
        'nl-NL',
        {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        },
    ).format(
        new Date(datum),
    )
}

export function BevestigingActiesSection({
                                             afspraak,
                                             onAfspraakGewijzigd,
                                         }: BevestigingActiesSectionProps) {
    const {
        gebruiker,
    } = useAuth()

    const [
        bezig,
        setBezig,
    ] = useState(false)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    if (gebruiker === null) {
        return null
    }

    const mijnBevestiging =
        afspraak.bevestigingen.find(
            (bevestiging) =>
                bevestiging.gebruiker.id
                === gebruiker.id,
        )

    if (mijnBevestiging === undefined) {
        return null
    }

    const isGoedkeurder =
        afspraak.goedkeurders.some(
            (goedkeurder) =>
                goedkeurder.id
                === gebruiker.id,
        )

    function verwerkNieuweBevestiging(
        nieuweBevestiging: Bevestiging,
    ) {
        onAfspraakGewijzigd({
            ...afspraak,
            bevestigingen:
                afspraak.bevestigingen.map(
                    (bevestiging) =>
                        bevestiging.id
                        === nieuweBevestiging.id
                            ? nieuweBevestiging
                            : bevestiging,
                ),
        })
    }

    async function handleGezien() {
        try {
            setBezig(true)
            setFoutmelding(null)

            const bevestiging =
                await markeerAfspraakGezien(
                    afspraak.id,
                )

            verwerkNieuweBevestiging(
                bevestiging,
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Gezien bevestigen is mislukt',
            )
        } finally {
            setBezig(false)
        }
    }

    async function handleBeslissing(
        beslissing: 'GOEDGEKEURD' | 'AFGEWEZEN',
    ) {
        try {
            setBezig(true)
            setFoutmelding(null)

            const bevestiging =
                await registreerBeslissing(
                    afspraak.id,
                    beslissing,
                )

            verwerkNieuweBevestiging(
                bevestiging,
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Beslissing registreren is mislukt',
            )
        } finally {
            setBezig(false)
        }
    }

    return (
        <section className="content-card bevestiging-acties">
            <div>
                <h2>
                    Mijn bevestiging
                </h2>

                <p>
                    Geef aan dat je de afspraak hebt
                    gelezen en registreer indien nodig
                    je beslissing.
                </p>
            </div>

            <div className="bevestiging-status">
                <p>
                    Gezien:{' '}
                    <strong>
                        {mijnBevestiging.gezien
                            ? 'Ja'
                            : 'Nee'}
                    </strong>
                </p>

                {mijnBevestiging.gezienOp !== null && (
                    <p>
                        Gezien op:{' '}
                        {formatteerDatumTijd(
                            mijnBevestiging.gezienOp,
                        )}
                    </p>
                )}

                {isGoedkeurder && (
                    <>
                        <p>
                            Beslissing:{' '}
                            <strong>
                                {mijnBevestiging.beslissing
                                === 'GEEN'
                                    ? 'Nog geen beslissing'
                                    : mijnBevestiging.beslissing
                                    === 'GOEDGEKEURD'
                                        ? 'Goedgekeurd'
                                        : 'Afgewezen'}
                            </strong>
                        </p>

                        {mijnBevestiging.beslistOp !== null && (
                            <p>
                                Beslist op:{' '}
                                {formatteerDatumTijd(
                                    mijnBevestiging.beslistOp,
                                )}
                            </p>
                        )}
                    </>
                )}
            </div>

            <div className="bevestiging-buttons">
                {!mijnBevestiging.gezien && (
                    <button
                        type="button"
                        className="secondary-button"
                        disabled={bezig}
                        onClick={() =>
                            void handleGezien()
                        }
                    >
                        {bezig
                            ? 'Opslaan...'
                            : 'Markeer als gezien'}
                    </button>
                )}

                {isGoedkeurder && (
                    <>
                        <button
                            type="button"
                            className="primary-button"
                            disabled={
                                bezig
                                || mijnBevestiging.beslissing
                                === 'GOEDGEKEURD'
                            }
                            onClick={() =>
                                void handleBeslissing(
                                    'GOEDGEKEURD',
                                )
                            }
                        >
                            Goedkeuren
                        </button>

                        <button
                            type="button"
                            className="danger-button"
                            disabled={
                                bezig
                                || mijnBevestiging.beslissing
                                === 'AFGEWEZEN'
                            }
                            onClick={() =>
                                void handleBeslissing(
                                    'AFGEWEZEN',
                                )
                            }
                        >
                            Afwijzen
                        </button>
                    </>
                )}
            </div>

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}
        </section>
    )
}