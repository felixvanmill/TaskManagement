package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateGoedkeurdersRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Betrokkenheid;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AfspraakGoedkeurdersServiceTest {

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
    void initiatiefnemerKanGoedkeurdersWijzigen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        UUID oudeGoedkeurderId =
                UUID.randomUUID();

        UUID betrokkeneId =
                UUID.randomUUID();

        UUID nieuweGoedkeurderId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                gebruiker(
                        initiatiefnemerId
                );

        Gebruiker oudeGoedkeurder =
                gebruiker(
                        oudeGoedkeurderId
                );

        Gebruiker betrokkene =
                gebruiker(
                        betrokkeneId
                );

        Gebruiker nieuweGoedkeurder =
                gebruiker(
                        nieuweGoedkeurderId
                );

        OperationeleAfspraak afspraak =
                maakAfspraak(
                        initiatiefnemer
                );

        afspraak.voegBetrokkenheidToe(
                oudeGoedkeurder,
                BetrokkenRol.GOEDKEURDER
        );

        afspraak.voegBevestigingToe(
                oudeGoedkeurder
        );

        afspraak.voegBetrokkenheidToe(
                betrokkene,
                BetrokkenRol.BETROKKENE
        );

        /*
         * Deze gebruiker heeft dus al een bevestiging.
         */
        afspraak.voegBevestigingToe(
                betrokkene
        );

        UpdateGoedkeurdersRequest request =
                new UpdateGoedkeurdersRequest(
                        List.of(
                                betrokkeneId,
                                nieuweGoedkeurderId
                        )
                );

        when(
                afspraakRepository.findById(
                        afspraakId
                )
        ).thenReturn(
                Optional.of(
                        afspraak
                )
        );

        when(
                gebruikerRepository.findAllById(
                        request.goedkeurderIds()
                )
        ).thenReturn(
                List.of(
                        betrokkene,
                        nieuweGoedkeurder
                )
        );

        when(
                afspraakRepository.save(
                        afspraak
                )
        ).thenReturn(
                afspraak
        );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(
                afspraakMapper.naarDetailResponse(
                        afspraak
                )
        ).thenReturn(
                response
        );

        AfspraakDetailResponse resultaat =
                afspraakService.wijzigGoedkeurders(
                        afspraakId,
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        List<Betrokkenheid> goedkeurderRollen =
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(betrokkenheid ->
                                betrokkenheid.getRol()
                                        == BetrokkenRol.GOEDKEURDER
                        )
                        .toList();

        assertEquals(
                2,
                goedkeurderRollen.size()
        );

        assertTrue(
                goedkeurderRollen.stream()
                        .anyMatch(betrokkenheid ->
                                betrokkeneId.equals(
                                        betrokkenheid
                                                .getGebruiker()
                                                .getId()
                                )
                        )
        );

        assertTrue(
                goedkeurderRollen.stream()
                        .anyMatch(betrokkenheid ->
                                nieuweGoedkeurderId.equals(
                                        betrokkenheid
                                                .getGebruiker()
                                                .getId()
                                )
                        )
        );

        assertFalse(
                goedkeurderRollen.stream()
                        .anyMatch(betrokkenheid ->
                                oudeGoedkeurderId.equals(
                                        betrokkenheid
                                                .getGebruiker()
                                                .getId()
                                )
                        )
        );

        /*
         * De gewone BETROKKENE-rol blijft bestaan.
         */
        assertTrue(
                afspraak.getBetrokkenheden()
                        .stream()
                        .anyMatch(betrokkenheid ->
                                betrokkenheid.getRol()
                                        == BetrokkenRol.BETROKKENE
                                        &&
                                        betrokkeneId.equals(
                                                betrokkenheid
                                                        .getGebruiker()
                                                        .getId()
                                        )
                        )
        );

        /*
         * Oude bevestiging blijft als historie bestaan.
         *
         * Betrokkene had al een bevestiging en krijgt dus
         * geen duplicaat.
         *
         * Nieuwe goedkeurder krijgt wel een nieuwe bevestiging.
         */
        assertEquals(
                3,
                afspraak.getBevestigingen()
                        .size()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen()
                        .size()
        );

        Wijziging wijziging =
                afspraak.getWijzigingen()
                        .get(0);

        assertEquals(
                "Goedkeurders",
                wijziging.getOnderdeel()
        );

        assertSame(
                initiatiefnemer,
                wijziging.getGewijzigdDoor()
        );

        assertTrue(
                wijziging.getOudeWaarde()
                        .contains(
                                oudeGoedkeurderId.toString()
                        )
        );

        assertTrue(
                wijziging.getNieuweWaarde()
                        .contains(
                                nieuweGoedkeurderId.toString()
                        )
        );

        verify(afspraakRepository)
                .save(
                        afspraak
                );

        verify(wijzigingRepository)
                .save(
                        wijziging
                );

        verify(afspraakMapper)
                .naarDetailResponse(
                        afspraak
                );
    }

    @Test
    void andereGebruikerKanGoedkeurdersNietWijzigen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        UUID andereGebruikerId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                gebruiker(
                        initiatiefnemerId
                );

        OperationeleAfspraak afspraak =
                maakAfspraak(
                        initiatiefnemer
                );

        UpdateGoedkeurdersRequest request =
                new UpdateGoedkeurdersRequest(
                        List.of()
                );

        when(
                afspraakRepository.findById(
                        afspraakId
                )
        ).thenReturn(
                Optional.of(
                        afspraak
                )
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                afspraakService
                                        .wijzigGoedkeurders(
                                                afspraakId,
                                                request,
                                                andereGebruikerId
                                        )
                );

        assertEquals(
                "Alleen de initiatiefnemer kan goedkeurders wijzigen",
                exception.getMessage()
        );

        verify(
                afspraakRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                gebruikerRepository,
                wijzigingRepository,
                afspraakMapper
        );
    }

    @Test
    void dezelfdeGoedkeurdersMakenGeenNieuweHistorie() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        UUID goedkeurderId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                gebruiker(
                        initiatiefnemerId
                );

        Gebruiker goedkeurder =
                gebruiker(
                        goedkeurderId
                );

        OperationeleAfspraak afspraak =
                maakAfspraak(
                        initiatiefnemer
                );

        afspraak.voegBetrokkenheidToe(
                goedkeurder,
                BetrokkenRol.GOEDKEURDER
        );

        afspraak.voegBevestigingToe(
                goedkeurder
        );

        UpdateGoedkeurdersRequest request =
                new UpdateGoedkeurdersRequest(
                        List.of(
                                goedkeurderId
                        )
                );

        when(
                afspraakRepository.findById(
                        afspraakId
                )
        ).thenReturn(
                Optional.of(
                        afspraak
                )
        );

        when(
                gebruikerRepository.findAllById(
                        request.goedkeurderIds()
                )
        ).thenReturn(
                List.of(
                        goedkeurder
                )
        );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(
                afspraakMapper.naarDetailResponse(
                        afspraak
                )
        ).thenReturn(
                response
        );

        AfspraakDetailResponse resultaat =
                afspraakService.wijzigGoedkeurders(
                        afspraakId,
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        assertTrue(
                afspraak.getWijzigingen()
                        .isEmpty()
        );

        assertEquals(
                1,
                afspraak.getBevestigingen()
                        .size()
        );

        verify(
                afspraakRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                wijzigingRepository
        );
    }

    private OperationeleAfspraak maakAfspraak(
            Gebruiker initiatiefnemer
    ) {

        return new OperationeleAfspraak(
                "Goedkeuringstest",
                "Beschrijving",
                "Reden",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );
    }

    private Gebruiker gebruiker(
            UUID id
    ) {

        Gebruiker gebruiker =
                mock(Gebruiker.class);

        when(
                gebruiker.getId()
        ).thenReturn(
                id
        );

        return gebruiker;
    }
}