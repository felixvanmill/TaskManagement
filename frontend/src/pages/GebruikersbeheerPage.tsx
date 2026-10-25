import {
    useEffect,
    useState,
} from 'react'

import {
    Link,
} from 'react-router-dom'

import {
    getGebruikersVoorBeheer,
    wijzigGebruikerToegang,
} from '../api/gebruikersBeheer'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    GebruikerBeheer,
    GebruikersRol,
} from '../types/auth'

interface GebruikerFormProps {
    gebruiker: GebruikerBeheer
    huidigeGebruikerId: string
    onGewijzigd: (
        gebruiker: GebruikerBeheer,
    ) => void
}

function GebruikerForm({
                           gebruiker,
                           huidigeGebruikerId,
                           onGewijzigd,
                       }: GebruikerFormProps) {
    const [
        rol,
        setRol,
    ] = useState<GebruikersRol>(
        gebruiker.rol,
    )

    const [
        actief,
        setActief,
    ] = useState(
        gebruiker.actief,
    )

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

    const isHuidigeGebruiker =
        gebruiker.id === huidigeGebruikerId

    const heeftWijzigingen =
        rol !== gebruiker.rol
        || actief !== gebruiker.actief

    async function handleOpslaan() {
        if (!heeftWijzigingen) {
            return
        }

        try {
            setOpslaan(true)
            setFoutmelding(null)
            setSuccesmelding(null)

            const bijgewerkt =
                await wijzigGebruikerToegang(
                    gebruiker.id,
                    {
                        rol,
                        actief,
                    },
                )

            onGewijzigd(
                bijgewerkt,
            )

            setSuccesmelding(
                'Gebruiker bijgewerkt.',
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Gebruiker kon niet worden bijgewerkt',
            )
        } finally {
            setOpslaan(false)
        }
    }

    return (
        <article className="content-card user-management-card">
            <div className="user-management-header">
                <div>
                    <h2>
                        {gebruiker.naam}
                    </h2>

                    <p>
                        {gebruiker.email}
                    </p>

                    <span>
                        {gebruiker.afdeling?.naam
                            ?? 'Geen afdeling'}
                    </span>
                </div>

                <div className="user-access-badges">
                    <span
                        className={
                            gebruiker.actief
                                ? 'access-badge access-active'
                                : 'access-badge access-inactive'
                        }
                    >
                        {gebruiker.actief
                            ? 'Actief'
                            : 'Inactief'}
                    </span>

                    <span className="access-badge">
                        {gebruiker.lokaleToegang
                            ? 'Login ingesteld'
                            : 'Nog geen login ingesteld'}
                    </span>
                </div>
            </div>

            {isHuidigeGebruiker && (
                <p className="admin-self-warning">
                    Dit is je eigen account. Je rol en
                    actieve status kunnen hier niet worden
                    gewijzigd om te voorkomen dat je jezelf
                    buitensluit.
                </p>
            )}

            <div className="user-management-fields">
                <label className="form-field">
                    <span>Rol</span>

                    <select
                        value={rol}
                        disabled={
                            opslaan
                            || isHuidigeGebruiker
                        }
                        onChange={(event) =>
                            setRol(
                                event.target.value as GebruikersRol,
                            )
                        }
                    >
                        <option value="GEBRUIKER">
                            Gebruiker
                        </option>

                        <option value="BEHEERDER">
                            Beheerder
                        </option>
                    </select>
                </label>

                <label className="access-checkbox">
                    <input
                        type="checkbox"
                        checked={actief}
                        disabled={
                            opslaan
                            || isHuidigeGebruiker
                        }
                        onChange={(event) =>
                            setActief(
                                event.target.checked,
                            )
                        }
                    />

                    <span>
                        Account actief
                    </span>
                </label>
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

            <div className="user-management-actions">
                <button
                    type="button"
                    className="primary-button"
                    disabled={
                        opslaan
                        || !heeftWijzigingen
                    }
                    onClick={() =>
                        void handleOpslaan()
                    }
                >
                    {opslaan
                        ? 'Opslaan...'
                        : 'Wijzigingen opslaan'}
                </button>
            </div>
        </article>
    )
}

export function GebruikersbeheerPage() {
    const {
        gebruiker,
    } = useAuth()

    const [
        gebruikers,
        setGebruikers,
    ] = useState<GebruikerBeheer[]>([])

    const [
        laden,
        setLaden,
    ] = useState(true)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    useEffect(() => {
        async function laadGebruikers() {
            try {
                setLaden(true)
                setFoutmelding(null)

                const response =
                    await getGebruikersVoorBeheer()

                setGebruikers(
                    response,
                )
            } catch (error) {
                setFoutmelding(
                    error instanceof Error
                        ? error.message
                        : 'Gebruikers konden niet worden geladen',
                )
            } finally {
                setLaden(false)
            }
        }

        void laadGebruikers()
    }, [])

    if (gebruiker === null) {
        return null
    }

    function verwerkGewijzigdeGebruiker(
        bijgewerkt: GebruikerBeheer,
    ) {
        setGebruikers(
            (huidigeGebruikers) =>
                huidigeGebruikers.map(
                    (item) =>
                        item.id === bijgewerkt.id
                            ? bijgewerkt
                            : item,
                ),
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
                        Beheer
                    </p>

                    <h1>
                        Gebruikersbeheer
                    </h1>

                    <p className="detail-description">
                        Beheer rollen en toegang van
                        gebruikers.
                    </p>
                </div>
            </header>

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}

            {laden && (
                <section className="content-card">
                    <p>
                        Gebruikers laden...
                    </p>
                </section>
            )}

            {!laden
                && foutmelding === null
                && gebruikers.length === 0 && (
                    <section className="content-card empty-state">
                        <h2>
                            Geen gebruikers gevonden
                        </h2>
                    </section>
                )}

            {!laden
                && foutmelding === null
                && gebruikers.length > 0 && (
                    <section className="user-management-list">
                        {gebruikers.map(
                            (beheerGebruiker) => (
                                <GebruikerForm
                                    key={beheerGebruiker.id}
                                    gebruiker={beheerGebruiker}
                                    huidigeGebruikerId={
                                        gebruiker.id
                                    }
                                    onGewijzigd={
                                        verwerkGewijzigdeGebruiker
                                    }
                                />
                            ),
                        )}
                    </section>
                )}
        </main>
    )
}