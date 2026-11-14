
import {
    useCallback,
    useEffect,
    useState,
} from 'react'

import {
    useLocation,
    useNavigate,
} from 'react-router-dom'

import {
    getNotificaties,
    getOngelezenAantal,
    markeerNotificatieGelezen,
} from '../api/notificaties'

import type {
    Notificatie,
    NotificatieType,
} from '../types/notificatie'

import './NotificatieBel.css'

function notificatieTitel(type: NotificatieType): string {
    switch (type) {
        case 'TAAK_TOEGEWEZEN':
            return 'Nieuwe taak'
        case 'LEESVERZOEK':
            return 'Afspraak lezen'
        case 'GOEDKEURING_GEVRAAGD':
            return 'Goedkeuring gevraagd'
    }
}

export function NotificatieBel() {
    const navigate = useNavigate()
    const { pathname } = useLocation()

    const [open, setOpen] = useState(false)
    const [notificaties, setNotificaties] =
        useState<Notificatie[]>([])
    const [ongelezenAantal, setOngelezenAantal] =
        useState(0)
    const [laden, setLaden] = useState(true)
    const [bezigId, setBezigId] =
        useState<string | null>(null)
    const [foutmelding, setFoutmelding] =
        useState<string | null>(null)

    const verversNotificaties = useCallback(async () => {
        try {
            const [lijst, aantal] = await Promise.all([
                getNotificaties(),
                getOngelezenAantal(),
            ])

            setNotificaties(lijst)
            setOngelezenAantal(aantal)
            setFoutmelding(null)
        } catch {
            setFoutmelding(
                'Notificaties konden niet worden geladen.',
            )
        } finally {
            setLaden(false)
        }
    }, [])


    useEffect(() => {
        const timeoutId = window.setTimeout(() => {
            void verversNotificaties()
        }, 0)

        const intervalId = window.setInterval(() => {
            void verversNotificaties()
        }, 30000)

        return () => {
            window.clearTimeout(timeoutId)
            window.clearInterval(intervalId)
        }
    }, [verversNotificaties, pathname])


    function wisselOpen() {
        setOpen((huidig) => !huidig)
        void verversNotificaties()
    }

    async function openNotificatie(
        notificatie: Notificatie,
    ) {
        try {
            setBezigId(notificatie.id)
            setFoutmelding(null)

            if (notificatie.gelezenOp === null) {
                await markeerNotificatieGelezen(
                    notificatie.id,
                )
            }

            setOpen(false)

            navigate(
                `/afspraken/${notificatie.afspraakId}`,
            )

            void verversNotificaties()
        } catch {
            setFoutmelding(
                'Notificatie openen is mislukt.',
            )
        } finally {
            setBezigId(null)
        }
    }

    return (
        <div className="notification-wrapper">
            <button
                type="button"
                className="notification-toggle"
                aria-label={`Notificaties, ${ongelezenAantal} ongelezen`}
                aria-expanded={open}
                aria-controls="notification-panel"
                onClick={wisselOpen}
            >
                <svg
                    width="23"
                    height="23"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                >
                    <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
                    <path d="M10 21h4" />
                </svg>

                {ongelezenAantal > 0 && (
                    <span className="notification-badge">
                        {ongelezenAantal > 99
                            ? '99+'
                            : ongelezenAantal}
                    </span>
                )}
            </button>

            {open && (
                <div
                    id="notification-panel"
                    className="notification-panel"
                >
                    <div className="notification-header">
                        <strong>Notificaties</strong>
                        <span>
                            {ongelezenAantal} ongelezen
                        </span>
                    </div>

                    {foutmelding !== null && (
                        <p className="notification-error">
                            {foutmelding}
                        </p>
                    )}

                    {laden ? (
                        <p className="notification-empty">
                            Notificaties laden...
                        </p>
                    ) : notificaties.length === 0 ? (
                        <p className="notification-empty">
                            Je hebt geen openstaande notificaties.
                        </p>
                    ) : (
                        <div className="notification-list">
                            {notificaties.map((notificatie) => (
                                <button
                                    key={notificatie.id}
                                    type="button"
                                    disabled={bezigId !== null}
                                    className={
                                        'notification-item'
                                        + (notificatie.gelezenOp === null
                                            ? ' notification-item-unread'
                                            : '')
                                    }
                                    onClick={() => {
                                        void openNotificatie(notificatie)
                                    }}
                                >
                                    <span className="notification-item-title">
                                        {notificatieTitel(notificatie.type)}

                                        {notificatie.gelezenOp === null && (
                                            <span
                                                className="notification-unread-dot"
                                                aria-label="Ongelezen"
                                            />
                                        )}
                                    </span>

                                    <span className="notification-message">
                                        {notificatie.bericht}
                                    </span>

                                    <span className="notification-date">
                                        {new Date(
                                            notificatie.aangemaaktOp,
                                        ).toLocaleString('nl-NL')}
                                    </span>
                                </button>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    )
}
