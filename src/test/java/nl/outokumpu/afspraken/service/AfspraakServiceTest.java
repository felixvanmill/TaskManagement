package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;
import nl.outokumpu.afspraken.dto.request.CreateAfspraakRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Orderverplaatsing;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;
import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    private AfspraakMapper afspraakMapper;
    private WijzigingRepository wijzigingRepository;

    private AfspraakService afspraakService;

    @BeforeEach
    void setUp() {

        afspraakRepository =
                mock(OperationeleAfspraakRepository.class);

        gebruikerRepository =
                mock(GebruikerRepository.class);

        afdelingRepository =
                mock(AfdelingRepository.class);

        afspraakMapper =
                mock(AfspraakMapper.class);

        wijzigingRepository =
                mock(WijzigingRepository.class);

        afspraakService =
                new AfspraakService(
                        afspraakRepository,
                        gebruikerRepository,
                        afdelingRepository,
                        wijzigingRepository,
                        afspraakMapper
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

        List<UUID> betrokkeneIds =
                List.of(betrokkeneId);

        List<UUID> goedkeurderIds =
                List.of(goedkeurderId);

        List<UUID> afdelingIds =
                List.of(afdelingId);

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

        AfspraakDetailResponse response =
                maakResponse(
                        "Nieuwe afspraak",
                        AfspraakType.ALGEMEEN
                );

        when(afspraakMapper.naarDetailResponse(
                any(OperationeleAfspraak.class)
        )).thenReturn(response);

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

        AfspraakDetailResponse resultaat =
                afspraakService.createAfspraak(
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        ArgumentCaptor<OperationeleAfspraak> captor =
                ArgumentCaptor.forClass(
                        OperationeleAfspraak.class
                );

        verify(afspraakRepository)
                .save(captor.capture());

        OperationeleAfspraak opgeslagenAfspraak =
                captor.getValue();

        assertEquals(
                "Nieuwe afspraak",
                opgeslagenAfspraak.getTitel()
        );

        assertEquals(
                AfspraakType.ALGEMEEN,
                opgeslagenAfspraak.getType()
        );

        assertSame(
                initiatiefnemer,
                opgeslagenAfspraak.getInitiatiefnemer()
        );

        assertEquals(
                1,
                opgeslagenAfspraak.getProcesstappen().size()
        );

        assertEquals(
                "Eerste beoordeling",
                opgeslagenAfspraak
                        .getProcesstappen()
                        .get(0)
                        .getNaam()
        );

        assertEquals(
                1,
                opgeslagenAfspraak
                        .getProcesstappen()
                        .get(0)
                        .getVolgorde()
        );

        assertEquals(
                LocalDate.of(2026, 10, 15),
                opgeslagenAfspraak
                        .getProcesstappen()
                        .get(0)
                        .getDeadline()
        );

        assertSame(
                verantwoordelijke,
                opgeslagenAfspraak
                        .getProcesstappen()
                        .get(0)
                        .getVerantwoordelijke()
        );

        assertEquals(
                2,
                opgeslagenAfspraak
                        .getBetrokkenheden()
                        .size()
        );

        assertSame(
                betrokkene,
                opgeslagenAfspraak
                        .getBetrokkenheden()
                        .get(0)
                        .getGebruiker()
        );

        assertEquals(
                BetrokkenRol.BETROKKENE,
                opgeslagenAfspraak
                        .getBetrokkenheden()
                        .get(0)
                        .getRol()
        );

        assertSame(
                goedkeurder,
                opgeslagenAfspraak
                        .getBetrokkenheden()
                        .get(1)
                        .getGebruiker()
        );

        assertEquals(
                BetrokkenRol.GOEDKEURDER,
                opgeslagenAfspraak
                        .getBetrokkenheden()
                        .get(1)
                        .getRol()
        );

        assertEquals(
                2,
                opgeslagenAfspraak
                        .getBevestigingen()
                        .size()
        );

        assertSame(
                betrokkene,
                opgeslagenAfspraak
                        .getBevestigingen()
                        .get(0)
                        .getGebruiker()
        );

        assertSame(
                goedkeurder,
                opgeslagenAfspraak
                        .getBevestigingen()
                        .get(1)
                        .getGebruiker()
        );

        assertEquals(
                1,
                opgeslagenAfspraak
                        .getBetrokkenAfdelingen()
                        .size()
        );

        assertTrue(
                opgeslagenAfspraak
                        .getBetrokkenAfdelingen()
                        .contains(afdeling)
        );

        verify(afspraakMapper)
                .naarDetailResponse(
                        opgeslagenAfspraak
                );
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

        verify(
                afspraakRepository,
                never()
        ).save(
                any(OperationeleAfspraak.class)
        );

        verifyNoInteractions(
                afspraakMapper
        );
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

        AfspraakDetailResponse response =
                maakResponse(
                        "Capaciteit aanpassen",
                        AfspraakType.CAPACITEITSWISSEL
                );

        when(afspraakMapper.naarDetailResponse(
                any(OperationeleAfspraak.class)
        )).thenReturn(response);

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

        AfspraakDetailResponse resultaat =
                afspraakService.createAfspraak(
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        ArgumentCaptor<OperationeleAfspraak> captor =
                ArgumentCaptor.forClass(
                        OperationeleAfspraak.class
                );

        verify(afspraakRepository)
                .save(captor.capture());

        assertInstanceOf(
                Capaciteitswissel.class,
                captor.getValue()
        );

        Capaciteitswissel capaciteitswissel =
                (Capaciteitswissel) captor.getValue();

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
                capaciteitswissel
                        .getProcesstappen()
                        .size()
        );

        assertSame(
                verantwoordelijke,
                capaciteitswissel
                        .getProcesstappen()
                        .get(0)
                        .getVerantwoordelijke()
        );

        verify(afspraakMapper)
                .naarDetailResponse(
                        capaciteitswissel
                );
    }

    @Test
    void maaktOrderverplaatsingAan() {

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

        AfspraakDetailResponse response =
                maakResponse(
                        "Order verplaatsen",
                        AfspraakType.ORDERVERPLAATSING
                );

        when(afspraakMapper.naarDetailResponse(
                any(OperationeleAfspraak.class)
        )).thenReturn(response);

        OrderverplaatsingData orderverplaatsingData =
                new OrderverplaatsingData(
                        "Tornio",
                        "Avesta",
                        "2B",
                        new BigDecimal("250.000"),
                        LocalDate.of(2026, 10, 20),
                        null
                );

        CreateAfspraakRequest request =
                new CreateAfspraakRequest(
                        "Order verplaatsen",
                        "Order wordt naar andere fabriek verplaatst",
                        "Capaciteit beter benutten",
                        null,
                        null,
                        AfspraakType.ORDERVERPLAATSING,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        verantwoordelijkeId,
                        "Orderverplaatsing uitvoeren",
                        LocalDate.of(2026, 10, 20),
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        orderverplaatsingData
                );

        AfspraakDetailResponse resultaat =
                afspraakService.createAfspraak(
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        ArgumentCaptor<OperationeleAfspraak> captor =
                ArgumentCaptor.forClass(
                        OperationeleAfspraak.class
                );

        verify(afspraakRepository)
                .save(captor.capture());

        assertInstanceOf(
                Orderverplaatsing.class,
                captor.getValue()
        );

        Orderverplaatsing orderverplaatsing =
                (Orderverplaatsing) captor.getValue();

        assertEquals(
                AfspraakType.ORDERVERPLAATSING,
                orderverplaatsing.getType()
        );

        assertEquals(
                "Tornio",
                orderverplaatsing.getVanFabriek()
        );

        assertEquals(
                "Avesta",
                orderverplaatsing.getNaarFabriek()
        );

        assertEquals(
                "2B",
                orderverplaatsing.getFinishType()
        );

        assertEquals(
                new BigDecimal("250.000"),
                orderverplaatsing.getTotaalVolumeTons()
        );

        assertEquals(
                LocalDate.of(2026, 10, 20),
                orderverplaatsing.getGewensteVerplaatsingsdatum()
        );

        assertEquals(
                UitvoeringsStatus.NIET_UITGEVOERD,
                orderverplaatsing.getUitvoeringsstatus()
        );

        assertEquals(
                1,
                orderverplaatsing
                        .getProcesstappen()
                        .size()
        );

        assertSame(
                verantwoordelijke,
                orderverplaatsing
                        .getProcesstappen()
                        .get(0)
                        .getVerantwoordelijke()
        );

        verify(afspraakMapper)
                .naarDetailResponse(
                        orderverplaatsing
                );
    }

    private AfspraakDetailResponse maakResponse(
            String titel,
            AfspraakType type
    ) {

        return new AfspraakDetailResponse(
                null,
                titel,
                null,
                null,
                null,
                null,
                type,
                AfspraakStatus.OPEN,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );
    }

    @Test
    void vindtAfspraakOpId() {

        UUID afspraakId = UUID.randomUUID();

        OperationeleAfspraak afspraak =
                mock(OperationeleAfspraak.class);

        AfspraakDetailResponse response =
                maakResponse(
                        "Bestaande afspraak",
                        AfspraakType.ALGEMEEN
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(Optional.of(afspraak));

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(response);

        AfspraakDetailResponse resultaat =
                afspraakService.vindAfspraak(afspraakId);

        assertSame(
                response,
                resultaat
        );

        verify(afspraakRepository)
                .findById(afspraakId);

        verify(afspraakMapper)
                .naarDetailResponse(afspraak);
    }

    @Test
    void weigertWanneerAfspraakNietBestaat() {

        UUID afspraakId = UUID.randomUUID();

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> afspraakService.vindAfspraak(afspraakId)
                );

        assertEquals(
                "Afspraak niet gevonden",
                exception.getMessage()
        );

        verify(afspraakRepository)
                .findById(afspraakId);

        verifyNoInteractions(
                afspraakMapper
        );
    }

    @Test
    void vindtAlleAfspraken() {

        OperationeleAfspraak afspraak1 =
                mock(OperationeleAfspraak.class);

        OperationeleAfspraak afspraak2 =
                mock(OperationeleAfspraak.class);

        AfspraakSummaryResponse response1 =
                mock(AfspraakSummaryResponse.class);

        AfspraakSummaryResponse response2 =
                mock(AfspraakSummaryResponse.class);

        when(afspraakRepository.findAll())
                .thenReturn(
                        List.of(
                                afspraak1,
                                afspraak2
                        )
                );

        when(afspraakMapper.naarSummaryResponse(afspraak1))
                .thenReturn(response1);

        when(afspraakMapper.naarSummaryResponse(afspraak2))
                .thenReturn(response2);

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.vindAlleAfspraken();

        assertEquals(
                List.of(response1, response2),
                resultaat
        );

        verify(afspraakRepository)
                .findAll();

        verify(afspraakMapper)
                .naarSummaryResponse(afspraak1);

        verify(afspraakMapper)
                .naarSummaryResponse(afspraak2);
    }

}