import {
    useState,
} from 'react'

import {
    Navigate,
    useNavigate,
} from 'react-router-dom'

import { useAuth } from '../auth/useAuth'

export function LoginPage() {
    const {
        gebruiker,
        login,
    } = useAuth()

    const navigate =
        useNavigate()

    const [
        email,
        setEmail,
    ] = useState('')

    const [
        wachtwoord,
        setWachtwoord,
    ] = useState('')

    const [
        foutmelding,
        setFoutmelding,
    ] = useState<string | null>(null)

    const [
        bezig,
        setBezig,
    ] = useState(false)

    if (gebruiker !== null) {
        return (
            <Navigate
                to="/"
                replace
            />
        )
    }

    async function handleSubmit(
        event: React.FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        setFoutmelding(null)
        setBezig(true)

        try {
            await login({
                email,
                wachtwoord,
            })

            navigate(
                '/',
                {
                    replace: true,
                },
            )
        } catch (error) {
            setFoutmelding(
                error instanceof Error
                    ? error.message
                    : 'Inloggen is mislukt',
            )
        } finally {
            setBezig(false)
        }
    }

    return (
        <main className="login-page">
            <section className="login-card">
                <div>
                    <p className="eyebrow">
                        Outokumpu
                    </p>

                    <h1>
                        Afsprakenmanagement
                    </h1>

                    <p className="intro">
                        Log in om operationele afspraken
                        te beheren.
                    </p>
                </div>

                <form
                    onSubmit={handleSubmit}
                    className="login-form"
                >
                    <label>
                        E-mailadres
                        <input
                            type="email"
                            value={email}
                            onChange={(event) =>
                                setEmail(
                                    event.target.value,
                                )
                            }
                            autoComplete="email"
                            required
                        />
                    </label>

                    <label>
                        Wachtwoord
                        <input
                            type="password"
                            value={wachtwoord}
                            onChange={(event) =>
                                setWachtwoord(
                                    event.target.value,
                                )
                            }
                            autoComplete="current-password"
                            required
                        />
                    </label>

                    {foutmelding !== null && (
                        <p className="error-message">
                            {foutmelding}
                        </p>
                    )}

                    <button
                        type="submit"
                        disabled={bezig}
                    >
                        {bezig
                            ? 'Inloggen...'
                            : 'Inloggen'}
                    </button>
                </form>
            </section>
        </main>
    )
}