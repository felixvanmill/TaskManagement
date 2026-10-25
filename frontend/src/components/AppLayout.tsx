import {
    Link,
    NavLink,
    Outlet,
} from 'react-router-dom'

import {
    useAuth,
} from '../auth/useAuth'

function navigatieClassName({
                                isActive,
                            }: {
    isActive: boolean
}) {
    return isActive
        ? 'sidebar-nav-link sidebar-nav-link-active'
        : 'sidebar-nav-link'
}

export function AppLayout() {
    const {
        gebruiker,
        logout,
    } = useAuth()

    if (gebruiker === null) {
        return null
    }

    return (
        <div className="app-shell">
            <aside className="app-sidebar">
                <div>
                    <Link
                        to="/"
                        className="sidebar-brand"
                    >
                        <span
                            className="brand-symbol"
                            aria-hidden="true"
                        />

                        <span className="brand-copy">
                            <strong>
                                OUTOKUMPU
                            </strong>

                            <small>
                                Afsprakenmanagement
                            </small>
                        </span>
                    </Link>

                    <nav
                        className="sidebar-navigation"
                        aria-label="Hoofdnavigatie"
                    >
                        <span className="sidebar-section-title">
                            Navigatie
                        </span>

                        <NavLink
                            to="/"
                            end
                            className={navigatieClassName}
                        >
                            Overzicht
                        </NavLink>

                        <NavLink
                            to="/afspraken/nieuw"
                            className={navigatieClassName}
                        >
                            Nieuwe afspraak
                        </NavLink>

                        {gebruiker.rol === 'BEHEERDER' && (
                            <NavLink
                                to="/beheer/gebruikers"
                                className={navigatieClassName}
                            >
                                Gebruikersbeheer
                            </NavLink>
                        )}
                    </nav>
                </div>

                <div className="sidebar-footer">
                    <div className="sidebar-user">
                        <div className="sidebar-avatar">
                            {gebruiker.naam
                                .charAt(0)
                                .toUpperCase()}
                        </div>

                        <div>
                            <strong>
                                {gebruiker.naam}
                            </strong>

                            <span>
                                {gebruiker.afdeling?.naam
                                    ?? gebruiker.rol}
                            </span>
                        </div>
                    </div>

                    <button
                        type="button"
                        className="sidebar-logout"
                        onClick={() =>
                            void logout()
                        }
                    >
                        Uitloggen
                    </button>
                </div>
            </aside>

            <div className="app-content">
                <Outlet />
            </div>
        </div>
    )
}