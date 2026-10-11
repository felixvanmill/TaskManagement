package nl.outokumpu.afspraken.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OpmerkingTest {

    @Test
    void maaktGeldigeOpmerkingAan() {

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        Gebruiker auteur =
                mock(
                        Gebruiker.class
                );

        Opmerking opmerking =
                new Opmerking(
                        afspraak,
                        auteur,
                        "Aanvullende terugkoppeling"
                );

        assertSame(
                afspraak,
                opmerking.getAfspraak()
        );

        assertSame(
                auteur,
                opmerking.getAuteur()
        );

        assertEquals(
                "Aanvullende terugkoppeling",
                opmerking.getTekst()
        );
    }

    @Test
    void verwijdertSpatiesRondTekst() {

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        Gebruiker auteur =
                mock(
                        Gebruiker.class
                );

        Opmerking opmerking =
                new Opmerking(
                        afspraak,
                        auteur,
                        "  Terugkoppeling  "
                );

        assertEquals(
                "Terugkoppeling",
                opmerking.getTekst()
        );
    }

    @Test
    void afspraakIsVerplicht() {

        Gebruiker auteur =
                mock(
                        Gebruiker.class
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new Opmerking(
                                        null,
                                        auteur,
                                        "Tekst"
                                )
                );

        assertEquals(
                "Afspraak is verplicht",
                exception.getMessage()
        );
    }

    @Test
    void auteurIsVerplicht() {

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new Opmerking(
                                        afspraak,
                                        null,
                                        "Tekst"
                                )
                );

        assertEquals(
                "Auteur is verplicht",
                exception.getMessage()
        );
    }

    @Test
    void legeOpmerkingWordtGeweigerd() {

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        Gebruiker auteur =
                mock(
                        Gebruiker.class
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new Opmerking(
                                        afspraak,
                                        auteur,
                                        "   "
                                )
                );

        assertEquals(
                "Opmerking mag niet leeg zijn",
                exception.getMessage()
        );
    }

    @Test
    void teLangeOpmerkingWordtGeweigerd() {

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        Gebruiker auteur =
                mock(
                        Gebruiker.class
                );

        String teLang =
                "a".repeat(
                        5001
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new Opmerking(
                                        afspraak,
                                        auteur,
                                        teLang
                                )
                );

        assertEquals(
                "Opmerking mag maximaal 5000 tekens bevatten",
                exception.getMessage()
        );
    }
}