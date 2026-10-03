package nl.outokumpu.afspraken.entity;

import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OrderverplaatsingTest {

    @Test
    void startMetNietUitgevoerd() {

        Orderverplaatsing orderverplaatsing =
                maakOrderverplaatsing();

        assertEquals(
                UitvoeringsStatus.NIET_UITGEVOERD,
                orderverplaatsing.getUitvoeringsstatus()
        );
    }

    @Test
    void wijzigtUitvoeringsstatus() {

        Orderverplaatsing orderverplaatsing =
                maakOrderverplaatsing();

        orderverplaatsing.wijzigUitvoeringsstatus(
                UitvoeringsStatus.IN_UITVOERING
        );

        assertEquals(
                UitvoeringsStatus.IN_UITVOERING,
                orderverplaatsing.getUitvoeringsstatus()
        );

        orderverplaatsing.wijzigUitvoeringsstatus(
                UitvoeringsStatus.UITGEVOERD
        );

        assertEquals(
                UitvoeringsStatus.UITGEVOERD,
                orderverplaatsing.getUitvoeringsstatus()
        );
    }

    @Test
    void weigertNullAlsUitvoeringsstatus() {

        Orderverplaatsing orderverplaatsing =
                maakOrderverplaatsing();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                orderverplaatsing
                                        .wijzigUitvoeringsstatus(null)
                );

        assertEquals(
                "Nieuwe uitvoeringsstatus is verplicht",
                exception.getMessage()
        );

        assertEquals(
                UitvoeringsStatus.NIET_UITGEVOERD,
                orderverplaatsing.getUitvoeringsstatus()
        );
    }

    @Test
    void weigertZelfdeUitvoeringsstatus() {

        Orderverplaatsing orderverplaatsing =
                maakOrderverplaatsing();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                orderverplaatsing
                                        .wijzigUitvoeringsstatus(
                                                UitvoeringsStatus.NIET_UITGEVOERD
                                        )
                );

        assertEquals(
                "Nieuwe uitvoeringsstatus moet verschillen van de huidige status",
                exception.getMessage()
        );
    }

    private Orderverplaatsing maakOrderverplaatsing() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        return new Orderverplaatsing(
                "Order verplaatsen",
                "Beschrijving",
                "Reden",
                null,
                null,
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer,
                "Fabriek A",
                "Fabriek B",
                "2B",
                new BigDecimal("125.500"),
                LocalDate.of(2026, 10, 15)
        );
    }
}