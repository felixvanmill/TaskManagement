import {
    useEffect,
    useState,
} from 'react'

import {
    Link,
    useNavigate,
} from 'react-router-dom'

import {
    getAfdelingen,
} from '../api/afdelingen'

import {
    createAfspraak,
} from '../api/afspraken'

import {
    getGebruikers,
} from '../api/gebruikers'

import type {
    Afdeling,
    AfspraakGebruiker,
    AfspraakType,
    CreateAfspraakRequest,
} from '../types/afspraak'

function wijzigSelectie(
    huidigeIds: string[],
    id: string,
) {
    if (huidigeIds.includes(id)) {
        return huidigeIds.filter(
            (huidigId) => huidigId !== id,
        )
    }

    return [
        ...huidigeIds,
        id,
    ]
}

export function NieuweAfspraakPage() {
    const navigate =
        useNavigate()

    const [
        gebruikers,
        setGebruikers,
    ] = useState<AfspraakGebruiker[]>([])

    const [
        afdelingen,
        setAfdelingen,
    ] = useState<Afdeling[]>([])

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

    const [titel, setTitel] =
        useState('')

    const [beschrijving, setBeschrijving] =
        useState('')

    const [reden, setReden] =
        useState('')

    const [achtergrond, setAchtergrond] =
        useState('')

    const [aannames, setAannames] =
        useState('')

    const [type, setType] =
        useState<AfspraakType>('ALGEMEEN')

    const [ingangsdatum, setIngangsdatum] =
        useState('')

    const [deadline, setDeadline] =
        useState('')

    const [
        verantwoordelijkeId,
        setVerantwoordelijkeId,
    ] = useState('')

    const [
        eersteProcesstapNaam,
        setEersteProcesstapNaam,
    ] = useState('')

    const [
        eersteProcesstapDeadline,
        setEersteProcesstapDeadline,
    ] = useState('')

    const [
        betrokkeneIds,
        setBetrokkeneIds,
    ] = useState<string[]>([])

    const [
        goedkeurderIds,
        setGoedkeurderIds,
    ] = useState<string[]>([])

    const [
        afdelingIds,
        setAfdelingIds,
    ] = useState<string[]>([])

    const [fabriek, setFabriek] =
        useState('')

    const [finishType, setFinishType] =
        useState('')

    const [grade, setGrade] =
        useState('')

    const [capaciteitVan, setCapaciteitVan] =
        useState('')

    const [capaciteitNaar, setCapaciteitNaar] =
        useState('')

    const [
        capaciteitEenheid,
        setCapaciteitEenheid,
    ] = useState('tons')

    const [periodeVan, setPeriodeVan] =
        useState('')

    const [periodeTot, setPeriodeTot] =
        useState('')

    const [vanFabriek, setVanFabriek] =
        useState('')

    const [naarFabriek, setNaarFabriek] =
        useState('')

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
        async function laadKeuzes() {
            try {
                setLaden(true)
                setFoutmelding(null)

                const [
                    gebruikersResponse,
                    afdelingenResponse,
                ] = await Promise.all([
                    getGebruikers(),
                    getAfdelingen(),
                ])

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
                        : 'Keuzelijsten konden niet worden geladen',
                )
            } finally {
                setLaden(false)
            }
        }

        void laadKeuzes()
    }, [])

    async function handleSubmit(
        event: React.FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        if (deadline < ingangsdatum) {
            setFoutmelding(
                'De deadline mag niet vóór de ingangsdatum liggen.',
            )

            return
        }

        if (
            type === 'CAPACITEITSWISSEL'
            && periodeTot < periodeVan
        ) {
            setFoutmelding(
                'De einddatum van de periode mag niet vóór de begindatum liggen.',
            )

            return
        }

        const request: CreateAfspraakRequest = {
            titel: titel.trim(),
            beschrijving: beschrijving.trim(),
            reden: reden.trim(),
            achtergrond: achtergrond.trim(),
            aannames: aannames.trim(),
            type,
            ingangsdatum,
            deadline,
            verantwoordelijkeId,
            eersteProcesstapNaam:
                eersteProcesstapNaam.trim(),
            eersteProcesstapDeadline,
            betrokkeneIds,
            goedkeurderIds,
            afdelingIds,
            capaciteitswissel:
                type === 'CAPACITEITSWISSEL'
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
                type === 'ORDERVERPLAATSING'
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

            const nieuweAfspraak =
                await createAfspraak(
                    request,
                )

            navigate(
                `/afspraken/${nieuweAfspraak.id}`,
                {
                    replace: true,
                },
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Afspraak kon niet worden aangemaakt',
            )
        } finally {
            setOpslaan(false)
        }
    }

    if (laden) {
        return (
            <main className="dashboard-page">
                <p>
                    Formulier laden...
                </p>
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
                        Nieuwe afspraak
                    </p>

                    <h1>
                        Operationele afspraak aanmaken
                    </h1>

                    <p className="detail-description">
                        Leg de afspraak, betrokkenen en
                        eerste processtap vast.
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
                            <span>Titel *</span>

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
                            <span>Type *</span>

                            <select
                                value={type}
                                onChange={(event) =>
                                    setType(
                                        event.target.value as AfspraakType,
                                    )
                                }
                                required
                            >
                                <option value="ALGEMEEN">
                                    Algemeen
                                </option>

                                <option value="CAPACITEITSWISSEL">
                                    Capaciteitswissel
                                </option>

                                <option value="ORDERVERPLAATSING">
                                    Orderverplaatsing
                                </option>
                            </select>
                        </label>

                        <label className="form-field">
                            <span>Ingangsdatum *</span>

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
                            <span>Deadline *</span>

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
                            <span>Beschrijving *</span>

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
                            <span>Reden *</span>

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
                            <span>Achtergrond</span>

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
                            <span>Aannames</span>

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

                {type === 'CAPACITEITSWISSEL' && (
                    <section className="content-card form-section">
                        <h2>
                            Capaciteitswissel
                        </h2>

                        <div className="form-grid">
                            <label className="form-field">
                                <span>Fabriek *</span>

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
                                <span>Finish type *</span>

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
                                <span>Grade *</span>

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
                                <span>Eenheid *</span>

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
                                <span>Capaciteit van *</span>

                                <input
                                    type="number"
                                    step="0.01"
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
                                <span>Capaciteit naar *</span>

                                <input
                                    type="number"
                                    step="0.01"
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
                                <span>Periode van *</span>

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
                                <span>Periode tot *</span>

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

                {type === 'ORDERVERPLAATSING' && (
                    <section className="content-card form-section">
                        <h2>
                            Orderverplaatsing
                        </h2>

                        <div className="form-grid">
                            <label className="form-field">
                                <span>Van fabriek *</span>

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
                                <span>Naar fabriek *</span>

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
                                <span>Finish type *</span>

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
                                <span>Totaal volume (tons) *</span>

                                <input
                                    type="number"
                                    step="0.01"
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

                <section className="content-card form-section">
                    <h2>
                        Eerste processtap
                    </h2>

                    <div className="form-grid">
                        <label className="form-field">
                            <span>Naam *</span>

                            <input
                                type="text"
                                value={eersteProcesstapNaam}
                                onChange={(event) =>
                                    setEersteProcesstapNaam(
                                        event.target.value,
                                    )
                                }
                                maxLength={255}
                                required
                            />
                        </label>

                        <label className="form-field">
                            <span>Deadline *</span>

                            <input
                                type="date"
                                value={eersteProcesstapDeadline}
                                onChange={(event) =>
                                    setEersteProcesstapDeadline(
                                        event.target.value,
                                    )
                                }
                                required
                            />
                        </label>

                        <label className="form-field form-field-wide">
                            <span>Verantwoordelijke *</span>

                            <select
                                value={verantwoordelijkeId}
                                onChange={(event) =>
                                    setVerantwoordelijkeId(
                                        event.target.value,
                                    )
                                }
                                required
                            >
                                <option value="">
                                    Kies een gebruiker
                                </option>

                                {gebruikers.map(
                                    (gebruiker) => (
                                        <option
                                            key={gebruiker.id}
                                            value={gebruiker.id}
                                        >
                                            {gebruiker.naam}
                                            {' — '}
                                            {gebruiker.email}
                                        </option>
                                    ),
                                )}
                            </select>
                        </label>
                    </div>
                </section>

                <section className="content-card form-section">
                    <h2>
                        Betrokkenen
                    </h2>

                    <div className="selection-grid">
                        {gebruikers.map(
                            (gebruiker) => (
                                <label
                                    key={gebruiker.id}
                                    className="selection-option"
                                >
                                    <input
                                        type="checkbox"
                                        checked={
                                            betrokkeneIds
                                                .includes(
                                                    gebruiker.id,
                                                )
                                        }
                                        onChange={() =>
                                            setBetrokkeneIds(
                                                (huidige) =>
                                                    wijzigSelectie(
                                                        huidige,
                                                        gebruiker.id,
                                                    ),
                                            )
                                        }
                                    />

                                    <span>
                                        <strong>
                                            {gebruiker.naam}
                                        </strong>

                                        <small>
                                            {gebruiker.afdeling
                                                    ?.naam
                                                ?? 'Geen afdeling'}
                                        </small>
                                    </span>
                                </label>
                            ),
                        )}
                    </div>
                </section>

                <section className="content-card form-section">
                    <h2>
                        Goedkeurders
                    </h2>

                    <div className="selection-grid">
                        {gebruikers.map(
                            (gebruiker) => (
                                <label
                                    key={gebruiker.id}
                                    className="selection-option"
                                >
                                    <input
                                        type="checkbox"
                                        checked={
                                            goedkeurderIds
                                                .includes(
                                                    gebruiker.id,
                                                )
                                        }
                                        onChange={() =>
                                            setGoedkeurderIds(
                                                (huidige) =>
                                                    wijzigSelectie(
                                                        huidige,
                                                        gebruiker.id,
                                                    ),
                                            )
                                        }
                                    />

                                    <span>
                                        <strong>
                                            {gebruiker.naam}
                                        </strong>

                                        <small>
                                            {gebruiker.email}
                                        </small>
                                    </span>
                                </label>
                            ),
                        )}
                    </div>
                </section>

                <section className="content-card form-section">
                    <h2>
                        Betrokken afdelingen
                    </h2>

                    <div className="selection-grid">
                        {afdelingen.map(
                            (afdeling) => (
                                <label
                                    key={afdeling.id}
                                    className="selection-option"
                                >
                                    <input
                                        type="checkbox"
                                        checked={
                                            afdelingIds
                                                .includes(
                                                    afdeling.id,
                                                )
                                        }
                                        onChange={() =>
                                            setAfdelingIds(
                                                (huidige) =>
                                                    wijzigSelectie(
                                                        huidige,
                                                        afdeling.id,
                                                    ),
                                            )
                                        }
                                    />

                                    <span>
                                        <strong>
                                            {afdeling.naam}
                                        </strong>
                                    </span>
                                </label>
                            ),
                        )}
                    </div>
                </section>

                <div className="form-actions">
                    <Link
                        to="/"
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
                            ? 'Afspraak opslaan...'
                            : 'Afspraak aanmaken'}
                    </button>
                </div>
            </form>
        </main>
    )
}