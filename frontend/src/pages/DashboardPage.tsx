import {
    useEffect,
    useState,
} from 'react'

import {
    useNavigate,
} from 'react-router-dom'

import {
    filterAfspraken,
    getActieveAfspraken,
    getArchiefAfspraken,
    zoekAfspraken,
} from '../api/afspraken'

import {
    getGebruikers,
} from '../api/gebruikers'

import {
    getAfdelingen,
} from '../api/afdelingen'

import {
    AfsprakenFilters,
} from '../components/AfsprakenFilters'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    Afdeling,
    AfspraakGebruiker,
    AfspraakStatus,
    AfspraakSummary,
    AfspraakType,
} from '../types/afspraak'

import type {
    AfspraakFilterParams,
} from '../api/afspraken'

type OverzichtWeergave =
    | 'actief'
    | 'archief'

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

function filterOpWeergave(
    afspraken: AfspraakSummary[],
    weergave: OverzichtWeergave,
) {
    if (weergave === 'archief') {
        return afspraken.filter(
            (afspraak) =>
                afspraak.status === 'AFGEROND',
        )
    }

    return afspraken.filter(
        (afspraak) =>
            afspraak.status !== 'AFGEROND',
    )
}

export function DashboardPage() {
    const navigate =
        useNavigate()

    const {
        gebruiker,
        logout,
    } = useAuth()

    const [
        afspraken,
        setAfspraken,
    ] = useState<AfspraakSummary[]>([])

    const [
        gebruikers,
        setGebruikers,
    ] = useState<AfspraakGebruiker[]>([])

    const [
        afdelingen,
        setAfdelingen,
    ] = useState<Afdeling[]>([])

    const [
        weergave,
        setWeergave,
    ] = useState<OverzichtWeergave>('actief')

    const [
        laden,
        setLaden,
    ] = useState(true)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    useEffect(() => {
        async function laadDashboard() {
            try {
                setLaden(true)
                setFoutmelding(null)

                const [
                    afsprakenResponse,
                    gebruikersResponse,
                    afdelingenResponse,
                ] = await Promise.all([
                    getActieveAfspraken(),
                    getGebruikers(),
                    getAfdelingen(),
                ])

                setAfspraken(
                    afsprakenResponse,
                )

                setGebruikers(
                    gebruikersResponse,
                )

                setAfdelingen(
                    afdelingenResponse,
                )
            } catch (error) {
                setFoutmelding(
                    error instanceof Error
                        ? error.message
                        : 'Dashboard kon niet worden geladen',
                )
            } finally {
                setLaden(false)
            }
        }

        void laadDashboard()
    }, [])

    if (gebruiker === null) {
        return null
    }

    async function laadBasisOverzicht(
        nieuweWeergave: OverzichtWeergave,
    ) {
        try {
            setLaden(true)
            setFoutmelding(null)

            const response =
                nieuweWeergave === 'actief'
                    ? await getActieveAfspraken()
                    : await getArchiefAfspraken()

            setAfspraken(
                response,
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Afspraken konden niet worden geladen',
            )
        } finally {
            setLaden(false)
        }
    }

    async function wijzigWeergave(
        nieuweWeergave: OverzichtWeergave,
    ) {
        setWeergave(
            nieuweWeergave,
        )

        await laadBasisOverzicht(
            nieuweWeergave,
        )
    }

    async function handleZoeken(
        zoekterm: string,
    ) {
        try {
            setLaden(true)
            setFoutmelding(null)

            const response =
                await zoekAfspraken(
                    zoekterm,
                )

            setAfspraken(
                filterOpWeergave(
                    response,
                    weergave,
                ),
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Zoeken is mislukt',
            )
        } finally {
            setLaden(false)
        }
    }

    async function handleFilteren(
        filters: AfspraakFilterParams,
    ) {
        try {
            setLaden(true)
            setFoutmelding(null)

            const response =
                await filterAfspraken(
                    filters,
                )

            setAfspraken(
                filterOpWeergave(
                    response,
                    weergave,
                ),
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Filteren is mislukt',
            )
        } finally {
            setLaden(false)
        }
    }

    async function handleReset() {
        await laadBasisOverzicht(
            weergave,
        )
    }

    function openAfspraak(
        id: string,
    ) {
        navigate(
            `/afspraken/${id}`,
        )
    }

    return (
        <main className="dashboard-page">
            <header className="dashboard-header">
                <div>
                    <p className="eyebrow">
                        Outokumpu
                    </p>

                    <h1>
                        Afsprakenmanagement
                    </h1>

                    <p className="dashboard-subtitle">
                        Operationele afspraken centraal
                        beheren en opvolgen.
                    </p>
                </div>

                <div className="user-actions">
                    <div className="current-user">
                        <strong>
                            {gebruiker.naam}
                        </strong>

                        <span>
                            {gebruiker.rol}
                        </span>
                    </div>

                    <button
                        type="button"
                        className="secondary-button"
                        onClick={() =>
                            void logout()
                        }
                    >
                        Uitloggen
                    </button>
                </div>
            </header>

            <section className="overview-header">
                <div>
                    <h2>
                        Afspraken
                    </h2>

                    <p>
                        {laden
                            ? 'Afspraken worden geladen...'
                            : `${afspraken.length} afspraak${
                                afspraken.length === 1
                                    ? ''
                                    : 'en'
                            } gevonden`}
                    </p>
                </div>

                <button
                    type="button"
                    className="primary-button"
                    onClick={() =>
                        navigate(
                            '/afspraken/nieuw',
                        )
                    }
                >
                    + Nieuwe afspraak
                </button>
            </section>

            <div className="overview-tabs">
                <button
                    type="button"
                    className={
                        weergave === 'actief'
                            ? 'overview-tab overview-tab-active'
                            : 'overview-tab'
                    }
                    disabled={laden}
                    onClick={() =>
                        void wijzigWeergave(
                            'actief',
                        )
                    }
                >
                    Actief
                </button>

                <button
                    type="button"
                    className={
                        weergave === 'archief'
                            ? 'overview-tab overview-tab-active'
                            : 'overview-tab'
                    }
                    disabled={laden}
                    onClick={() =>
                        void wijzigWeergave(
                            'archief',
                        )
                    }
                >
                    Archief
                </button>
            </div>

            <AfsprakenFilters
                key={weergave}
                weergave={weergave}
                gebruikers={gebruikers}
                afdelingen={afdelingen}
                bezig={laden}
                onZoeken={handleZoeken}
                onFilteren={handleFilteren}
                onReset={handleReset}
            />

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}

            {laden && (
                <section className="content-card">
                    <p>
                        Afspraken laden...
                    </p>
                </section>
            )}

            {!laden
                && foutmelding === null
                && afspraken.length === 0 && (
                    <section className="content-card empty-state">
                        <h3>
                            Geen afspraken gevonden
                        </h3>

                        <p>
                            Er zijn geen afspraken die
                            aan de huidige selectie voldoen.
                        </p>
                    </section>
                )}

            {!laden
                && foutmelding === null
                && afspraken.length > 0 && (
                    <section className="table-card">
                        <div className="table-wrapper">
                            <table className="afspraken-table">
                                <thead>
                                <tr>
                                    <th>Afspraak</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>Ingangsdatum</th>
                                    <th>Deadline</th>
                                    <th>Initiatiefnemer</th>
                                    <th>
                                        Laatst gewijzigd
                                    </th>
                                </tr>
                                </thead>

                                <tbody>
                                {afspraken.map(
                                    (afspraak) => (
                                        <tr
                                            key={afspraak.id}
                                            className="clickable-row"
                                            tabIndex={0}
                                            onClick={() =>
                                                openAfspraak(
                                                    afspraak.id,
                                                )
                                            }
                                            onKeyDown={(event) => {
                                                if (
                                                    event.key === 'Enter'
                                                    || event.key === ' '
                                                ) {
                                                    openAfspraak(
                                                        afspraak.id,
                                                    )
                                                }
                                            }}
                                        >
                                            <td>
                                                <strong>
                                                    {afspraak.titel}
                                                </strong>
                                            </td>

                                            <td>
                                                {typeLabel(
                                                    afspraak.type,
                                                )}
                                            </td>

                                            <td>
                                                    <span
                                                        className={
                                                            `status-badge status-${afspraak.status.toLowerCase()}`
                                                        }
                                                    >
                                                        {statusLabel(
                                                            afspraak.status,
                                                        )}
                                                    </span>
                                            </td>

                                            <td>
                                                {formatteerDatum(
                                                    afspraak.ingangsdatum,
                                                )}
                                            </td>

                                            <td>
                                                {formatteerDatum(
                                                    afspraak.deadline,
                                                )}
                                            </td>

                                            <td>
                                                <div className="user-cell">
                                                    <strong>
                                                        {
                                                            afspraak
                                                                .initiatiefnemer
                                                                .naam
                                                        }
                                                    </strong>

                                                    <span>
                                                            {
                                                                afspraak
                                                                    .initiatiefnemer
                                                                    .afdeling
                                                                    ?.naam
                                                                ?? 'Geen afdeling'
                                                            }
                                                        </span>
                                                </div>
                                            </td>

                                            <td>
                                                {formatteerDatumTijd(
                                                    afspraak
                                                        .laatstGewijzigdOp,
                                                )}
                                            </td>
                                        </tr>
                                    ),
                                )}
                                </tbody>
                            </table>
                        </div>
                    </section>
                )}
        </main>
    )
}