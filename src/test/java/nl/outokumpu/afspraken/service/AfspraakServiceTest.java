package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.request.CreateAfspraakRequest;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AfspraakServiceTest {

    private OperationeleAfspraakRepository afspraakRepository;
    private GebruikerRepository gebruikerRepository;
    private AfdelingRepository afdelingRepository;

    private AfspraakService afspraakService;

    @BeforeEach
    void setUp() {
        afspraakRepository =
                mock(OperationeleAfspraakRepository.class);

        gebruikerRepository =
                mock(GebruikerRepository.class);

        afdelingRepository =
                mock(AfdelingRepository.class);

        afspraakService = new AfspraakService(
                afspraakRepository,
                gebruikerRepository,
                afdelingRepository
        );
    }

    @Test
    void maaktAlgemeneAfspraakAan() {

        UUID initiatiefnemerId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();
        UUID betrokkeneId = UUID.randomUUID();
        UUID goedkeurderId = UUID.randomUUID();
        UUID afdelingId = UUID.randomUUID();

        Gebruiker initiatiefnemer = mock(Gebruiker.class);
        Gebruiker verantwoordelijke = mock(Gebruiker.class);
        Gebruiker betrokkene = mock(Gebruiker.class);
        Gebruiker goedkeurder = mock(Gebruiker.class);
        Afdeling afdeling = mock(Afdeling.class);

        List<UUID> betrokkeneIds = List.of(betrokkeneId);
        List<UUID> goedkeurderIds = List.of(goedkeurderId);
        List<UUID> afdelingIds = List.of(afdelingId);

        when(gebruikerRepository.findById(initiatiefnemerId))
                .thenReturn(Optional.of(initiatiefnemer));

        when(gebruikerRepository.findById(verantwoordelijkeId))
                .thenReturn(Optional.of(verantwoordelijke));

        when(gebruikerRepository.findAllById(betrokkeneIds))
                .thenReturn(List.of(betrokkene));

        when(gebruikerRepository.findAllById(goedkeurderIds))
                .thenReturn(List.of(goedkeurder));

        when(afdelingRepository.findAllById(afdelingIds))
                .thenReturn(List.of(afdeling));

        when(afspraakRepository.save(any(OperationeleAfspraak.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAfspraakRequest request =
                new CreateAfspraakRequest(
                        "Nieuwe afspraak",
                        "Beschrijving",
                        "Operationele reden",
                        "Achtergrond",
                        "Aanname",
                        AfspraakType.ALGEMEEN,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        verantwoordelijkeId,
                        "Eerste beoordeling",
                        LocalDate.of(2026, 10, 15),
                        betrokkeneIds,
                        goedkeurderIds,
                        afdelingIds,
                        null,
                        null
                );

        OperationeleAfspraak resultaat =
                afspraakService.createAfspraak(
                        request,
                        initiatiefnemerId
                );

        assertEquals(
                "Nieuwe afspraak",
                resultaat.getTitel()
        );

        assertEquals(
                AfspraakType.ALGEMEEN,
                resultaat.getType()
        );

        assertSame(
                initiatiefnemer,
                resultaat.getInitiatiefnemer()
        );

        assertEquals(
                1,
                resultaat.getProcesstappen().size()
        );

        assertEquals(
                "Eerste beoordeling",
                resultaat.getProcesstappen().get(0).getNaam()
        );

        assertEquals(
                1,
                resultaat.getProcesstappen().get(0).getVolgorde()
        );

        assertEquals(
                LocalDate.of(2026, 10, 15),
                resultaat.getProcesstappen().get(0).getDeadline()
        );

        assertSame(
                verantwoordelijke,
                resultaat.getProcesstappen().get(0).getVerantwoordelijke()
        );

        assertEquals(
                2,
                resultaat.getBetrokkenheden().size()
        );

        assertSame(
                betrokkene,
                resultaat.getBetrokkenheden().get(0).getGebruiker()
        );

        assertEquals(
                BetrokkenRol.BETROKKENE,
                resultaat.getBetrokkenheden().get(0).getRol()
        );

        assertSame(
                goedkeurder,
                resultaat.getBetrokkenheden().get(1).getGebruiker()
        );

        assertEquals(
                BetrokkenRol.GOEDKEURDER,
                resultaat.getBetrokkenheden().get(1).getRol()
        );

        assertEquals(
                1,
                resultaat.getBevestigingen().size()
        );

        assertSame(
                goedkeurder,
                resultaat.getBevestigingen().get(0).getGebruiker()
        );

        assertEquals(
                1,
                resultaat.getBetrokkenAfdelingen().size()
        );

        assertTrue(
                resultaat.getBetrokkenAfdelingen().contains(afdeling)
        );

        verify(gebruikerRepository)
                .findById(initiatiefnemerId);

        verify(gebruikerRepository)
                .findById(verantwoordelijkeId);

        verify(gebruikerRepository)
                .findAllById(betrokkeneIds);

        verify(gebruikerRepository)
                .findAllById(goedkeurderIds);

        verify(afdelingRepository)
                .findAllById(afdelingIds);

        verify(afspraakRepository)
                .save(any(OperationeleAfspraak.class));
    }

    @Test
    void weigertAfspraakWanneerBetrokkeneNietBestaat() {

        UUID initiatiefnemerId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();
        UUID onbekendeBetrokkeneId = UUID.randomUUID();

        Gebruiker initiatiefnemer = mock(Gebruiker.class);
        Gebruiker verantwoordelijke = mock(Gebruiker.class);

        List<UUID> betrokkeneIds =
                List.of(onbekendeBetrokkeneId);

        when(gebruikerRepository.findById(initiatiefnemerId))
                .thenReturn(Optional.of(initiatiefnemer));

        when(gebruikerRepository.findById(verantwoordelijkeId))
                .thenReturn(Optional.of(verantwoordelijke));

        when(gebruikerRepository.findAllById(betrokkeneIds))
                .thenReturn(List.of());

        CreateAfspraakRequest request =
                new CreateAfspraakRequest(
                        "Nieuwe afspraak",
                        "Beschrijving",
                        "Operationele reden",
                        null,
                        null,
                        AfspraakType.ALGEMEEN,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        verantwoordelijkeId,
                        "Eerste beoordeling",
                        LocalDate.of(2026, 10, 15),
                        betrokkeneIds,
                        List.of(),
                        List.of(),
                        null,
                        null
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> afspraakService.createAfspraak(
                                request,
                                initiatiefnemerId
                        )
                );

        assertEquals(
                "Een of meer betrokken gebruikers zijn niet gevonden",
                exception.getMessage()
        );

        verify(afspraakRepository, never())
                .save(any(OperationeleAfspraak.class));
    }

    @Test
    void maaktCapaciteitswisselAan() {

        UUID initiatiefnemerId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();

        Gebruiker initiatiefnemer = mock(Gebruiker.class);
        Gebruiker verantwoordelijke = mock(Gebruiker.class);

        when(gebruikerRepository.findById(initiatiefnemerId))
                .thenReturn(Optional.of(initiatiefnemer));

        when(gebruikerRepository.findById(verantwoordelijkeId))
                .thenReturn(Optional.of(verantwoordelijke));

        when(gebruikerRepository.findAllById(List.of()))
                .thenReturn(List.of());

        when(afdelingRepository.findAllById(List.of()))
                .thenReturn(List.of());

        when(afspraakRepository.save(any(OperationeleAfspraak.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CapaciteitswisselData capaciteitswisselData =
                new CapaciteitswisselData(
                        "Tornio",
                        "2B",
                        "304L",
                        new BigDecimal("100.000"),
                        new BigDecimal("125.000"),
                        "ton",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                );

        CreateAfspraakRequest request =
                new CreateAfspraakRequest(
                        "Capaciteit aanpassen",
                        "Tijdelijke capaciteitswissel",
                        "Productiebehoefte gewijzigd",
                        null,
                        null,
                        AfspraakType.CAPACITEITSWISSEL,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        verantwoordelijkeId,
                        "Capaciteitswissel uitvoeren",
                        LocalDate.of(2026, 10, 15),
                        List.of(),
                        List.of(),
                        List.of(),
                        capaciteitswisselData,
                        null
                );

        OperationeleAfspraak resultaat =
                afspraakService.createAfspraak(
                        request,
                        initiatiefnemerId
                );

        assertInstanceOf(
                Capaciteitswissel.class,
                resultaat
        );

        Capaciteitswissel capaciteitswissel =
                (Capaciteitswissel) resultaat;

        assertEquals(
                AfspraakType.CAPACITEITSWISSEL,
                capaciteitswissel.getType()
        );

        assertEquals(
                "Tornio",
                capaciteitswissel.getFabriek()
        );

        assertEquals(
                "2B",
                capaciteitswissel.getFinishType()
        );

        assertEquals(
                "304L",
                capaciteitswissel.getGrade()
        );

        assertEquals(
                new BigDecimal("100.000"),
                capaciteitswissel.getCapaciteitVan()
        );

        assertEquals(
                new BigDecimal("125.000"),
                capaciteitswissel.getCapaciteitNaar()
        );

        assertEquals(
                "ton",
                capaciteitswissel.getCapaciteitEenheid()
        );

        assertEquals(
                LocalDate.of(2026, 10, 1),
                capaciteitswissel.getPeriodeVan()
        );

        assertEquals(
                LocalDate.of(2026, 10, 31),
                capaciteitswissel.getPeriodeTot()
        );

        assertEquals(
                1,
                capaciteitswissel.getProcesstappen().size()
        );

        assertSame(
                verantwoordelijke,
                capaciteitswissel
                        .getProcesstappen()
                        .get(0)
                        .getVerantwoordelijke()
        );

        verify(afspraakRepository)
                .save(any(Capaciteitswissel.class));
    }
}