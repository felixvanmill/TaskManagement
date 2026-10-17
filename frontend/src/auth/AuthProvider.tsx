import {
    useEffect,
    useState,
} from 'react'

import {
    getCurrentUser,
    login as loginRequest,
    logout as logoutRequest,
} from '../api/http'

import type {
    AuthUser,
    LoginRequest,
} from '../types/auth'

import { AuthContext } from './auth-context'

import type {
    ReactNode,
} from 'react'

interface AuthProviderProps {
    children: ReactNode
}

export function AuthProvider({
                                 children,
                             }: AuthProviderProps) {
    const [
        gebruiker,
        setGebruiker,
    ] = useState<AuthUser | null>(null)

    const [
        laden,
        setLaden,
    ] = useState(true)

    useEffect(() => {
        async function laadGebruiker() {
            try {
                const ingelogdeGebruiker =
                    await getCurrentUser()

                setGebruiker(
                    ingelogdeGebruiker,
                )
            } catch {
                setGebruiker(null)
            } finally {
                setLaden(false)
            }
        }

        void laadGebruiker()
    }, [])

    async function login(
        request: LoginRequest,
    ) {
        const ingelogdeGebruiker =
            await loginRequest(request)

        setGebruiker(
            ingelogdeGebruiker,
        )
    }

    async function logout() {
        await logoutRequest()

        setGebruiker(null)
    }

    return (
        <AuthContext.Provider
            value={{
                gebruiker,
                laden,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    )
}