import {
    useState,
} from 'react'

import {
    wijzigUitvoeringsstatus,
} from '../api/workflow'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    AfspraakDetail,
    UitvoeringsStatus,
} from '../types/afspraak'

interface TypespecifiekeDetailsSectionProps {
    afspraak: AfspraakDetail
    onAfspraakGewijzigd?: (
        afspraak: AfspraakDetail,
    ) => void
}

function formatteerDatum(
    datum: string,
) {
    return new Intl.DateTimeFormat(
        'nl-NL',
        {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        },
    ).format(
        new Date(`${datum}T00:00:00`),
    )
}

function uitvoeringsstatusLabel(
    status: UitvoeringsStatus | null,
) {
    switch (status) {
        case 'NIET_UITGEVOERD':
            return 'Niet uitgevoerd'

        case 'IN_UITVOERING':
            return 'In uitvoering'

        case 'UITGEVOERD':
            return 'Uitgevoerd'

        default:
            return 'Niet ingesteld'
    }
}

function bepaalHuidigeVerantwoordelijke(
    afspraak: AfspraakDetail,
) {
    if (afspraak.processtappen.length === 0) {
        return null
    }

    const actieveProcesstap =
        afspraak.processtappen.find(
            (processtap) =>
                processtap.status !== 'AFGEROND',
        )

    if (actieveProcesstap !== undefined) {
        return actieveProcesstap.verantwoordelijke
    }

    return afspraak.processtappen[
    afspraak.processtappen.length - 1
        ].verantwoordelijke
}

export function TypespecifiekeDetailsSection({
                                                 afspraak,
                                                 onAfspraakGewijzigd,
                                             }: TypespecifiekeDetailsSectionProps) {
    const {
        gebruiker,
    } = useAuth()

    const [
        geselecteerdeStatus,
        setGeselecteerdeStatus,
    ] = useState<UitvoeringsStatus>(
        afspraak.orderverplaatsing?.uitvoeringsstatus
        ?? 'NIET_UITGEVOERD',
    )

    const [
        bezig,
        setBezig,
    ] = useState(false)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    async function handleUitvoeringsstatusWijzigen() {
        if (afspraak.orderverplaatsing === null) {
            return
        }

        if (
            geselecteerdeStatus
            === afspraak.orderverplaatsing
                .uitvoeringsstatus
        ) {
            return
        }

        try {
            setBezig(true)
            setFoutmelding(null)

            const bijgewerkteAfspraak =
                await wijzigUitvoeringsstatus(
                    afspraak.id,
                    geselecteerdeStatus,
                )

            onAfspraakGewijzigd?.(
                bijgewerkteAfspraak,
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Uitvoeringsstatus kon niet worden gewijzigd',
            )
        } finally {
            setBezig(false)
        }
    }

    if (
        afspraak.type === 'CAPACITEITSWISSEL'
        && afspraak.capaciteitswissel !== null
    ) {
        const gegevens =
            afspraak.capaciteitswissel

        return (
            <section className="detail-section">
                <h2>
                    Capaciteitswissel
                </h2>

                <article className="content-card">
                    <dl className="detail-list">
                        <div>
                            <dt>Fabriek</dt>
                            <dd>
                                {gegevens.fabriek}
                            </dd>
                        </div>

                        <div>
                            <dt>Finish type</dt>
                            <dd>
                                {gegevens.finishType}
                            </dd>
                        </div>

                        <div>
                            <dt>Grade</dt>
                            <dd>
                                {gegevens.grade}
                            </dd>
                        </div>

                        <div>
                            <dt>Capaciteit van</dt>
                            <dd>
                                {gegevens.capaciteitVan}
                                {' '}
                                {gegevens.capaciteitEenheid}
                            </dd>
                        </div>

                        <div>
                            <dt>Capaciteit naar</dt>
                            <dd>
                                {gegevens.capaciteitNaar}
                                {' '}
                                {gegevens.capaciteitEenheid}
                            </dd>
                        </div>

                        <div>
                            <dt>Periode</dt>
                            <dd>
                                {formatteerDatum(
                                    gegevens.periodeVan,
                                )}
                                {' t/m '}
                                {formatteerDatum(
                                    gegevens.periodeTot,
                                )}
                            </dd>
                        </div>
                    </dl>
                </article>
            </section>
        )
    }

    if (
        afspraak.type === 'ORDERVERPLAATSING'
        && afspraak.orderverplaatsing !== null
    ) {
        const gegevens =
            afspraak.orderverplaatsing

        const huidigeVerantwoordelijke =
            bepaalHuidigeVerantwoordelijke(
                afspraak,
            )

        const magWijzigen =
            gebruiker !== null
            && huidigeVerantwoordelijke !== null
            && gebruiker.id === huidigeVerantwoordelijke.id

        return (
            <section className="detail-section">
                <h2>
                    Orderverplaatsing
                </h2>

                <article className="content-card">
                    <dl className="detail-list">
                        <div>
                            <dt>Van fabriek</dt>
                            <dd>
                                {gegevens.vanFabriek}
                            </dd>
                        </div>

                        <div>
                            <dt>Naar fabriek</dt>
                            <dd>
                                {gegevens.naarFabriek}
                            </dd>
                        </div>

                        <div>
                            <dt>Finish type</dt>
                            <dd>
                                {gegevens.finishType}
                            </dd>
                        </div>

                        <div>
                            <dt>Totaal volume</dt>
                            <dd>
                                {gegevens.totaalVolumeTons}
                                {' tons'}
                            </dd>
                        </div>

                        <div>
                            <dt>
                                Gewenste verplaatsingsdatum
                            </dt>
                            <dd>
                                {formatteerDatum(
                                    gegevens
                                        .gewensteVerplaatsingsdatum,
                                )}
                            </dd>
                        </div>

                        <div>
                            <dt>Uitvoeringsstatus</dt>
                            <dd>
                                {uitvoeringsstatusLabel(
                                    gegevens
                                        .uitvoeringsstatus,
                                )}
                            </dd>
                        </div>
                    </dl>

                    {magWijzigen && (
                        <div className="workflow-actions">
                            <label className="form-field">
                                <span>
                                    Nieuwe uitvoeringsstatus
                                </span>

                                <select
                                    value={geselecteerdeStatus}
                                    disabled={bezig}
                                    onChange={(event) =>
                                        setGeselecteerdeStatus(
                                            event.target.value as UitvoeringsStatus,
                                        )
                                    }
                                >
                                    <option value="NIET_UITGEVOERD">
                                        Niet uitgevoerd
                                    </option>

                                    <option value="IN_UITVOERING">
                                        In uitvoering
                                    </option>

                                    <option value="UITGEVOERD">
                                        Uitgevoerd
                                    </option>
                                </select>
                            </label>

                            <button
                                type="button"
                                className="primary-button"
                                disabled={
                                    bezig
                                    || geselecteerdeStatus
                                    === gegevens.uitvoeringsstatus
                                }
                                onClick={() =>
                                    void handleUitvoeringsstatusWijzigen()
                                }
                            >
                                {bezig
                                    ? 'Opslaan...'
                                    : 'Status opslaan'}
                            </button>
                        </div>
                    )}

                    {foutmelding !== null && (
                        <div className="alert-error">
                            {foutmelding}
                        </div>
                    )}
                </article>
            </section>
        )
    }

    return null
}