import { createContext } from 'react'

import type {
    AuthUser,
    LoginRequest,
} from '../types/auth'

export interface AuthContextValue {
    gebruiker: AuthUser | null
    laden: boolean
    login: (
        request: LoginRequest,
    ) => Promise<void>
    logout: () => Promise<void>
}

export const AuthContext =
    createContext<AuthContextValue | null>(
        null,
    )