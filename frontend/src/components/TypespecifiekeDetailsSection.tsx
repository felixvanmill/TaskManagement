import type {
    AfspraakDetail,
} from '../types/afspraak'

interface TypespecifiekeDetailsSectionProps {
    afspraak: AfspraakDetail
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
    status: string | null,
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

export function TypespecifiekeDetailsSection({
                                                 afspraak,
                                             }: TypespecifiekeDetailsSectionProps) {
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
                </article>
            </section>
        )
    }

    return null
}