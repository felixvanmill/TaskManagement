import {
    useState,
} from 'react'

import {
    bepaalHuidigeProcesstap,
    statusLabel,
} from '../utils/afspraak'

import {
    wijzigAfspraakStatus,
} from '../api/workflow'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    AfspraakDetail,
    AfspraakStatus,
} from '../types/afspraak'

interface AfspraakStatusSectionProps {
    afspraak: AfspraakDetail
    onAfspraakGewijzigd: (
        afspraak: AfspraakDetail,
    ) => void
}

export function AfspraakStatusSection({
                                          afspraak,
                                          onAfspraakGewijzigd,
                                      }: AfspraakStatusSectionProps) {
    const {
        gebruiker,
    } = useAuth()

    const [
        nieuweStatus,
        setNieuweStatus,
    ] = useState<AfspraakStatus | ''>('')

    const [
        opslaan,
        setOpslaan,
    ] = useState(false)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    const huidigeProcesstap =
        bepaalHuidigeProcesstap(
            afspraak,
        )

    const magWijzigen =
        gebruiker !== null
        && huidigeProcesstap !== undefined
        && gebruiker.id
        === huidigeProcesstap.verantwoordelijke.id

    async function handleOpslaan() {
        if (nieuweStatus === '') {
            return
        }

        try {
            setOpslaan(true)
            setFoutmelding(null)

            const bijgewerkteAfspraak =
                await wijzigAfspraakStatus(
                    afspraak.id,
                    nieuweStatus,
                )

            onAfspraakGewijzigd(
                bijgewerkteAfspraak,
            )

            setNieuweStatus('')
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Status kon niet worden gewijzigd',
            )
        } finally {
            setOpslaan(false)
        }
    }

    return (
        <section className="content-card afspraak-status-section">
            <div>
                <h2>
                    Afspraakstatus
                </h2>

                <p>
                    Huidige status:{' '}
                    <strong>
                        {statusLabel(
                            afspraak.status,
                        )}
                    </strong>
                </p>

                {huidigeProcesstap !== undefined && (
                    <p>
                        Huidige verantwoordelijke:{' '}
                        <strong>
                            {
                                huidigeProcesstap
                                    .verantwoordelijke
                                    .naam
                            }
                        </strong>
                    </p>
                )}
            </div>

            {magWijzigen && (
                <div className="status-change-controls">
                    <select
                        value={nieuweStatus}
                        onChange={(event) =>
                            setNieuweStatus(
                                event.target.value as AfspraakStatus | '',
                            )
                        }
                    >
                        <option value="">
                            Kies nieuwe status
                        </option>

                        {afspraak.status !== 'OPEN' && (
                            <option value="OPEN">
                                {statusLabel('OPEN')}
                            </option>
                        )}

                        {afspraak.status !== 'IN_BEHANDELING' && (
                            <option value="IN_BEHANDELING">
                                {statusLabel('IN_BEHANDELING')}
                            </option>
                        )}

                        {afspraak.status !== 'AFGEROND' && (
                            <option value="AFGEROND">
                                {statusLabel('AFGEROND')}
                            </option>
                        )}
                    </select>

                    <button
                        type="button"
                        className="primary-button"
                        disabled={
                            opslaan
                            || nieuweStatus === ''
                        }
                        onClick={() =>
                            void handleOpslaan()
                        }
                    >
                        {opslaan
                            ? 'Opslaan...'
                            : 'Status wijzigen'}
                    </button>
                </div>
            )}

            {!magWijzigen && (
                <p className="muted-text">
                    Alleen de huidige verantwoordelijke
                    kan de afspraakstatus wijzigen.
                </p>
            )}

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}
        </section>
    )
}