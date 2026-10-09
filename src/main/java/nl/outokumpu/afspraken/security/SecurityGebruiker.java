package nl.outokumpu.afspraken.security;

import nl.outokumpu.afspraken.exception.AuthenticatieException;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public final class SecurityGebruiker {

    private SecurityGebruiker() {
    }

    public static UUID gebruikerId(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AuthenticatieException(
                    "Gebruiker is niet geauthenticeerd"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal
                instanceof GebruikerPrincipal gebruikerPrincipal)) {

            throw new AuthenticatieException(
                    "Gebruiker is niet geauthenticeerd"
            );
        }

        return gebruikerPrincipal.getId();
    }
}