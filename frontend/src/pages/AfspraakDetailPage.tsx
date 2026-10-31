import {
    useEffect,
    useState,
} from 'react'

import {
    statusLabel,
    typeLabel,
} from '../utils/afspraak'

import {
    formatteerDatum,
    formatteerDatumTijd,
} from '../utils/datum'

import {
    useAuth,
} from '../auth/useAuth'

import {
    OpmerkingenSection,
} from '../components/OpmerkingenSection'

import {
    TypespecifiekeDetailsSection,
} from '../components/TypespecifiekeDetailsSection'

import {
    ToewijzingSection,
} from '../components/ToewijzingSection'

import {
    ProcesstappenSection,
} from '../components/ProcesstappenSection'

import {
    BevestigingActiesSection,
} from '../components/BevestigingActiesSection'

import {
    Link,
    useParams,
} from 'react-router-dom'

import {
    getAfspraak,
} from '../api/afspraken'

import {
    AfspraakStatusSection,
} from '../components/AfspraakStatusSection'

import type {
    AfspraakDetail,
    Beslissing,
} from '../types/afspraak'

type ToewijzingHistorie = {
    processtap: string
    betrokkenen: string
    afdelingen: string
    verantwoordelijke: string
}

type ToewijzingVerschil = {
    label: string
    oudeWaarde: string
    nieuweWaarde: string
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

function parseToewijzingWaarde(
    waarde: string | null,
): ToewijzingHistorie | null {

    if (waarde === null) {
        return null
    }

    const match =
        waarde.match(
            /^Processtap: (.*?) \| Betrokkenen: (.*?) \| Afdelingen: (.*?) \| Verantwoordelijke: (.*)$/,
        )

    if (match === null) {
        return null
    }

    const [
        ,
        processtap,
        betrokkenen,
        afdelingen,
        verantwoordelijke,
    ] = match

    if (
        processtap === undefined
        || betrokkenen === undefined
        || afdelingen === undefined
        || verantwoordelijke === undefined
    ) {
        return null
    }

    return {
        processtap,
        betrokkenen,
        afdelingen,
        verantwoordelijke,
    }
}

function bepaalToewijzingVerschillen(
    oudeWaarde: string | null,
    nieuweWaarde: string | null,
): ToewijzingVerschil[] | null {

    const oud =
        parseToewijzingWaarde(
            oudeWaarde,
        )

    const nieuw =
        parseToewijzingWaarde(
            nieuweWaarde,
        )

    if (
        oud === null
        || nieuw === null
    ) {
        return null
    }

    const verschillen: ToewijzingVerschil[] = []

    if (
        oud.processtap
        !== nieuw.processtap
    ) {
        verschillen.push({
            label: 'Processtap',
            oudeWaarde: oud.processtap,
            nieuweWaarde: nieuw.processtap,
        })
    }

    if (
        oud.betrokkenen
        !== nieuw.betrokkenen
    ) {
        verschillen.push({
            label: 'Betrokkenen',
            oudeWaarde: oud.betrokkenen,
            nieuweWaarde: nieuw.betrokkenen,
        })
    }

    if (
        oud.afdelingen
        !== nieuw.afdelingen
    ) {
        verschillen.push({
            label: 'Afdelingen',
            oudeWaarde: oud.afdelingen,
            nieuweWaarde: nieuw.afdelingen,
        })
    }

    if (
        oud.verantwoordelijke
        !== nieuw.verantwoordelijke
    ) {
        verschillen.push({
            label: 'Verantwoordelijke',
            oudeWaarde: oud.verantwoordelijke,
            nieuweWaarde: nieuw.verantwoordelijke,
        })
    }

    return verschillen
}

export function AfspraakDetailPage() {
    const { id } =
        useParams<{ id: string }>()

    const {
        gebruiker,
    } = useAuth()

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

    const actieveProcesstap =
        afspraak.processtappen.find(
            (processtap) =>
                processtap.status !== 'AFGEROND',
        )

    const laatsteProcesstap =
        afspraak.processtappen[
        afspraak.processtappen.length - 1
            ]

    const huidigeVerantwoordelijke =
        actieveProcesstap?.verantwoordelijke
        ?? laatsteProcesstap?.verantwoordelijke
        ?? null

    const magBewerken =
        gebruiker !== null
        && (
            gebruiker.id
            === afspraak.initiatiefnemer.id
            || gebruiker.id
            === huidigeVerantwoordelijke?.id
        )

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

                <div className="status-change-controls">
                    {magBewerken && (
                        <Link
                            to={
                                `/afspraken/${afspraak.id}/bewerken`
                            }
                            className="secondary-link-button"
                        >
                            Afspraak bewerken
                        </Link>
                    )}

                    <span
                        className={
                            `status-badge status-${afspraak.status.toLowerCase()}`
                        }
                    >
                        {statusLabel(
                            afspraak.status,
                        )}
                    </span>
                </div>
            </header>

            <AfspraakStatusSection
                afspraak={afspraak}
                onAfspraakGewijzigd={setAfspraak}
            />

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
                        <h3>
                            Reden
                        </h3>

                        <p>
                            {afspraak.reden
                                || 'Niet ingevuld'}
                        </p>
                    </div>

                    <div className="detail-text-block">
                        <h3>
                            Achtergrond
                        </h3>

                        <p>
                            {afspraak.achtergrond
                                || 'Niet ingevuld'}
                        </p>
                    </div>

                    <div className="detail-text-block">
                        <h3>
                            Aannames
                        </h3>

                        <p>
                            {afspraak.aannames
                                || 'Niet ingevuld'}
                        </p>
                    </div>
                </article>
            </section>

            <TypespecifiekeDetailsSection
                afspraak={afspraak}
                onAfspraakGewijzigd={setAfspraak}
            />

            <ToewijzingSection
                afspraak={afspraak}
                onAfspraakGewijzigd={setAfspraak}
            />

            <ProcesstappenSection
                afspraak={afspraak}
                onAfspraakGewijzigd={setAfspraak}
            />

            <BevestigingActiesSection
                afspraak={afspraak}
                onAfspraakGewijzigd={setAfspraak}
            />

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
                                            (
                                                betrokkenGebruiker,
                                            ) => (
                                                <li
                                                    key={
                                                        betrokkenGebruiker
                                                            .id
                                                    }
                                                >
                                                    <strong>
                                                        {
                                                            betrokkenGebruiker
                                                                .naam
                                                        }
                                                    </strong>

                                                    <span>
                                                        {
                                                            betrokkenGebruiker
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

                    {afspraak.goedkeurders.length === 0
                        ? (
                            <p>
                                Geen goedkeurders aangewezen.
                            </p>
                        )
                        : (
                            <ul className="simple-list">
                                {afspraak.goedkeurders.map(
                                    (goedkeurder) => {
                                        const bevestiging =
                                            afspraak.bevestigingen.find(
                                                (item) =>
                                                    item.gebruiker.id
                                                    === goedkeurder.id,
                                            )

                                        return (
                                            <li
                                                key={
                                                    goedkeurder.id
                                                }
                                            >
                                                <strong>
                                                    {
                                                        goedkeurder
                                                            .naam
                                                    }
                                                </strong>

                                                <span>
                                    {bevestiging?.gezien
                                        ? 'Gezien'
                                        : 'Nog niet gezien'}

                                                    {' · '}

                                                    {bevestiging === undefined
                                                        ? 'Nog geen beslissing'
                                                        : beslissingLabel(
                                                            bevestiging
                                                                .beslissing,
                                                        )}
                                </span>
                                            </li>
                                        )
                                    },
                                )}
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

                {afspraak.wijzigingen.length === 0
                    ? (
                        <div className="content-card">
                            <p>
                                Nog geen wijzigingen geregistreerd.
                            </p>
                        </div>
                    )
                    : (
                        <div className="history-list">
                            {
                                afspraak.wijzigingen
                                    .map(
                                        (wijziging) => {

                                            const toewijzingVerschillen =
                                                wijziging.onderdeel
                                                === 'Toewijzing'
                                                    ? bepaalToewijzingVerschillen(
                                                        wijziging.oudeWaarde,
                                                        wijziging.nieuweWaarde,
                                                    )
                                                    : null

                                            return (
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

                                                        {
                                                            toewijzingVerschillen
                                                            !== null
                                                            && toewijzingVerschillen
                                                                .length > 0
                                                                ? (
                                                                    <div>
                                                                        {
                                                                            toewijzingVerschillen
                                                                                .map(
                                                                                    (
                                                                                        verschil,
                                                                                    ) => (
                                                                                        <p
                                                                                            key={
                                                                                                verschil.label
                                                                                            }
                                                                                        >
                                                                                            <strong>
                                                                                                {
                                                                                                    verschil.label
                                                                                                }
                                                                                                :
                                                                                            </strong>
                                                                                            {' '}
                                                                                            {
                                                                                                verschil
                                                                                                    .oudeWaarde
                                                                                            }
                                                                                            {' → '}
                                                                                            {
                                                                                                verschil
                                                                                                    .nieuweWaarde
                                                                                            }
                                                                                        </p>
                                                                                    ),
                                                                                )
                                                                        }
                                                                    </div>
                                                                )
                                                                : (
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
                                                                )
                                                        }
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
                                            )
                                        },
                                    )
                            }
                        </div>
                    )}
            </section>
        </main>
    )
}