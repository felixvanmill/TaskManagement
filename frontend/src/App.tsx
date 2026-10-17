import {
    Navigate,
    Route,
    Routes,
} from 'react-router-dom'

import { ProtectedRoute } from './components/ProtectedRoute'
import { AfspraakDetailPage } from './pages/AfspraakDetailPage'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { NieuweAfspraakPage } from './pages/NieuweAfspraakPage'

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