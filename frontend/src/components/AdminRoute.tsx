import {
    Navigate,
    Outlet,
} from 'react-router-dom'

import {
    useAuth,
} from '../auth/useAuth'

export function AdminRoute() {
    const {
        gebruiker,
    } = useAuth()

    if (
        gebruiker === null
        || gebruiker.rol !== 'BEHEERDER'
    ) {
        return (
            <Navigate
                to="/"
                replace
            />
        )
    }

    return <Outlet />
}