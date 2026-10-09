package nl.outokumpu.afspraken.entity;

import nl.outokumpu.afspraken.enums.AfspraakStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OperationeleAfspraakTest {

    @Test
    void wijzigtStatusNaarInBehandeling() {

        OperationeleAfspraak afspraak =
                maakAfspraak();

        assertEquals(
                AfspraakStatus.OPEN,
                afspraak.getStatus()
        );

        afspraak.wijzigStatus(
                AfspraakStatus.IN_BEHANDELING
        );

        assertEquals(
                AfspraakStatus.IN_BEHANDELING,
                afspraak.getStatus()
        );

        assertNull(
                afspraak.getAfgerondOp()
        );
    }

    @Test
    void rondtAfspraakAf() {

        OperationeleAfspraak afspraak =
                maakAfspraak();

        afspraak.wijzigStatus(
                AfspraakStatus.IN_BEHANDELING
        );

        afspraak.wijzigStatus(
                AfspraakStatus.AFGEROND
        );

        assertEquals(
                AfspraakStatus.AFGEROND,
                afspraak.getStatus()
        );

        assertNotNull(
                afspraak.getAfgerondOp()
        );
    }

    @Test
    void weigertNullAlsNieuweStatus() {

        OperationeleAfspraak afspraak =
                maakAfspraak();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> afspraak.wijzigStatus(null)
                );

        assertEquals(
                "Nieuwe afspraakstatus is verplicht",
                exception.getMessage()
        );

        assertEquals(
                AfspraakStatus.OPEN,
                afspraak.getStatus()
        );
    }

    private OperationeleAfspraak maakAfspraak() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        return new OperationeleAfspraak(
                "Operationele afspraak",
                "Beschrijving",
                "Reden",
                null,
                null,
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );
    }
}