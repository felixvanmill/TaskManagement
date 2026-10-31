import {
    useEffect,
    useState,
} from 'react'

import {
    bepaalActieveProcesstap,
    bepaalHuidigeVerantwoordelijke,
} from '../utils/afspraak'

import {
    getAfdelingen,
} from '../api/afdelingen'

import {
    getGebruikers,
} from '../api/gebruikers'

import {
    wijzigToewijzing,
} from '../api/workflow'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    Afdeling,
    AfspraakDetail,
    AfspraakGebruiker,
} from '../types/afspraak'

interface ToewijzingSectionProps {
    afspraak: AfspraakDetail
    onAfspraakGewijzigd: (
        afspraak: AfspraakDetail,
    ) => void
}

function wijzigSelectie(
    huidigeIds: string[],
    id: string,
) {
    if (huidigeIds.includes(id)) {
        return huidigeIds.filter(
            (huidigId) =>
                huidigId !== id,
        )
    }

    return [
        ...huidigeIds,
        id,
    ]
}

function bepaalStandaardProcesstap(
    afspraak: AfspraakDetail,
) {
    const actieveProcesstap =
        bepaalActieveProcesstap(
            afspraak,
        )

    if (actieveProcesstap !== undefined) {
        return actieveProcesstap
    }

    return afspraak.processtappen[0]
        ?? null
}

export function ToewijzingSection({
                                      afspraak,
                                      onAfspraakGewijzigd,
                                  }: ToewijzingSectionProps) {
    const {
        gebruiker,
    } = useAuth()

    const standaardProcesstap =
        bepaalStandaardProcesstap(
            afspraak,
        )

    const [
        gebruikers,
        setGebruikers,
    ] = useState<AfspraakGebruiker[]>([])

    const [
        afdelingen,
        setAfdelingen,
    ] = useState<Afdeling[]>([])

    const [
        processtapId,
        setProcesstapId,
    ] = useState(
        standaardProcesstap?.id
        ?? '',
    )

    const [
        verantwoordelijkeId,
        setVerantwoordelijkeId,
    ] = useState(
        standaardProcesstap
            ?.verantwoordelijke.id
        ?? '',
    )

    const [
        betrokkeneIds,
        setBetrokkeneIds,
    ] = useState<string[]>(
        afspraak.betrokkenGebruikers.map(
            (betrokkene) =>
                betrokkene.id,
        ),
    )

    const [
        afdelingIds,
        setAfdelingIds,
    ] = useState<string[]>(
        afspraak.betrokkenAfdelingen.map(
            (afdeling) =>
                afdeling.id,
        ),
    )

    const [
        laden,
        setLaden,
    ] = useState(false)

    const [
        opslaan,
        setOpslaan,
    ] = useState(false)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    const [
        succesmelding,
        setSuccesmelding,
    ] = useState<string | null>(null)

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

    useEffect(() => {
        if (!magWijzigen) {
            return
        }

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
    }, [magWijzigen])

    if (!magWijzigen) {
        return null
    }

    async function handleOpslaan() {
        if (
            processtapId === ''
            || verantwoordelijkeId === ''
        ) {
            setFoutmelding(
                'Processtap en verantwoordelijke zijn verplicht.',
            )

            return
        }

        try {
            setOpslaan(true)
            setFoutmelding(null)
            setSuccesmelding(null)

            const bijgewerkteAfspraak =
                await wijzigToewijzing(
                    afspraak.id,
                    {
                        betrokkeneIds,
                        afdelingIds,
                        processtapId,
                        verantwoordelijkeId,
                    },
                )

            onAfspraakGewijzigd(
                bijgewerkteAfspraak,
            )

            setSuccesmelding(
                'Toewijzing bijgewerkt.',
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Toewijzing kon niet worden bijgewerkt',
            )
        } finally {
            setOpslaan(false)
        }
    }

    return (
        <section className="detail-section">
            <h2>
                Toewijzing
            </h2>

            <article className="content-card">
                {laden ? (
                    <p>
                        Keuzes laden...
                    </p>
                ) : (
                    <>
                        <div className="form-grid">
                            <label className="form-field">
                                <span>
                                    Processtap
                                </span>

                                <select
                                    value={processtapId}
                                    disabled={opslaan}
                                    onChange={(event) => {
                                        const nieuwProcesstapId =
                                            event.target.value

                                        setProcesstapId(
                                            nieuwProcesstapId,
                                        )

                                        const processtap =
                                            afspraak.processtappen.find(
                                                (item) =>
                                                    item.id
                                                    === nieuwProcesstapId,
                                            )

                                        if (processtap !== undefined) {
                                            setVerantwoordelijkeId(
                                                processtap
                                                    .verantwoordelijke
                                                    .id,
                                            )
                                        }
                                    }}
                                >
                                    {afspraak.processtappen.map(
                                        (processtap) => (
                                            <option
                                                key={processtap.id}
                                                value={processtap.id}
                                            >
                                                {processtap.volgorde}
                                                {' - '}
                                                {processtap.naam}
                                            </option>
                                        ),
                                    )}
                                </select>
                            </label>

                            <label className="form-field">
                                <span>
                                    Verantwoordelijke
                                </span>

                                <select
                                    value={
                                        verantwoordelijkeId
                                    }
                                    disabled={opslaan}
                                    onChange={(event) =>
                                        setVerantwoordelijkeId(
                                            event.target.value,
                                        )
                                    }
                                >
                                    <option value="">
                                        Selecteer gebruiker
                                    </option>

                                    {gebruikers.map(
                                        (keuzeGebruiker) => (
                                            <option
                                                key={
                                                    keuzeGebruiker.id
                                                }
                                                value={
                                                    keuzeGebruiker.id
                                                }
                                            >
                                                {
                                                    keuzeGebruiker
                                                        .naam
                                                }
                                            </option>
                                        ),
                                    )}
                                </select>
                            </label>
                        </div>

                        <h3>
                            Betrokkenen
                        </h3>

                        <div className="selection-grid">
                            {gebruikers.map(
                                (keuzeGebruiker) => (
                                    <label
                                        key={
                                            keuzeGebruiker.id
                                        }
                                        className="selection-option"
                                    >
                                        <input
                                            type="checkbox"
                                            checked={
                                                betrokkeneIds
                                                    .includes(
                                                        keuzeGebruiker.id,
                                                    )
                                            }
                                            disabled={opslaan}
                                            onChange={() =>
                                                setBetrokkeneIds(
                                                    (huidige) =>
                                                        wijzigSelectie(
                                                            huidige,
                                                            keuzeGebruiker.id,
                                                        ),
                                                )
                                            }
                                        />

                                        <span>
                                            <strong>
                                                {
                                                    keuzeGebruiker
                                                        .naam
                                                }
                                            </strong>

                                            <small>
                                                {
                                                    keuzeGebruiker
                                                        .afdeling
                                                        ?.naam
                                                    ?? 'Geen afdeling'
                                                }
                                            </small>
                                        </span>
                                    </label>
                                ),
                            )}
                        </div>

                        <h3>
                            Betrokken afdelingen
                        </h3>

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
                                            disabled={opslaan}
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

                        {foutmelding !== null && (
                            <div className="alert-error">
                                {foutmelding}
                            </div>
                        )}

                        {succesmelding !== null && (
                            <div className="alert-success">
                                {succesmelding}
                            </div>
                        )}

                        <div className="form-actions">
                            <button
                                type="button"
                                className="primary-button"
                                disabled={
                                    opslaan
                                    || processtapId === ''
                                    || verantwoordelijkeId === ''
                                }
                                onClick={() =>
                                    void handleOpslaan()
                                }
                            >
                                {opslaan
                                    ? 'Opslaan...'
                                    : 'Toewijzing opslaan'}
                            </button>
                        </div>
                    </>
                )}
            </article>
        </section>
    )
}