import {
    useState,
} from 'react'

import {
    processtapStatusLabel,
} from '../utils/afspraak'

import {
    formatteerDatum,
    formatteerDatumTijd,
} from '../utils/datum'

import {
    rondProcesstapAf,
    startProcesstap,
} from '../api/workflow'

import {
    useAuth,
} from '../auth/useAuth'

import type {
    AfspraakDetail,
} from '../types/afspraak'

interface ProcesstappenSectionProps {
    afspraak: AfspraakDetail
    onAfspraakGewijzigd: (
        afspraak: AfspraakDetail,
    ) => void
}

export function ProcesstappenSection({
                                         afspraak,
                                         onAfspraakGewijzigd,
                                     }: ProcesstappenSectionProps) {
    const {
        gebruiker,
    } = useAuth()

    const [
        bezigMetStapId,
        setBezigMetStapId,
    ] = useState<string | null>(null)

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    async function handleStarten(
        processtapId: string,
    ) {
        try {
            setBezigMetStapId(
                processtapId,
            )

            setFoutmelding(null)

            const bijgewerkteAfspraak =
                await startProcesstap(
                    processtapId,
                )

            onAfspraakGewijzigd(
                bijgewerkteAfspraak,
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Processtap kon niet worden gestart',
            )
        } finally {
            setBezigMetStapId(null)
        }
    }

    async function handleAfronden(
        processtapId: string,
    ) {
        try {
            setBezigMetStapId(
                processtapId,
            )

            setFoutmelding(null)

            const bijgewerkteAfspraak =
                await rondProcesstapAf(
                    processtapId,
                )

            onAfspraakGewijzigd(
                bijgewerkteAfspraak,
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Processtap kon niet worden afgerond',
            )
        } finally {
            setBezigMetStapId(null)
        }
    }

    return (
        <section className="detail-section">
            <h2>
                Processtappen
            </h2>

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}

            {afspraak.processtappen.length === 0 ? (
                <div className="content-card">
                    <p>
                        Geen processtappen beschikbaar.
                    </p>
                </div>
            ) : (
                <div className="process-list">
                    {afspraak.processtappen.map(
                        (processtap) => {
                            const isVerantwoordelijke =
                                gebruiker?.id
                                === processtap
                                    .verantwoordelijke
                                    .id

                            const isBezig =
                                bezigMetStapId
                                === processtap.id

                            return (
                                <article
                                    key={processtap.id}
                                    className="content-card process-card"
                                >
                                    <div>
                                        <span className="process-number">
                                            {processtap.volgorde}
                                        </span>
                                    </div>

                                    <div className="process-content">
                                        <h3>
                                            {processtap.naam}
                                        </h3>

                                        <p>
                                            Verantwoordelijke:{' '}
                                            <strong>
                                                {
                                                    processtap
                                                        .verantwoordelijke
                                                        .naam
                                                }
                                            </strong>
                                        </p>

                                        <p>
                                            Deadline:{' '}
                                            {formatteerDatum(
                                                processtap.deadline,
                                            )}
                                        </p>

                                        {processtap.gestartOp !== null && (
                                            <p>
                                                Gestart:{' '}
                                                {formatteerDatumTijd(
                                                    processtap.gestartOp,
                                                )}
                                            </p>
                                        )}

                                        {processtap.afgerondOp !== null && (
                                            <p>
                                                Afgerond:{' '}
                                                {formatteerDatumTijd(
                                                    processtap.afgerondOp,
                                                )}
                                            </p>
                                        )}
                                    </div>

                                    <div className="process-actions">
                                        <span
                                            className={
                                                `process-status process-status-${processtap.status.toLowerCase()}`
                                            }
                                        >
                                            {processtapStatusLabel(
                                                processtap.status,
                                            )}
                                        </span>

                                        {isVerantwoordelijke
                                            && processtap.status
                                            === 'NIET_GESTART' && (
                                                <button
                                                    type="button"
                                                    className="primary-button"
                                                    disabled={isBezig}
                                                    onClick={() =>
                                                        void handleStarten(
                                                            processtap.id,
                                                        )
                                                    }
                                                >
                                                    {isBezig
                                                        ? 'Starten...'
                                                        : 'Start processtap'}
                                                </button>
                                            )}

                                        {isVerantwoordelijke
                                            && processtap.status
                                            === 'IN_UITVOERING' && (
                                                <button
                                                    type="button"
                                                    className="primary-button"
                                                    disabled={isBezig}
                                                    onClick={() =>
                                                        void handleAfronden(
                                                            processtap.id,
                                                        )
                                                    }
                                                >
                                                    {isBezig
                                                        ? 'Afronden...'
                                                        : 'Rond processtap af'}
                                                </button>
                                            )}
                                    </div>
                                </article>
                            )
                        },
                    )}
                </div>
            )}
        </section>
    )
}