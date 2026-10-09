package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.LoginRequest;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.AuthResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.exception.AuthenticatieException;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final GebruikerRepository gebruikerRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            GebruikerRepository gebruikerRepository
    ) {
        this.authenticationManager =
                authenticationManager;

        this.gebruikerRepository =
                gebruikerRepository;
    }

    public Authentication login(
            LoginRequest request
    ) {

        try {

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.wachtwoord()
                    )
            );

        } catch (AuthenticationException exception) {

            /*
             * Bewust generieke melding.
             * We verklappen niet of het e-mailadres
             * of het wachtwoord fout was.
             */
            throw new AuthenticatieException(
                    "Ongeldige inloggegevens"
            );
        }
    }

    public AuthResponse vindIngelogdeGebruiker(
            String email
    ) {

        Gebruiker gebruiker =
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                email
                        )
                        .orElseThrow(() ->
                                new AuthenticatieException(
                                        "Ingelogde gebruiker niet gevonden"
                                )
                        );

        AfdelingResponse afdelingResponse =
                naarAfdelingResponse(
                        gebruiker.getAfdeling()
                );

        return new AuthResponse(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getEmail(),
                gebruiker.getRol(),
                afdelingResponse
        );
    }

    private AfdelingResponse naarAfdelingResponse(
            Afdeling afdeling
    ) {

        if (afdeling == null) {
            return null;
        }

        return new AfdelingResponse(
                afdeling.getId(),
                afdeling.getNaam()
        );
    }
}