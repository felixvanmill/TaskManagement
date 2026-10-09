package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateToewijzingRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.*;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
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

class WorkflowToewijzingServiceTest {

    private ProcesstapRepository processtapRepository;
    private OperationeleAfspraakRepository afspraakRepository;
    private GebruikerRepository gebruikerRepository;
    private AfdelingRepository afdelingRepository;
    private WijzigingRepository wijzigingRepository;
    private AfspraakMapper afspraakMapper;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {

        processtapRepository =
                mock(ProcesstapRepository.class);

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

        workflowService =
                new WorkflowService(
                        processtapRepository,
                        afspraakRepository,
                        gebruikerRepository,
                        afdelingRepository,
                        wijzigingRepository,
                        afspraakMapper
                );
    }

    @Test
    void initiatiefnemerKanToewijzingWijzigen() {

        UUID afspraakId = UUID.randomUUID();
        UUID initiatiefnemerId = UUID.randomUUID();

        UUID oudeBetrokkeneId = UUID.randomUUID();
        UUID nieuweBetrokkeneId = UUID.randomUUID();

        UUID oudeAfdelingId = UUID.randomUUID();
        UUID nieuweAfdelingId = UUID.randomUUID();

        UUID oudeVerantwoordelijkeId = UUID.randomUUID();
        UUID nieuweVerantwoordelijkeId = UUID.randomUUID();

        UUID processtapId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker oudeBetrokkene =
                mock(Gebruiker.class);

        Gebruiker nieuweBetrokkene =
                mock(Gebruiker.class);

        Gebruiker oudeVerantwoordelijke =
                mock(Gebruiker.class);

        Gebruiker nieuweVerantwoordelijke =
                mock(Gebruiker.class);

        Afdeling oudeAfdeling =
                mock(Afdeling.class);

        Afdeling nieuweAfdeling =
                mock(Afdeling.class);

        when(initiatiefnemer.getId())
                .thenReturn(initiatiefnemerId);

        when(oudeBetrokkene.getId())
                .thenReturn(oudeBetrokkeneId);

        when(nieuweBetrokkene.getId())
                .thenReturn(nieuweBetrokkeneId);

        when(oudeVerantwoordelijke.getId())
                .thenReturn(oudeVerantwoordelijkeId);

        when(nieuweVerantwoordelijke.getId())
                .thenReturn(nieuweVerantwoordelijkeId);

        when(oudeAfdeling.getId())
                .thenReturn(oudeAfdelingId);

        when(nieuweAfdeling.getId())
                .thenReturn(nieuweAfdelingId);

        OperationeleAfspraak afspraak =
                maakAfspraak(
                        initiatiefnemer
                );

        afspraak.voegBetrokkenheidToe(
                oudeBetrokkene,
                BetrokkenRol.BETROKKENE
        );

        afspraak.voegBevestigingToe(
                oudeBetrokkene
        );

        afspraak.voegAfdelingToe(
                oudeAfdeling
        );

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        oudeVerantwoordelijke
                );

        UpdateToewijzingRequest request =
                new UpdateToewijzingRequest(
                        List.of(nieuweBetrokkeneId),
                        List.of(nieuweAfdelingId),
                        processtapId,
                        nieuweVerantwoordelijkeId
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        when(
                gebruikerRepository.findAllById(
                        request.betrokkeneIds()
                )
        ).thenReturn(
                List.of(nieuweBetrokkene)
        );

        when(
                afdelingRepository.findAllById(
                        request.afdelingIds()
                )
        ).thenReturn(
                List.of(nieuweAfdeling)
        );

        when(processtapRepository.findById(processtapId))
                .thenReturn(
                        Optional.of(processtap)
                );

        when(
                gebruikerRepository.findById(
                        nieuweVerantwoordelijkeId
                )
        ).thenReturn(
                Optional.of(nieuweVerantwoordelijke)
        );

        when(afspraakRepository.save(afspraak))
                .thenReturn(afspraak);

        when(processtapRepository.save(processtap))
                .thenReturn(processtap);

        when(wijzigingRepository.save(any(Wijziging.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(response);

        AfspraakDetailResponse resultaat =
                workflowService.updateToewijzing(
                        afspraakId,
                        request,
                        initiatiefnemerId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                1,
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(b ->
                                b.getRol()
                                        == BetrokkenRol.BETROKKENE
                        )
                        .count()
        );

        assertSame(
                nieuweBetrokkene,
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(b ->
                                b.getRol()
                                        == BetrokkenRol.BETROKKENE
                        )
                        .findFirst()
                        .orElseThrow()
                        .getGebruiker()
        );

        assertEquals(
                1,
                afspraak.getBetrokkenAfdelingen()
                        .size()
        );

        assertTrue(
                afspraak.getBetrokkenAfdelingen()
                        .contains(nieuweAfdeling)
        );

        assertSame(
                nieuweVerantwoordelijke,
                processtap.getVerantwoordelijke()
        );

        assertEquals(
                2,
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
                "Toewijzing",
                wijziging.getOnderdeel()
        );

        assertSame(
                initiatiefnemer,
                wijziging.getGewijzigdDoor()
        );

        assertNotEquals(
                wijziging.getOudeWaarde(),
                wijziging.getNieuweWaarde()
        );

        verify(afspraakRepository)
                .save(afspraak);

        verify(processtapRepository)
                .save(processtap);

        verify(wijzigingRepository)
                .save(wijziging);

        verify(afspraakMapper)
                .naarDetailResponse(afspraak);
    }

    @Test
    void huidigeVerantwoordelijkeKanNieuweVerantwoordelijkeToewijzen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID initiatiefnemerId =
                UUID.randomUUID();

        UUID huidigeVerantwoordelijkeId =
                UUID.randomUUID();

        UUID nieuweVerantwoordelijkeId =
                UUID.randomUUID();

        UUID processtapId =
                UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker huidigeVerantwoordelijke =
                mock(Gebruiker.class);

        Gebruiker nieuweVerantwoordelijke =
                mock(Gebruiker.class);

        when(initiatiefnemer.getId())
                .thenReturn(initiatiefnemerId);

        when(huidigeVerantwoordelijke.getId())
                .thenReturn(
                        huidigeVerantwoordelijkeId
                );

        when(nieuweVerantwoordelijke.getId())
                .thenReturn(
                        nieuweVerantwoordelijkeId
                );

        OperationeleAfspraak afspraak =
                maakAfspraak(
                        initiatiefnemer
                );

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        huidigeVerantwoordelijke
                );

        UpdateToewijzingRequest request =
                new UpdateToewijzingRequest(
                        List.of(),
                        List.of(),
                        processtapId,
                        nieuweVerantwoordelijkeId
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        when(
                gebruikerRepository.findAllById(
                        List.of()
                )
        ).thenReturn(
                List.of()
        );

        when(
                afdelingRepository.findAllById(
                        List.of()
                )
        ).thenReturn(
                List.of()
        );

        when(processtapRepository.findById(processtapId))
                .thenReturn(
                        Optional.of(processtap)
                );

        when(
                gebruikerRepository.findById(
                        nieuweVerantwoordelijkeId
                )
        ).thenReturn(
                Optional.of(nieuweVerantwoordelijke)
        );

        when(wijzigingRepository.save(any(Wijziging.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(response);

        AfspraakDetailResponse resultaat =
                workflowService.updateToewijzing(
                        afspraakId,
                        request,
                        huidigeVerantwoordelijkeId
                );

        assertSame(
                response,
                resultaat
        );

        assertSame(
                nieuweVerantwoordelijke,
                processtap.getVerantwoordelijke()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen()
                        .size()
        );

        assertSame(
                huidigeVerantwoordelijke,
                afspraak.getWijzigingen()
                        .get(0)
                        .getGewijzigdDoor()
        );
    }

    @Test
    void onbevoegdeGebruikerKanToewijzingNietWijzigen() {

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
                maakAfspraak(
                        initiatiefnemer
                );

        afspraak.voegProcesstapToe(
                "Eerste beoordeling",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        UpdateToewijzingRequest request =
                new UpdateToewijzingRequest(
                        List.of(),
                        List.of(),
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                workflowService.updateToewijzing(
                                        afspraakId,
                                        request,
                                        andereGebruikerId
                                )
                );

        assertEquals(
                "Gebruiker is niet bevoegd om de toewijzing te wijzigen",
                exception.getMessage()
        );

        verifyNoInteractions(
                gebruikerRepository,
                afdelingRepository,
                processtapRepository,
                wijzigingRepository,
                afspraakMapper
        );

        verify(
                afspraakRepository,
                never()
        ).save(any());
    }

    private OperationeleAfspraak maakAfspraak(
            Gebruiker initiatiefnemer
    ) {

        return new OperationeleAfspraak(
                "Operationele afspraak",
                "Beschrijving",
                "Reden",
                null,
                null,
                LocalDate.of(2026, 10, 9),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );
    }
}