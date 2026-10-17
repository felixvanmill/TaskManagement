import {
    useEffect,
    useState,
} from 'react'

import {
    getAfspraken,
} from '../api/afspraken'

import { useAuth } from '../auth/useAuth'

import type {
    AfspraakStatus,
    AfspraakSummary,
    AfspraakType,
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

export function DashboardPage() {
    const {
        gebruiker,
        logout,
    } = useAuth()

    const [
        afspraken,
        setAfspraken,
    ] = useState<AfspraakSummary[]>([])

    const [
        laden,
        setLaden,
    ] = useState(true)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    useEffect(() => {
        async function laadAfspraken() {
            try {
                setLaden(true)
                setFoutmelding(null)

                const response =
                    await getAfspraken()

                setAfspraken(response)
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

        void laadAfspraken()
    }, [])

    if (gebruiker === null) {
        return null
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
                        onClick={() => void logout()}
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
            </section>

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
                            Nog geen afspraken
                        </h3>

                        <p>
                            Er zijn momenteel geen
                            operationele afspraken beschikbaar.
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
                                        <tr key={afspraak.id}>
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