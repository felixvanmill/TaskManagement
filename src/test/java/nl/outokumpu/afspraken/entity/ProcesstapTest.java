package nl.outokumpu.afspraken.entity;

import nl.outokumpu.afspraken.enums.ProcesstapStatus;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProcesstapTest {

    @Test
    void startProcesstap() {

        OperationeleAfspraak afspraak =
                mock(OperationeleAfspraak.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        Processtap processtap =
                new Processtap(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        afspraak,
                        verantwoordelijke
                );

        assertEquals(
                ProcesstapStatus.NIET_GESTART,
                processtap.getStatus()
        );

        assertNull(
                processtap.getGestartOp()
        );

        processtap.start();

        assertEquals(
                ProcesstapStatus.IN_UITVOERING,
                processtap.getStatus()
        );

        assertNotNull(
                processtap.getGestartOp()
        );
    }

    @Test
    void rondProcesstapAf() {

        OperationeleAfspraak afspraak =
                mock(OperationeleAfspraak.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        Processtap processtap =
                new Processtap(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        afspraak,
                        verantwoordelijke
                );

        processtap.start();
        processtap.rondAf();

        assertEquals(
                ProcesstapStatus.AFGEROND,
                processtap.getStatus()
        );

        assertNotNull(
                processtap.getGestartOp()
        );

        assertNotNull(
                processtap.getAfgerondOp()
        );
    }

    @Test
    void weigertAfrondenWanneerProcesstapNietGestartIs() {

        OperationeleAfspraak afspraak =
                mock(OperationeleAfspraak.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        Processtap processtap =
                new Processtap(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        afspraak,
                        verantwoordelijke
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        processtap::rondAf
                );

        assertEquals(
                "Processtap kan alleen worden afgerond vanuit IN_UITVOERING",
                exception.getMessage()
        );

        assertEquals(
                ProcesstapStatus.NIET_GESTART,
                processtap.getStatus()
        );

        assertNull(
                processtap.getAfgerondOp()
        );
    }
}