package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;
import nl.outokumpu.afspraken.dto.request.UpdateAfspraakRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Orderverplaatsing;
import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

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

class AfspraakBewerkenServiceTest {

    private OperationeleAfspraakRepository afspraakRepository;
    private GebruikerRepository gebruikerRepository;
    private AfdelingRepository afdelingRepository;
    private WijzigingRepository wijzigingRepository;
    private AfspraakMapper afspraakMapper;

    private AfspraakService afspraakService;

    @BeforeEach
    void setUp() {

        afspraakRepository =
                mock(OperationeleAfspraakRepository.class);

        gebruikerRepository =
                mock(GebruikerRepository.class);

        afdelingRepository =
                mock(AfdelingRepository.class);

        wijzigingRepository =
                mock(WijzigingRepository.class);

        afspraakMapper =
                mock(AfspraakMapper.class);

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
    void initiatiefnemerKanAlgemeneAfspraakWijzigen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        when(initiatiefnemer.getId())
                .thenReturn(
                        initiatiefnemerId
                );

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Oude titel",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        UpdateAfspraakRequest request =
                new UpdateAfspraakRequest(
                        "Nieuwe titel",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        null,
                        null
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        when(afspraakRepository.save(afspraak))
                .thenReturn(
                        afspraak
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(
                        response
                );

        AfspraakDetailResponse resultaat =
                afspraakService.updateAfspraak(
                        afspraakId,
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                "Nieuwe titel",
                afspraak.getTitel()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        Wijziging wijziging =
                afspraak.getWijzigingen()
                        .get(0);

        assertEquals(
                "Titel",
                wijziging.getOnderdeel()
        );

        assertEquals(
                "Oude titel",
                wijziging.getOudeWaarde()
        );

        assertEquals(
                "Nieuwe titel",
                wijziging.getNieuweWaarde()
        );

        assertSame(
                initiatiefnemer,
                wijziging.getGewijzigdDoor()
        );

        verify(afspraakRepository)
                .save(afspraak);

        verify(wijzigingRepository)
                .saveAll(
                        afspraak.getWijzigingen()
                );

        verify(afspraakMapper)
                .naarDetailResponse(afspraak);
    }

    @Test
    void huidigeVerantwoordelijkeKanAfspraakWijzigen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        UUID verantwoordelijkeId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(initiatiefnemer.getId())
                .thenReturn(
                        initiatiefnemerId
                );

        when(verantwoordelijke.getId())
                .thenReturn(
                        verantwoordelijkeId
                );

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Titel",
                        "Oude beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        afspraak.voegProcesstapToe(
                "Uitvoeren",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        UpdateAfspraakRequest request =
                new UpdateAfspraakRequest(
                        "Titel",
                        "Nieuwe beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        null,
                        null
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        when(afspraakRepository.save(afspraak))
                .thenReturn(
                        afspraak
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(
                        response
                );

        AfspraakDetailResponse resultaat =
                afspraakService.updateAfspraak(
                        afspraakId,
                        request,
                        verantwoordelijkeId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                "Nieuwe beschrijving",
                afspraak.getBeschrijving()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        assertSame(
                verantwoordelijke,
                afspraak.getWijzigingen()
                        .get(0)
                        .getGewijzigdDoor()
        );
    }

    @Test
    void wijzigtCapaciteitswisselgegevens() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        when(initiatiefnemer.getId())
                .thenReturn(
                        initiatiefnemerId
                );

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

        CapaciteitswisselData data =
                new CapaciteitswisselData(
                        "Avesta",
                        "2B",
                        "304L",
                        new BigDecimal("100.000"),
                        new BigDecimal("125.000"),
                        "ton",
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                );

        UpdateAfspraakRequest request =
                new UpdateAfspraakRequest(
                        "Capaciteitswissel",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        data,
                        null
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        when(afspraakRepository.save(afspraak))
                .thenReturn(
                        afspraak
                );

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(
                        mock(AfspraakDetailResponse.class)
                );

        afspraakService.updateAfspraak(
                afspraakId,
                request,
                initiatiefnemerId
        );

        assertEquals(
                "Avesta",
                afspraak.getFabriek()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        assertEquals(
                "Fabriek",
                afspraak.getWijzigingen()
                        .get(0)
                        .getOnderdeel()
        );

        assertEquals(
                "Tornio",
                afspraak.getWijzigingen()
                        .get(0)
                        .getOudeWaarde()
        );

        assertEquals(
                "Avesta",
                afspraak.getWijzigingen()
                        .get(0)
                        .getNieuweWaarde()
        );
    }

    @Test
    void wijzigtOrderverplaatsingZonderUitvoeringsstatusTeWijzigen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        when(initiatiefnemer.getId())
                .thenReturn(
                        initiatiefnemerId
                );

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

        OrderverplaatsingData data =
                new OrderverplaatsingData(
                        "Tornio",
                        "Avesta",
                        "2B",
                        new BigDecimal("150.000"),
                        LocalDate.of(2026, 10, 15),
                        null
                );

        UpdateAfspraakRequest request =
                new UpdateAfspraakRequest(
                        "Orderverplaatsing",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        null,
                        data
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        when(afspraakRepository.save(afspraak))
                .thenReturn(
                        afspraak
                );

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(
                        mock(AfspraakDetailResponse.class)
                );

        afspraakService.updateAfspraak(
                afspraakId,
                request,
                initiatiefnemerId
        );

        assertEquals(
                new BigDecimal("150.000"),
                afspraak.getTotaalVolumeTons()
        );

        assertEquals(
                UitvoeringsStatus.IN_UITVOERING,
                afspraak.getUitvoeringsstatus()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        assertEquals(
                "Totaal volume tons",
                afspraak.getWijzigingen()
                        .get(0)
                        .getOnderdeel()
        );
    }

    @Test
    void onbevoegdeGebruikerKanAfspraakNietWijzigen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        UUID verantwoordelijkeId =
                UUID.randomUUID();

        UUID andereGebruikerId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(initiatiefnemer.getId())
                .thenReturn(
                        initiatiefnemerId
                );

        when(verantwoordelijke.getId())
                .thenReturn(
                        verantwoordelijkeId
                );

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

        afspraak.voegProcesstapToe(
                "Uitvoeren",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        UpdateAfspraakRequest request =
                new UpdateAfspraakRequest(
                        "Nieuwe titel",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        null,
                        null
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                afspraakService.updateAfspraak(
                                        afspraakId,
                                        request,
                                        andereGebruikerId
                                )
                );

        assertEquals(
                "Gebruiker is niet bevoegd om de afspraak te wijzigen",
                exception.getMessage()
        );

        assertEquals(
                "Titel",
                afspraak.getTitel()
        );

        assertTrue(
                afspraak.getWijzigingen().isEmpty()
        );

        verify(
                afspraakRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                wijzigingRepository,
                afspraakMapper
        );
    }
}