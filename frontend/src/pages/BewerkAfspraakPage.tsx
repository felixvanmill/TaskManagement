import {
    useEffect,
    useState,
} from 'react'

import {
    Link,
    useNavigate,
    useParams,
} from 'react-router-dom'

import {
    getAfspraak,
    updateAfspraak,
} from '../api/afspraken'

import type {
    UpdateAfspraakRequest,
} from '../api/afspraken'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    AfspraakDetail,
    AfspraakType,
} from '../types/afspraak'

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

export function BewerkAfspraakPage() {
    const { id } =
        useParams<{ id: string }>()

    const navigate =
        useNavigate()

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
        opslaan,
        setOpslaan,
    ] = useState(false)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    const [
        titel,
        setTitel,
    ] = useState('')

    const [
        beschrijving,
        setBeschrijving,
    ] = useState('')

    const [
        reden,
        setReden,
    ] = useState('')

    const [
        achtergrond,
        setAchtergrond,
    ] = useState('')

    const [
        aannames,
        setAannames,
    ] = useState('')

    const [
        ingangsdatum,
        setIngangsdatum,
    ] = useState('')

    const [
        deadline,
        setDeadline,
    ] = useState('')

    const [
        fabriek,
        setFabriek,
    ] = useState('')

    const [
        finishType,
        setFinishType,
    ] = useState('')

    const [
        grade,
        setGrade,
    ] = useState('')

    const [
        capaciteitVan,
        setCapaciteitVan,
    ] = useState('')

    const [
        capaciteitNaar,
        setCapaciteitNaar,
    ] = useState('')

    const [
        capaciteitEenheid,
        setCapaciteitEenheid,
    ] = useState('')

    const [
        periodeVan,
        setPeriodeVan,
    ] = useState('')

    const [
        periodeTot,
        setPeriodeTot,
    ] = useState('')

    const [
        vanFabriek,
        setVanFabriek,
    ] = useState('')

    const [
        naarFabriek,
        setNaarFabriek,
    ] = useState('')

    const [
        orderFinishType,
        setOrderFinishType,
    ] = useState('')

    const [
        totaalVolumeTons,
        setTotaalVolumeTons,
    ] = useState('')

    const [
        gewensteVerplaatsingsdatum,
        setGewensteVerplaatsingsdatum,
    ] = useState('')

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
                setLaden(true)
                setFoutmelding(null)

                const response =
                    await getAfspraak(id)

                setAfspraak(response)

                setTitel(
                    response.titel,
                )

                setBeschrijving(
                    response.beschrijving,
                )

                setReden(
                    response.reden ?? '',
                )

                setAchtergrond(
                    response.achtergrond ?? '',
                )

                setAannames(
                    response.aannames ?? '',
                )

                setIngangsdatum(
                    response.ingangsdatum,
                )

                setDeadline(
                    response.deadline,
                )

                if (
                    response.type
                    === 'CAPACITEITSWISSEL'
                    && response.capaciteitswissel !== null
                ) {
                    const gegevens =
                        response.capaciteitswissel

                    setFabriek(
                        gegevens.fabriek,
                    )

                    setFinishType(
                        gegevens.finishType,
                    )

                    setGrade(
                        gegevens.grade,
                    )

                    setCapaciteitVan(
                        String(
                            gegevens.capaciteitVan,
                        ),
                    )

                    setCapaciteitNaar(
                        String(
                            gegevens.capaciteitNaar,
                        ),
                    )

                    setCapaciteitEenheid(
                        gegevens.capaciteitEenheid,
                    )

                    setPeriodeVan(
                        gegevens.periodeVan,
                    )

                    setPeriodeTot(
                        gegevens.periodeTot,
                    )
                }

                if (
                    response.type
                    === 'ORDERVERPLAATSING'
                    && response.orderverplaatsing !== null
                ) {
                    const gegevens =
                        response.orderverplaatsing

                    setVanFabriek(
                        gegevens.vanFabriek,
                    )

                    setNaarFabriek(
                        gegevens.naarFabriek,
                    )

                    setOrderFinishType(
                        gegevens.finishType,
                    )

                    setTotaalVolumeTons(
                        String(
                            gegevens.totaalVolumeTons,
                        ),
                    )

                    setGewensteVerplaatsingsdatum(
                        gegevens
                            .gewensteVerplaatsingsdatum,
                    )
                }
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
        && afspraak === null
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
                    {foutmelding}
                </div>
            </main>
        )
    }

    if (afspraak === null) {
        return (
            <main className="dashboard-page">
                <Link
                    to="/"
                    className="back-link"
                >
                    ← Terug naar afspraken
                </Link>

                <div className="alert-error">
                    Afspraak niet gevonden
                </div>
            </main>
        )
    }

    const huidigeVerantwoordelijke =
        bepaalHuidigeVerantwoordelijke(
            afspraak,
        )

    const magWijzigen =
        gebruiker !== null
        && (
            gebruiker.id
            === afspraak.initiatiefnemer.id
            || gebruiker.id
            === huidigeVerantwoordelijke?.id
        )

    if (!magWijzigen) {
        return (
            <main className="dashboard-page">
                <Link
                    to={`/afspraken/${afspraak.id}`}
                    className="back-link"
                >
                    ← Terug naar afspraak
                </Link>

                <div className="alert-error">
                    Je bent niet bevoegd om deze afspraak
                    te wijzigen.
                </div>
            </main>
        )
    }

    async function handleSubmit(
        event: React.FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        if (afspraak === null) {
            setFoutmelding(
                'Afspraak is niet geladen.',
            )

            return
        }
        if (deadline < ingangsdatum) {
            setFoutmelding(
                'De deadline mag niet vóór de ingangsdatum liggen.',
            )

            return
        }

        if (
            afspraak?.type === 'CAPACITEITSWISSEL'
            && periodeTot < periodeVan
        ) {
            setFoutmelding(
                'De einddatum van de periode mag niet vóór de begindatum liggen.',
            )

            return
        }

        const request: UpdateAfspraakRequest = {
            titel: titel.trim(),
            beschrijving: beschrijving.trim(),
            reden: reden.trim(),
            achtergrond: achtergrond.trim(),
            aannames: aannames.trim(),
            ingangsdatum,
            deadline,
            capaciteitswissel:
                afspraak?.type === 'CAPACITEITSWISSEL'
                    ? {
                        fabriek:
                            fabriek.trim(),
                        finishType:
                            finishType.trim(),
                        grade:
                            grade.trim(),
                        capaciteitVan:
                            Number(capaciteitVan),
                        capaciteitNaar:
                            Number(capaciteitNaar),
                        capaciteitEenheid:
                            capaciteitEenheid.trim(),
                        periodeVan,
                        periodeTot,
                    }
                    : null,
            orderverplaatsing:
                afspraak?.type === 'ORDERVERPLAATSING'
                    ? {
                        vanFabriek:
                            vanFabriek.trim(),
                        naarFabriek:
                            naarFabriek.trim(),
                        finishType:
                            orderFinishType.trim(),
                        totaalVolumeTons:
                            Number(totaalVolumeTons),
                        gewensteVerplaatsingsdatum,
                        uitvoeringsstatus: null,
                    }
                    : null,
        }

        try {
            setOpslaan(true)
            setFoutmelding(null)

            const bijgewerkteAfspraak =
                await updateAfspraak(
                    afspraak.id,
                    request,
                )

            navigate(
                `/afspraken/${bijgewerkteAfspraak.id}`,
                {
                    replace: true,
                },
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Afspraak kon niet worden bijgewerkt',
            )
        } finally {
            setOpslaan(false)
        }
    }

    return (
        <main className="dashboard-page">
            <Link
                to={`/afspraken/${afspraak.id}`}
                className="back-link"
            >
                ← Terug naar afspraak
            </Link>

            <header className="detail-header">
                <div>
                    <p className="eyebrow">
                        {typeLabel(
                            afspraak.type,
                        )}
                    </p>

                    <h1>
                        Afspraak bewerken
                    </h1>

                    <p className="detail-description">
                        Wijzig de inhoud van de operationele
                        afspraak. Toewijzing en workflow worden
                        apart beheerd.
                    </p>
                </div>
            </header>

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}

            <form
                className="create-form"
                onSubmit={handleSubmit}
            >
                <section className="content-card form-section">
                    <h2>
                        Basisgegevens
                    </h2>

                    <div className="form-grid">
                        <label className="form-field form-field-wide">
                            <span>
                                Titel *
                            </span>

                            <input
                                type="text"
                                value={titel}
                                onChange={(event) =>
                                    setTitel(
                                        event.target.value,
                                    )
                                }
                                maxLength={255}
                                required
                            />
                        </label>

                        <label className="form-field">
                            <span>
                                Type
                            </span>

                            <input
                                type="text"
                                value={
                                    typeLabel(
                                        afspraak.type,
                                    )
                                }
                                disabled
                            />
                        </label>

                        <label className="form-field">
                            <span>
                                Ingangsdatum *
                            </span>

                            <input
                                type="date"
                                value={ingangsdatum}
                                onChange={(event) =>
                                    setIngangsdatum(
                                        event.target.value,
                                    )
                                }
                                required
                            />
                        </label>

                        <label className="form-field">
                            <span>
                                Deadline *
                            </span>

                            <input
                                type="date"
                                value={deadline}
                                onChange={(event) =>
                                    setDeadline(
                                        event.target.value,
                                    )
                                }
                                required
                            />
                        </label>

                        <label className="form-field form-field-wide">
                            <span>
                                Beschrijving *
                            </span>

                            <textarea
                                value={beschrijving}
                                onChange={(event) =>
                                    setBeschrijving(
                                        event.target.value,
                                    )
                                }
                                maxLength={5000}
                                rows={5}
                                required
                            />
                        </label>

                        <label className="form-field form-field-wide">
                            <span>
                                Reden *
                            </span>

                            <textarea
                                value={reden}
                                onChange={(event) =>
                                    setReden(
                                        event.target.value,
                                    )
                                }
                                maxLength={2000}
                                rows={3}
                                required
                            />
                        </label>

                        <label className="form-field form-field-wide">
                            <span>
                                Achtergrond
                            </span>

                            <textarea
                                value={achtergrond}
                                onChange={(event) =>
                                    setAchtergrond(
                                        event.target.value,
                                    )
                                }
                                maxLength={5000}
                                rows={3}
                            />
                        </label>

                        <label className="form-field form-field-wide">
                            <span>
                                Aannames
                            </span>

                            <textarea
                                value={aannames}
                                onChange={(event) =>
                                    setAannames(
                                        event.target.value,
                                    )
                                }
                                maxLength={5000}
                                rows={3}
                            />
                        </label>
                    </div>
                </section>

                {afspraak.type === 'CAPACITEITSWISSEL' && (
                    <section className="content-card form-section">
                        <h2>
                            Capaciteitswissel
                        </h2>

                        <div className="form-grid">
                            <label className="form-field">
                                <span>
                                    Fabriek *
                                </span>

                                <input
                                    type="text"
                                    value={fabriek}
                                    onChange={(event) =>
                                        setFabriek(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={100}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Finish type *
                                </span>

                                <input
                                    type="text"
                                    value={finishType}
                                    onChange={(event) =>
                                        setFinishType(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={100}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Grade *
                                </span>

                                <input
                                    type="text"
                                    value={grade}
                                    onChange={(event) =>
                                        setGrade(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={100}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Capaciteitseenheid *
                                </span>

                                <input
                                    type="text"
                                    value={capaciteitEenheid}
                                    onChange={(event) =>
                                        setCapaciteitEenheid(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={50}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Capaciteit van *
                                </span>

                                <input
                                    type="number"
                                    step="any"
                                    value={capaciteitVan}
                                    onChange={(event) =>
                                        setCapaciteitVan(
                                            event.target.value,
                                        )
                                    }
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Capaciteit naar *
                                </span>

                                <input
                                    type="number"
                                    step="any"
                                    value={capaciteitNaar}
                                    onChange={(event) =>
                                        setCapaciteitNaar(
                                            event.target.value,
                                        )
                                    }
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Periode van *
                                </span>

                                <input
                                    type="date"
                                    value={periodeVan}
                                    onChange={(event) =>
                                        setPeriodeVan(
                                            event.target.value,
                                        )
                                    }
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Periode tot *
                                </span>

                                <input
                                    type="date"
                                    value={periodeTot}
                                    onChange={(event) =>
                                        setPeriodeTot(
                                            event.target.value,
                                        )
                                    }
                                    required
                                />
                            </label>
                        </div>
                    </section>
                )}

                {afspraak.type === 'ORDERVERPLAATSING' && (
                    <section className="content-card form-section">
                        <h2>
                            Orderverplaatsing
                        </h2>

                        <div className="form-grid">
                            <label className="form-field">
                                <span>
                                    Van fabriek *
                                </span>

                                <input
                                    type="text"
                                    value={vanFabriek}
                                    onChange={(event) =>
                                        setVanFabriek(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={100}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Naar fabriek *
                                </span>

                                <input
                                    type="text"
                                    value={naarFabriek}
                                    onChange={(event) =>
                                        setNaarFabriek(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={100}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Finish type *
                                </span>

                                <input
                                    type="text"
                                    value={orderFinishType}
                                    onChange={(event) =>
                                        setOrderFinishType(
                                            event.target.value,
                                        )
                                    }
                                    maxLength={100}
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Totaal volume (tons) *
                                </span>

                                <input
                                    type="number"
                                    step="any"
                                    value={totaalVolumeTons}
                                    onChange={(event) =>
                                        setTotaalVolumeTons(
                                            event.target.value,
                                        )
                                    }
                                    required
                                />
                            </label>

                            <label className="form-field">
                                <span>
                                    Gewenste verplaatsingsdatum *
                                </span>

                                <input
                                    type="date"
                                    value={
                                        gewensteVerplaatsingsdatum
                                    }
                                    onChange={(event) =>
                                        setGewensteVerplaatsingsdatum(
                                            event.target.value,
                                        )
                                    }
                                    required
                                />
                            </label>
                        </div>
                    </section>
                )}

                <div className="form-actions">
                    <Link
                        to={`/afspraken/${afspraak.id}`}
                        className="secondary-link-button"
                    >
                        Annuleren
                    </Link>

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={opslaan}
                    >
                        {opslaan
                            ? 'Wijzigingen opslaan...'
                            : 'Wijzigingen opslaan'}
                    </button>
                </div>
            </form>
        </main>
    )
}