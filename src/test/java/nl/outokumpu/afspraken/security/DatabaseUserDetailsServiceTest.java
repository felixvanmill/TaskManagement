package nl.outokumpu.afspraken.security;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseUserDetailsServiceTest {

    private GebruikerRepository gebruikerRepository;
    private DatabaseUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {

        gebruikerRepository =
                mock(GebruikerRepository.class);

        userDetailsService =
                new DatabaseUserDetailsService(
                        gebruikerRepository
                );
    }

    @Test
    void laadtGebruikerVoorSpringSecurity() {

        Afdeling afdeling =
                mock(Afdeling.class);

        Gebruiker gebruiker =
                new Gebruiker(
                        "Admin",
                        "admin@example.com",
                        afdeling
                );

        gebruiker.configureerToegang(
                "$2a$10$voorbeeldhash",
                GebruikersRol.BEHEERDER,
                true
        );

        when(
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                "ADMIN@example.com"
                        )
        ).thenReturn(
                Optional.of(
                        gebruiker
                )
        );

        UserDetails resultaat =
                userDetailsService.loadUserByUsername(
                        "ADMIN@example.com"
                );

        assertEquals(
                "admin@example.com",
                resultaat.getUsername()
        );

        assertEquals(
                "$2a$10$voorbeeldhash",
                resultaat.getPassword()
        );

        assertTrue(
                resultaat.isEnabled()
        );

        assertTrue(
                resultaat.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals(
                                                "ROLE_BEHEERDER"
                                        )
                        )
        );
    }

    @Test
    void inactieveGebruikerIsDisabled() {

        Afdeling afdeling =
                mock(Afdeling.class);

        Gebruiker gebruiker =
                new Gebruiker(
                        "Gebruiker",
                        "user@example.com",
                        afdeling
                );

        gebruiker.configureerToegang(
                "$2a$10$voorbeeldhash",
                GebruikersRol.GEBRUIKER,
                false
        );

        when(
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                "user@example.com"
                        )
        ).thenReturn(
                Optional.of(
                        gebruiker
                )
        );

        UserDetails resultaat =
                userDetailsService.loadUserByUsername(
                        "user@example.com"
                );

        assertFalse(
                resultaat.isEnabled()
        );
    }

    @Test
    void onbekendeGebruikerWordtGeweigerd() {

        when(
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                "onbekend@example.com"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UsernameNotFoundException.class,
                () ->
                        userDetailsService.loadUserByUsername(
                                "onbekend@example.com"
                        )
        );
    }

    @Test
    void gebruikerZonderWachtwoordKanNietLokaalInloggen() {

        Afdeling afdeling =
                mock(Afdeling.class);

        Gebruiker gebruiker =
                new Gebruiker(
                        "Gebruiker",
                        "user@example.com",
                        afdeling
                );

        when(
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                "user@example.com"
                        )
        ).thenReturn(
                Optional.of(
                        gebruiker
                )
        );

        assertThrows(
                UsernameNotFoundException.class,
                () ->
                        userDetailsService.loadUserByUsername(
                                "user@example.com"
                        )
        );
    }
}