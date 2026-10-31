import {
    useState,
} from 'react'

import {
    statusLabel,
    typeLabel,
} from '../utils/afspraak'

import type {
    FormEvent,
} from 'react'

import type {
    Afdeling,
    AfspraakGebruiker,
    AfspraakStatus,
    AfspraakType,
} from '../types/afspraak'

import type {
    AfspraakFilterParams,
} from '../api/afspraken'

interface AfsprakenFiltersProps {
    weergave: 'actief' | 'archief'
    gebruikers: AfspraakGebruiker[]
    afdelingen: Afdeling[]
    bezig: boolean
    onZoeken: (
        zoekterm: string,
    ) => Promise<void>
    onFilteren: (
        filters: AfspraakFilterParams,
    ) => Promise<void>
    onReset: () => Promise<void>
}

export function AfsprakenFilters({
                                     weergave,
                                     gebruikers,
                                     afdelingen,
                                     bezig,
                                     onZoeken,
                                     onFilteren,
                                     onReset,
                                 }: AfsprakenFiltersProps) {
    const [
        zoekterm,
        setZoekterm,
    ] = useState('')

    const [
        type,
        setType,
    ] = useState<AfspraakType | ''>('')

    const [
        status,
        setStatus,
    ] = useState<
        Exclude<AfspraakStatus, 'AFGEROND'> | ''
    >('')

    const [
        verantwoordelijkeId,
        setVerantwoordelijkeId,
    ] = useState('')

    const [
        afdelingId,
        setAfdelingId,
    ] = useState('')

    const [
        fabriek,
        setFabriek,
    ] = useState('')

    const [
        periodeVan,
        setPeriodeVan,
    ] = useState('')

    const [
        periodeTot,
        setPeriodeTot,
    ] = useState('')

    async function handleZoeken(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const waarde =
            zoekterm.trim()

        if (!waarde) {
            await onReset()
            return
        }

        setType('')
        setStatus('')
        setVerantwoordelijkeId('')
        setAfdelingId('')
        setFabriek('')
        setPeriodeVan('')
        setPeriodeTot('')

        await onZoeken(
            waarde,
        )
    }

    async function handleFilteren(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        setZoekterm('')

        await onFilteren({
            type:
                type || undefined,

            status:
                weergave === 'archief'
                    ? 'AFGEROND'
                    : status || undefined,

            verantwoordelijkeId:
                verantwoordelijkeId || undefined,

            afdelingId:
                afdelingId || undefined,

            fabriek:
                fabriek || undefined,

            periodeVan:
                periodeVan || undefined,

            periodeTot:
                periodeTot || undefined,
        })
    }

    async function handleReset() {
        setZoekterm('')
        setType('')
        setStatus('')
        setVerantwoordelijkeId('')
        setAfdelingId('')
        setFabriek('')
        setPeriodeVan('')
        setPeriodeTot('')

        await onReset()
    }

    return (
        <section className="content-card filters-card">
            <div className="filters-heading">
                <div>
                    <h2>
                        Zoeken en filteren
                    </h2>

                    <p>
                        Zoek op titel, beschrijving of trefwoord,
                        of gebruik de filters.
                    </p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    disabled={bezig}
                    onClick={() =>
                        void handleReset()
                    }
                >
                    Wis filters
                </button>
            </div>

            <form
                className="search-form"
                onSubmit={(event) =>
                    void handleZoeken(event)
                }
            >
                <label htmlFor="afspraak-zoeken">
                    Zoeken
                </label>

                <div className="search-controls">
                    <input
                        id="afspraak-zoeken"
                        type="search"
                        value={zoekterm}
                        placeholder="Bijvoorbeeld capaciteit, planning..."
                        disabled={bezig}
                        onChange={(event) =>
                            setZoekterm(
                                event.target.value,
                            )
                        }
                    />

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={bezig}
                    >
                        Zoeken
                    </button>
                </div>
            </form>

            <form
                className="filters-form"
                onSubmit={(event) =>
                    void handleFilteren(event)
                }
            >
                <div className="filters-grid">
                    <label className="form-field">
                        <span>Type</span>

                        <select
                            value={type}
                            disabled={bezig}
                            onChange={(event) =>
                                setType(
                                    event.target.value as AfspraakType | '',
                                )
                            }
                        >
                            <option value="">
                                Alle typen
                            </option>

                            <option value="ALGEMEEN">
                                {typeLabel('ALGEMEEN')}
                            </option>

                            <option value="CAPACITEITSWISSEL">
                                {typeLabel('CAPACITEITSWISSEL')}
                            </option>

                            <option value="ORDERVERPLAATSING">
                                {typeLabel('ORDERVERPLAATSING')}
                            </option>
                        </select>
                    </label>

                    {weergave === 'actief' && (
                        <label className="form-field">
                            <span>Status</span>

                            <select
                                value={status}
                                disabled={bezig}
                                onChange={(event) =>
                                    setStatus(
                                        event.target.value as
                                            Exclude<
                                                AfspraakStatus,
                                                'AFGEROND'
                                            >
                                            | '',
                                    )
                                }
                            >
                                <option value="">
                                    Alle actieve statussen
                                </option>

                                <option value="OPEN">
                                    {statusLabel('OPEN')}
                                </option>

                                <option value="IN_BEHANDELING">
                                    {statusLabel('IN_BEHANDELING')}
                                </option>
                            </select>
                        </label>
                    )}

                    <label className="form-field">
                        <span>Verantwoordelijke</span>

                        <select
                            value={verantwoordelijkeId}
                            disabled={bezig}
                            onChange={(event) =>
                                setVerantwoordelijkeId(
                                    event.target.value,
                                )
                            }
                        >
                            <option value="">
                                Alle verantwoordelijken
                            </option>

                            {gebruikers.map(
                                (gebruiker) => (
                                    <option
                                        key={gebruiker.id}
                                        value={gebruiker.id}
                                    >
                                        {gebruiker.naam}
                                    </option>
                                ),
                            )}
                        </select>
                    </label>

                    <label className="form-field">
                        <span>Afdeling</span>

                        <select
                            value={afdelingId}
                            disabled={bezig}
                            onChange={(event) =>
                                setAfdelingId(
                                    event.target.value,
                                )
                            }
                        >
                            <option value="">
                                Alle afdelingen
                            </option>

                            {afdelingen.map(
                                (afdeling) => (
                                    <option
                                        key={afdeling.id}
                                        value={afdeling.id}
                                    >
                                        {afdeling.naam}
                                    </option>
                                ),
                            )}
                        </select>
                    </label>

                    <label className="form-field">
                        <span>Fabriek</span>

                        <input
                            type="text"
                            value={fabriek}
                            placeholder="Bijvoorbeeld Tornio"
                            disabled={bezig}
                            onChange={(event) =>
                                setFabriek(
                                    event.target.value,
                                )
                            }
                        />
                    </label>

                    <label className="form-field">
                        <span>Periode vanaf</span>

                        <input
                            type="date"
                            value={periodeVan}
                            disabled={bezig}
                            onChange={(event) =>
                                setPeriodeVan(
                                    event.target.value,
                                )
                            }
                        />
                    </label>

                    <label className="form-field">
                        <span>Periode tot</span>

                        <input
                            type="date"
                            value={periodeTot}
                            disabled={bezig}
                            onChange={(event) =>
                                setPeriodeTot(
                                    event.target.value,
                                )
                            }
                        />
                    </label>
                </div>

                <div className="filter-actions">
                    <button
                        type="submit"
                        className="primary-button"
                        disabled={bezig}
                    >
                        {bezig
                            ? 'Laden...'
                            : 'Filters toepassen'}
                    </button>
                </div>
            </form>
        </section>
    )
}