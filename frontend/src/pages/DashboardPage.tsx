import { useAuth } from '../auth/useAuth'

export function DashboardPage() {
    const {
        gebruiker,
        logout,
    } = useAuth()

    if (gebruiker === null) {
        return null
    }

    return (
        <main className="dashboard-page">
            <header className="dashboard-header">
                <div>
                    <p className="eyebrow">
                        Outokumpu
                    </p>

                    <h1>
                        Afsprakenmanagement
                    </h1>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void logout()}
                >
                    Uitloggen
                </button>
            </header>

            <section className="welcome-card">
                <h2>
                    Welkom, {gebruiker.naam}
                </h2>

                <p>
                    {gebruiker.email}
                </p>

                <p>
                    Rol: {gebruiker.rol}
                </p>

                {gebruiker.afdeling !== null && (
                    <p>
                        Afdeling:{' '}
                        {gebruiker.afdeling.naam}
                    </p>
                )}
            </section>
        </main>
    )
}