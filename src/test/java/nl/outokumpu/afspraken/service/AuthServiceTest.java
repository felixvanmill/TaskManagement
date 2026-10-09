package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.LoginRequest;
import nl.outokumpu.afspraken.dto.response.AuthResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.exception.AuthenticatieException;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private GebruikerRepository gebruikerRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {

        authenticationManager =
                mock(AuthenticationManager.class);

        gebruikerRepository =
                mock(GebruikerRepository.class);

        authService =
                new AuthService(
                        authenticationManager,
                        gebruikerRepository
                );
    }

    @Test
    void geldigeInloggegevensWordenGeauthenticeerd() {

        LoginRequest request =
                new LoginRequest(
                        "admin@example.com",
                        "SterkWachtwoord123!"
                );

        Authentication authentication =
                mock(Authentication.class);

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenReturn(
                authentication
        );

        Authentication resultaat =
                authService.login(
                        request
                );

        assertSame(
                authentication,
                resultaat
        );

        verify(
                authenticationManager
        ).authenticate(
                any(
                        UsernamePasswordAuthenticationToken.class
                )
        );
    }

    @Test
    void ongeldigeInloggegevensGevenVeiligeFoutmelding() {

        LoginRequest request =
                new LoginRequest(
                        "admin@example.com",
                        "fout"
                );

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        ).thenThrow(
                new BadCredentialsException(
                        "Bad credentials"
                )
        );

        AuthenticatieException exception =
                assertThrows(
                        AuthenticatieException.class,
                        () ->
                                authService.login(
                                        request
                                )
                );

        assertEquals(
                "Ongeldige inloggegevens",
                exception.getMessage()
        );
    }

    @Test
    void haaltIngelogdeGebruikerOp() {

        UUID afdelingId =
                UUID.randomUUID();

        Afdeling afdeling =
                mock(Afdeling.class);

        when(
                afdeling.getId()
        ).thenReturn(
                afdelingId
        );

        when(
                afdeling.getNaam()
        ).thenReturn(
                "Capacity Planning"
        );

        Gebruiker gebruiker =
                new Gebruiker(
                        "Admin",
                        "admin@example.com",
                        afdeling
                );

        gebruiker.configureerToegang(
                "$2a$10$hash",
                GebruikersRol.BEHEERDER,
                true
        );

        when(
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                "admin@example.com"
                        )
        ).thenReturn(
                Optional.of(
                        gebruiker
                )
        );

        AuthResponse response =
                authService
                        .vindIngelogdeGebruiker(
                                "admin@example.com"
                        );

        assertEquals(
                "Admin",
                response.naam()
        );

        assertEquals(
                "admin@example.com",
                response.email()
        );

        assertEquals(
                GebruikersRol.BEHEERDER,
                response.rol()
        );

        assertEquals(
                afdelingId,
                response.afdeling().id()
        );
    }
}