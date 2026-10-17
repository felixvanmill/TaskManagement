import {
    useEffect,
    useState,
} from 'react'

import {
    OpmerkingenSection,
} from '../components/OpmerkingenSection'

import {
    Link,
    useParams,
} from 'react-router-dom'

import {
    getAfspraak,
} from '../api/afspraken'

import type {
    AfspraakDetail,
    AfspraakStatus,
    AfspraakType,
    Beslissing,
    ProcesstapStatus,
} from '../types/afspraak'

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

function statusLabel(
    status: AfspraakStatus,
) {
    switch (status) {
        case 'OPEN':
            return 'Open'

        case 'IN_BEHANDELING':
            return 'In behandeling'

        case 'AFGEROND':
            return 'Afgerond'
    }
}

function typeLabel(
    type: AfspraakType,
) {
    switch (type) {
        case 'ALGEMEEN':
            return 'Algemeen'

        case 'CAPACITEITSWISSEL':
            return 'Capaciteitswissel'

        case 'ORDERVERPLAATSING':
            return 'Orderverplaatsing'
    }
}

function processtapStatusLabel(
    status: ProcesstapStatus,
) {
    switch (status) {
        case 'NIET_GESTART':
            return 'Niet gestart'

        case 'IN_UITVOERING':
            return 'In uitvoering'

        case 'AFGEROND':
            return 'Afgerond'
    }
}

function beslissingLabel(
    beslissing: Beslissing,
) {
    switch (beslissing) {
        case 'GEEN':
            return 'Nog geen beslissing'

        case 'GOEDGEKEURD':
            return 'Goedgekeurd'

        case 'AFGEWEZEN':
            return 'Afgewezen'
    }
}

export function AfspraakDetailPage() {
    const { id } =
        useParams<{ id: string }>()

    const [
        afspraak,
        setAfspraak,
    ] = useState<AfspraakDetail | null>(null)

    const [
        laden,
        setLaden,
    ] = useState(true)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    useEffect(() => {
        async function laadAfspraak() {
            if (id === undefined) {
                setFoutmelding(
                    'Geen afspraak geselecteerd',
                )

                setLaden(false)

                return
            }

            try {
                const response =
                    await getAfspraak(id)

                setAfspraak(response)
            } catch (error) {
                setFoutmelding(
                    error instanceof Error
                        ? error.message
                        : 'Afspraak kon niet worden geladen',
                )
            } finally {
                setLaden(false)
            }
        }

        void laadAfspraak()
    }, [id])

    if (laden) {
        return (
            <main className="dashboard-page">
                <p>
                    Afspraak laden...
                </p>
            </main>
        )
    }

    if (
        foutmelding !== null
        || afspraak === null
    ) {
        return (
            <main className="dashboard-page">
                <Link
                    to="/"
                    className="back-link"
                >
                    ← Terug naar afspraken
                </Link>

                <div className="alert-error">
                    {foutmelding
                        ?? 'Afspraak niet gevonden'}
                </div>
            </main>
        )
    }

    return (
        <main className="dashboard-page">
            <Link
                to="/"
                className="back-link"
            >
                ← Terug naar afspraken
            </Link>

            <header className="detail-header">
                <div>
                    <p className="eyebrow">
                        {typeLabel(
                            afspraak.type,
                        )}
                    </p>

                    <h1>
                        {afspraak.titel}
                    </h1>

                    <p className="detail-description">
                        {afspraak.beschrijving}
                    </p>
                </div>

                <span
                    className={
                        `status-badge status-${afspraak.status.toLowerCase()}`
                    }
                >
                    {statusLabel(
                        afspraak.status,
                    )}
                </span>
            </header>

            <section className="detail-grid">
                <article className="content-card">
                    <h2>
                        Algemene gegevens
                    </h2>

                    <dl className="detail-list">
                        <div>
                            <dt>Ingangsdatum</dt>
                            <dd>
                                {formatteerDatum(
                                    afspraak.ingangsdatum,
                                )}
                            </dd>
                        </div>

                        <div>
                            <dt>Deadline</dt>
                            <dd>
                                {formatteerDatum(
                                    afspraak.deadline,
                                )}
                            </dd>
                        </div>

                        <div>
                            <dt>Initiatiefnemer</dt>
                            <dd>
                                {
                                    afspraak
                                        .initiatiefnemer
                                        .naam
                                }
                            </dd>
                        </div>

                        <div>
                            <dt>Afdeling</dt>
                            <dd>
                                {
                                    afspraak
                                        .initiatiefnemer
                                        .afdeling
                                        ?.naam
                                    ?? 'Geen afdeling'
                                }
                            </dd>
                        </div>

                        <div>
                            <dt>Aangemaakt</dt>
                            <dd>
                                {formatteerDatumTijd(
                                    afspraak.aangemaaktOp,
                                )}
                            </dd>
                        </div>

                        <div>
                            <dt>Laatst gewijzigd</dt>
                            <dd>
                                {formatteerDatumTijd(
                                    afspraak
                                        .laatstGewijzigdOp,
                                )}
                            </dd>
                        </div>
                    </dl>
                </article>

                <article className="content-card">
                    <h2>
                        Context
                    </h2>

                    <div className="detail-text-block">
                        <h3>Reden</h3>

                        <p>
                            {afspraak.reden
                                || 'Niet ingevuld'}
                        </p>
                    </div>

                    <div className="detail-text-block">
                        <h3>Achtergrond</h3>

                        <p>
                            {afspraak.achtergrond
                                || 'Niet ingevuld'}
                        </p>
                    </div>

                    <div className="detail-text-block">
                        <h3>Aannames</h3>

                        <p>
                            {afspraak.aannames
                                || 'Niet ingevuld'}
                        </p>
                    </div>
                </article>
            </section>

            <section className="detail-section">
                <h2>
                    Processtappen
                </h2>

                {afspraak.type === 'CAPACITEITSWISSEL'
                    && afspraak.capaciteitswissel !== null && (
                        <section className="detail-section">
                            <h2>
                                Capaciteitswissel
                            </h2>

                            <article className="content-card">
                                <dl className="detail-list">
                                    <div>
                                        <dt>Fabriek</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .fabriek
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Finish type</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .finishType
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Grade</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .grade
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Capaciteit van</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .capaciteitVan
                                            }
                                            {' '}
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .capaciteitEenheid
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Capaciteit naar</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .capaciteitNaar
                                            }
                                            {' '}
                                            {
                                                afspraak
                                                    .capaciteitswissel
                                                    .capaciteitEenheid
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Periode</dt>
                                        <dd>
                                            {formatteerDatum(
                                                afspraak
                                                    .capaciteitswissel
                                                    .periodeVan,
                                            )}
                                            {' t/m '}
                                            {formatteerDatum(
                                                afspraak
                                                    .capaciteitswissel
                                                    .periodeTot,
                                            )}
                                        </dd>
                                    </div>
                                </dl>
                            </article>
                        </section>
                    )}

                {afspraak.type === 'ORDERVERPLAATSING'
                    && afspraak.orderverplaatsing !== null && (
                        <section className="detail-section">
                            <h2>
                                Orderverplaatsing
                            </h2>

                            <article className="content-card">
                                <dl className="detail-list">
                                    <div>
                                        <dt>Van fabriek</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .orderverplaatsing
                                                    .vanFabriek
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Naar fabriek</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .orderverplaatsing
                                                    .naarFabriek
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Finish type</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .orderverplaatsing
                                                    .finishType
                                            }
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Totaal volume</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .orderverplaatsing
                                                    .totaalVolumeTons
                                            }
                                            {' tons'}
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>
                                            Gewenste verplaatsingsdatum
                                        </dt>
                                        <dd>
                                            {formatteerDatum(
                                                afspraak
                                                    .orderverplaatsing
                                                    .gewensteVerplaatsingsdatum,
                                            )}
                                        </dd>
                                    </div>

                                    <div>
                                        <dt>Uitvoeringsstatus</dt>
                                        <dd>
                                            {
                                                afspraak
                                                    .orderverplaatsing
                                                    .uitvoeringsstatus
                                                ?? 'Niet ingesteld'
                                            }
                                        </dd>
                                    </div>
                                </dl>
                            </article>
                        </section>
                    )}

                {afspraak.processtappen.length === 0 ? (
                    <div className="content-card">
                        <p>
                            Geen processtappen beschikbaar.
                        </p>
                    </div>
                ) : (
                    <div className="process-list">
                        {afspraak.processtappen.map(
                            (processtap) => (
                                <article
                                    key={processtap.id}
                                    className="content-card process-card"
                                >
                                    <div>
                                        <span className="process-number">
                                            {
                                                processtap
                                                    .volgorde
                                            }
                                        </span>
                                    </div>

                                    <div>
                                        <h3>
                                            {
                                                processtap
                                                    .naam
                                            }
                                        </h3>

                                        <p>
                                            Verantwoordelijke:{' '}
                                            <strong>
                                                {
                                                    processtap
                                                        .verantwoordelijke
                                                        .naam
                                                }
                                            </strong>
                                        </p>

                                        <p>
                                            Deadline:{' '}
                                            {formatteerDatum(
                                                processtap.deadline,
                                            )}
                                        </p>
                                    </div>

                                    <strong>
                                        {processtapStatusLabel(
                                            processtap.status,
                                        )}
                                    </strong>
                                </article>
                            ),
                        )}
                    </div>
                )}
            </section>

            <section className="detail-grid">
                <article className="content-card">
                    <h2>
                        Betrokkenen
                    </h2>

                    {afspraak.betrokkenGebruikers.length === 0
                        ? (
                            <p>
                                Geen betrokken gebruikers.
                            </p>
                        )
                        : (
                            <ul className="simple-list">
                                {
                                    afspraak
                                        .betrokkenGebruikers
                                        .map(
                                            (gebruiker) => (
                                                <li
                                                    key={
                                                        gebruiker.id
                                                    }
                                                >
                                                    <strong>
                                                        {
                                                            gebruiker
                                                                .naam
                                                        }
                                                    </strong>

                                                    <span>
                                                        {
                                                            gebruiker
                                                                .afdeling
                                                                ?.naam
                                                            ?? 'Geen afdeling'
                                                        }
                                                    </span>
                                                </li>
                                            ),
                                        )
                                }
                            </ul>
                        )}

                    <h3>
                        Betrokken afdelingen
                    </h3>

                    {afspraak.betrokkenAfdelingen.length === 0
                        ? (
                            <p>
                                Geen betrokken afdelingen.
                            </p>
                        )
                        : (
                            <ul className="simple-list">
                                {
                                    afspraak
                                        .betrokkenAfdelingen
                                        .map(
                                            (afdeling) => (
                                                <li
                                                    key={
                                                        afdeling.id
                                                    }
                                                >
                                                    {
                                                        afdeling
                                                            .naam
                                                    }
                                                </li>
                                            ),
                                        )
                                }
                            </ul>
                        )}
                </article>

                <article className="content-card">
                    <h2>
                        Goedkeuring
                    </h2>

                    {afspraak.bevestigingen.length === 0
                        ? (
                            <p>
                                Geen bevestigingen beschikbaar.
                            </p>
                        )
                        : (
                            <ul className="simple-list">
                                {
                                    afspraak
                                        .bevestigingen
                                        .map(
                                            (bevestiging) => (
                                                <li
                                                    key={
                                                        bevestiging.id
                                                    }
                                                >
                                                    <strong>
                                                        {
                                                            bevestiging
                                                                .gebruiker
                                                                .naam
                                                        }
                                                    </strong>

                                                    <span>
                                                        {bevestiging.gezien
                                                            ? 'Gezien'
                                                            : 'Nog niet gezien'}
                                                        {' · '}
                                                        {beslissingLabel(
                                                            bevestiging
                                                                .beslissing,
                                                        )}
                                                    </span>
                                                </li>
                                            ),
                                        )
                                }
                            </ul>
                        )}
                </article>
            </section>

            <OpmerkingenSection
                afspraakId={afspraak.id}
            />

            <section className="detail-section">
                <h2>
                    Wijzigingshistorie
                </h2>

                {afspraak.wijzigingen.length === 0 ? (
                    <div className="content-card">
                        <p>
                            Nog geen wijzigingen geregistreerd.
                        </p>
                    </div>
                ) : (
                    <div className="history-list">
                        {
                            afspraak.wijzigingen
                                .map(
                                    (wijziging) => (
                                        <article
                                            key={
                                                wijziging.id
                                            }
                                            className="content-card history-item"
                                        >
                                            <div>
                                                <strong>
                                                    {
                                                        wijziging
                                                            .onderdeel
                                                    }
                                                </strong>

                                                <p>
                                                    {
                                                        wijziging
                                                            .oudeWaarde
                                                        ?? '—'
                                                    }
                                                    {' → '}
                                                    {
                                                        wijziging
                                                            .nieuweWaarde
                                                        ?? '—'
                                                    }
                                                </p>
                                            </div>

                                            <div className="history-meta">
                                                <span>
                                                    {
                                                        wijziging
                                                            .gewijzigdDoor
                                                            .naam
                                                    }
                                                </span>

                                                <span>
                                                    {
                                                        formatteerDatumTijd(
                                                            wijziging
                                                                .gewijzigdOp,
                                                        )
                                                    }
                                                </span>
                                            </div>
                                        </article>
                                    ),
                                )
                        }
                    </div>
                )}
            </section>
        </main>
    )
}