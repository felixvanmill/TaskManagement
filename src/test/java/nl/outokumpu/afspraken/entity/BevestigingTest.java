package nl.outokumpu.afspraken.entity;

import nl.outokumpu.afspraken.enums.Beslissing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BevestigingTest {

    @Test
    void markeertAfspraakAlsGezien() {

        Bevestiging bevestiging =
                maakBevestiging();

        assertFalse(
                bevestiging.isGezien()
        );

        assertNull(
                bevestiging.getGezienOp()
        );

        bevestiging.markeerGezien();

        assertTrue(
                bevestiging.isGezien()
        );

        assertNotNull(
                bevestiging.getGezienOp()
        );
    }

    @Test
    void weigertTweedeKeerGezienRegistreren() {

        Bevestiging bevestiging =
                maakBevestiging();

        bevestiging.markeerGezien();

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        bevestiging::markeerGezien
                );

        assertEquals(
                "Afspraak is al als gezien geregistreerd",
                exception.getMessage()
        );
    }

    @Test
    void registreertGoedkeuring() {

        Bevestiging bevestiging =
                maakBevestiging();

        bevestiging.registreerBeslissing(
                Beslissing.GOEDGEKEURD
        );

        assertEquals(
                Beslissing.GOEDGEKEURD,
                bevestiging.getBeslissing()
        );

        assertNotNull(
                bevestiging.getBeslistOp()
        );
    }

    @Test
    void registreertAfwijzing() {

        Bevestiging bevestiging =
                maakBevestiging();

        bevestiging.registreerBeslissing(
                Beslissing.AFGEWEZEN
        );

        assertEquals(
                Beslissing.AFGEWEZEN,
                bevestiging.getBeslissing()
        );

        assertNotNull(
                bevestiging.getBeslistOp()
        );
    }

    @Test
    void weigertGeenAlsBeslissing() {

        Bevestiging bevestiging =
                maakBevestiging();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                bevestiging.registreerBeslissing(
                                        Beslissing.GEEN
                                )
                );

        assertEquals(
                "Beslissing moet GOEDGEKEURD of AFGEWEZEN zijn",
                exception.getMessage()
        );

        assertEquals(
                Beslissing.GEEN,
                bevestiging.getBeslissing()
        );

        assertNull(
                bevestiging.getBeslistOp()
        );
    }

    private Bevestiging maakBevestiging() {

        OperationeleAfspraak afspraak =
                mock(OperationeleAfspraak.class);

        Gebruiker gebruiker =
                mock(Gebruiker.class);

        return new Bevestiging(
                afspraak,
                gebruiker
        );
    }
}