package nl.outokumpu.afspraken.entity;

import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AfspraakBewerkenDomeinTest {

    @Test
    void wijzigtAlgemeneAfspraakgegevens() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Oude titel",
                        "Oude beschrijving",
                        "Oude reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 20),
                        initiatiefnemer
                );

        afspraak.wijzigGegevens(
                "Nieuwe titel",
                "Nieuwe beschrijving",
                "Nieuwe reden",
                "Nieuwe achtergrond",
                "Nieuwe aannames",
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 11, 1)
        );

        assertEquals(
                "Nieuwe titel",
                afspraak.getTitel()
        );

        assertEquals(
                "Nieuwe beschrijving",
                afspraak.getBeschrijving()
        );

        assertEquals(
                "Nieuwe reden",
                afspraak.getReden()
        );

        assertEquals(
                "Nieuwe achtergrond",
                afspraak.getAchtergrond()
        );

        assertEquals(
                "Nieuwe aannames",
                afspraak.getAannames()
        );

        assertEquals(
                LocalDate.of(2026, 10, 5),
                afspraak.getIngangsdatum()
        );

        assertEquals(
                LocalDate.of(2026, 11, 1),
                afspraak.getDeadline()
        );
    }

    @Test
    void wijzigtCapaciteitswisselgegevens() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Capaciteitswissel afspraak =
                new Capaciteitswissel(
                        "Capaciteitswissel",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer,
                        "Tornio",
                        "2B",
                        "304L",
                        new BigDecimal("100.000"),
                        new BigDecimal("125.000"),
                        "ton",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                );

        afspraak.wijzigCapaciteitsgegevens(
                "Avesta",
                "2D",
                "316L",
                new BigDecimal("125.000"),
                new BigDecimal("150.000"),
                "ton",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
        );

        assertEquals(
                "Avesta",
                afspraak.getFabriek()
        );

        assertEquals(
                "2D",
                afspraak.getFinishType()
        );

        assertEquals(
                "316L",
                afspraak.getGrade()
        );

        assertEquals(
                new BigDecimal("125.000"),
                afspraak.getCapaciteitVan()
        );

        assertEquals(
                new BigDecimal("150.000"),
                afspraak.getCapaciteitNaar()
        );

        assertEquals(
                LocalDate.of(2026, 11, 1),
                afspraak.getPeriodeVan()
        );

        assertEquals(
                LocalDate.of(2026, 11, 30),
                afspraak.getPeriodeTot()
        );
    }

    @Test
    void wijzigtOrdergegevensZonderUitvoeringsstatusTeVeranderen() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Orderverplaatsing afspraak =
                new Orderverplaatsing(
                        "Orderverplaatsing",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer,
                        "Tornio",
                        "Avesta",
                        "2B",
                        new BigDecimal("100.000"),
                        LocalDate.of(2026, 10, 15)
                );

        afspraak.wijzigUitvoeringsstatus(
                UitvoeringsStatus.IN_UITVOERING
        );

        afspraak.wijzigOrdergegevens(
                "Avesta",
                "Tornio",
                "2D",
                new BigDecimal("175.000"),
                LocalDate.of(2026, 10, 25)
        );

        assertEquals(
                "Avesta",
                afspraak.getVanFabriek()
        );

        assertEquals(
                "Tornio",
                afspraak.getNaarFabriek()
        );

        assertEquals(
                "2D",
                afspraak.getFinishType()
        );

        assertEquals(
                new BigDecimal("175.000"),
                afspraak.getTotaalVolumeTons()
        );

        assertEquals(
                LocalDate.of(2026, 10, 25),
                afspraak.getGewensteVerplaatsingsdatum()
        );

        assertEquals(
                UitvoeringsStatus.IN_UITVOERING,
                afspraak.getUitvoeringsstatus()
        );
    }

    @Test
    void weigertLegeTitelBijWijzigen() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Titel",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                afspraak.wijzigGegevens(
                                        " ",
                                        "Beschrijving",
                                        "Reden",
                                        null,
                                        null,
                                        LocalDate.of(2026, 10, 1),
                                        LocalDate.of(2026, 10, 31)
                                )
                );

        assertEquals(
                "Titel is verplicht",
                exception.getMessage()
        );
    }
}