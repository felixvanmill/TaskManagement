package nl.outokumpu.afspraken.entity;

import nl.outokumpu.afspraken.enums.GebruikersRol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GebruikerSecurityTest {

    @Test
    void nieuweGebruikerHeeftVeiligeStandaardRol() {

        Afdeling afdeling =
                mock(Afdeling.class);

        Gebruiker gebruiker =
                new Gebruiker(
                        "Jan Jansen",
                        "jan@example.com",
                        afdeling
                );

        assertEquals(
                GebruikersRol.GEBRUIKER,
                gebruiker.getRol()
        );

        assertTrue(
                gebruiker.isActief()
        );

        assertNull(
                gebruiker.getWachtwoordHash()
        );
    }

    @Test
    void configureertLokaleToegang() {

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

        assertEquals(
                "$2a$10$voorbeeldhash",
                gebruiker.getWachtwoordHash()
        );

        assertEquals(
                GebruikersRol.BEHEERDER,
                gebruiker.getRol()
        );

        assertTrue(
                gebruiker.isActief()
        );
    }

    @Test
    void rolEnToegangKunnenWordenGewijzigd() {

        Afdeling afdeling =
                mock(Afdeling.class);

        Gebruiker gebruiker =
                new Gebruiker(
                        "Gebruiker",
                        "user@example.com",
                        afdeling
                );

        gebruiker.wijzigRol(
                GebruikersRol.BEHEERDER
        );

        gebruiker.wijzigToegang(
                false
        );

        assertEquals(
                GebruikersRol.BEHEERDER,
                gebruiker.getRol()
        );

        assertFalse(
                gebruiker.isActief()
        );
    }

    @Test
    void weigertLegeWachtwoordhash() {

        Afdeling afdeling =
                mock(Afdeling.class);

        Gebruiker gebruiker =
                new Gebruiker(
                        "Gebruiker",
                        "user@example.com",
                        afdeling
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                gebruiker.wijzigWachtwoordHash(
                                        " "
                                )
                );

        assertEquals(
                "Wachtwoordhash is verplicht",
                exception.getMessage()
        );
    }
}