import {
    useEffect,
    useState,
} from 'react'

import {
    formatteerDatumTijd,
} from '../utils/datum'

import {
    getOpmerkingen,
    voegOpmerkingToe,
} from '../api/opmerkingen'

import type {
    Opmerking,
} from '../types/opmerking'

interface OpmerkingenSectionProps {
    afspraakId: string
}

export function OpmerkingenSection({
                                       afspraakId,
                                   }: OpmerkingenSectionProps) {
    const [
        opmerkingen,
        setOpmerkingen,
    ] = useState<Opmerking[]>([])

    const [
        tekst,
        setTekst,
    ] = useState('')

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

    useEffect(() => {
        async function laadOpmerkingen() {
            try {
                setLaden(true)
                setFoutmelding(null)

                const response =
                    await getOpmerkingen(
                        afspraakId,
                    )

                setOpmerkingen(response)
            } catch (error) {
                setFoutmelding(
                    error instanceof Error
                        ? error.message
                        : 'Opmerkingen konden niet worden geladen',
                )
            } finally {
                setLaden(false)
            }
        }

        void laadOpmerkingen()
    }, [afspraakId])

    async function handleSubmit(
        event: React.FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const opgeschoondeTekst =
            tekst.trim()

        if (opgeschoondeTekst.length === 0) {
            return
        }

        try {
            setOpslaan(true)
            setFoutmelding(null)

            const nieuweOpmerking =
                await voegOpmerkingToe(
                    afspraakId,
                    opgeschoondeTekst,
                )

            setOpmerkingen(
                (huidigeOpmerkingen) => [
                    ...huidigeOpmerkingen,
                    nieuweOpmerking,
                ],
            )

            setTekst('')
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Opmerking kon niet worden toegevoegd',
            )
        } finally {
            setOpslaan(false)
        }
    }

    return (
        <section className="detail-section">
            <div className="comments-header">
                <div>
                    <h2>
                        Opmerkingen
                    </h2>

                    <p>
                        Aanvullende informatie en
                        terugkoppeling bij deze afspraak.
                    </p>
                </div>

                {!laden && (
                    <span className="comments-count">
                        {opmerkingen.length}
                    </span>
                )}
            </div>

            <form
                className="comment-form content-card"
                onSubmit={handleSubmit}
            >
                <label htmlFor="nieuwe-opmerking">
                    Nieuwe opmerking
                </label>

                <textarea
                    id="nieuwe-opmerking"
                    name="opmerking"
                    value={tekst}
                    onChange={(event) =>
                        setTekst(
                            event.target.value,
                        )
                    }
                    maxLength={5000}
                    rows={4}
                    placeholder="Voeg een opmerking of terugkoppeling toe..."
                    required
                />

                <div className="comment-form-footer">
                    <span>
                        {tekst.length} / 5000
                    </span>

                    <button
                        type="submit"
                        className="primary-button"
                        disabled={
                            opslaan
                            || tekst.trim().length === 0
                        }
                    >
                        {opslaan
                            ? 'Opslaan...'
                            : 'Opmerking toevoegen'}
                    </button>
                </div>
            </form>

            {foutmelding !== null && (
                <div className="alert-error">
                    {foutmelding}
                </div>
            )}

            {laden ? (
                <div className="content-card">
                    <p>
                        Opmerkingen laden...
                    </p>
                </div>
            ) : opmerkingen.length === 0 ? (
                <div className="content-card empty-state">
                    <h3>
                        Nog geen opmerkingen
                    </h3>

                    <p>
                        Er is nog geen aanvullende
                        terugkoppeling geplaatst.
                    </p>
                </div>
            ) : (
                <div className="comments-list">
                    {opmerkingen.map(
                        (opmerking) => (
                            <article
                                key={opmerking.id}
                                className="content-card comment-card"
                            >
                                <div className="comment-meta">
                                    <div>
                                        <strong>
                                            {
                                                opmerking
                                                    .auteur
                                                    .naam
                                            }
                                        </strong>

                                        <span>
                                            {
                                                opmerking
                                                    .auteur
                                                    .afdeling
                                                    ?.naam
                                                ?? 'Geen afdeling'
                                            }
                                        </span>
                                    </div>

                                    <time>
                                        {formatteerDatumTijd(
                                            opmerking
                                                .aangemaaktOp,
                                        )}
                                    </time>
                                </div>

                                <p className="comment-text">
                                    {opmerking.tekst}
                                </p>
                            </article>
                        ),
                    )}
                </div>
            )}
        </section>
    )
}