import {
    Navigate,
    Route,
    Routes,
} from 'react-router-dom'

import {
    AdminRoute,
} from './components/AdminRoute'

import {
    ProtectedRoute,
} from './components/ProtectedRoute'

import {
    AfspraakDetailPage,
} from './pages/AfspraakDetailPage'

import {
    DashboardPage,
} from './pages/DashboardPage'

import {
    GebruikersbeheerPage,
} from './pages/GebruikersbeheerPage'

import {
    LoginPage,
} from './pages/LoginPage'

import {
    NieuweAfspraakPage,
} from './pages/NieuweAfspraakPage'

import {
    BewerkAfspraakPage,
} from './pages/BewerkAfspraakPage'

function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={<LoginPage />}
            />

            <Route
                element={<ProtectedRoute />}
            >
                <Route
                    path="/"
                    element={<DashboardPage />}
                />

                <Route
                    path="/afspraken/nieuw"
                    element={<NieuweAfspraakPage />}
                />

                <Route
                    path="/afspraken/:id"
                    element={<AfspraakDetailPage />}
                />

                <Route
                    path="/afspraken/:id/bewerken"
                    element={<BewerkAfspraakPage />}
                />

                <Route
                    element={<AdminRoute />}
                >
                    <Route
                        path="/beheer/gebruikers"
                        element={
                            <GebruikersbeheerPage />
                        }
                    />
                </Route>
            </Route>

            <Route
                path="*"
                element={
                    <Navigate
                        to="/"
                        replace
                    />
                }
            />
        </Routes>
    )
}

export default App