import {
    Navigate,
    Outlet,
} from 'react-router-dom'

import { useAuth } from '../auth/useAuth'

export function ProtectedRoute() {
    const {
        gebruiker,
        laden,
    } = useAuth()

    if (laden) {
        return (
            <main className="centered-page">
                <p>Applicatie laden...</p>
            </main>
        )
    }

    if (gebruiker === null) {
        return (
            <Navigate
                to="/login"
                replace
            />
        )
    }

    return <Outlet />
}