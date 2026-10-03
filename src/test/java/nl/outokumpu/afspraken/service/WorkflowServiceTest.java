package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Processtap;
import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowServiceTest {

    private ProcesstapRepository processtapRepository;
    private WijzigingRepository wijzigingRepository;
    private AfspraakMapper afspraakMapper;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {

        processtapRepository =
                mock(ProcesstapRepository.class);

        wijzigingRepository =
                mock(WijzigingRepository.class);

        afspraakMapper =
                mock(AfspraakMapper.class);

        workflowService =
                new WorkflowService(
                        processtapRepository,
                        wijzigingRepository,
                        afspraakMapper
                );
    }

    @Test
    void verantwoordelijkeKanProcesstapStarten() {

        UUID processtapId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Operationele afspraak",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        when(processtapRepository.findById(processtapId))
                .thenReturn(
                        Optional.of(processtap)
                );

        when(processtapRepository.save(processtap))
                .thenReturn(processtap);

        when(wijzigingRepository.save(any(Wijziging.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(response);

        AfspraakDetailResponse resultaat =
                workflowService.startProcesstap(
                        processtapId,
                        gebruikerId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                ProcesstapStatus.IN_UITVOERING,
                processtap.getStatus()
        );

        assertNotNull(
                processtap.getGestartOp()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        Wijziging wijziging =
                afspraak.getWijzigingen().get(0);

        assertSame(
                verantwoordelijke,
                wijziging.getGewijzigdDoor()
        );

        assertEquals(
                "NIET_GESTART",
                wijziging.getOudeWaarde()
        );

        assertEquals(
                "IN_UITVOERING",
                wijziging.getNieuweWaarde()
        );

        verify(processtapRepository)
                .findById(processtapId);

        verify(processtapRepository)
                .save(processtap);

        verify(wijzigingRepository)
                .save(wijziging);

        verify(afspraakMapper)
                .naarDetailResponse(afspraak);
    }

    @Test
    void weigertWanneerProcesstapNietBestaat() {

        UUID processtapId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        when(processtapRepository.findById(processtapId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> workflowService.startProcesstap(
                                processtapId,
                                gebruikerId
                        )
                );

        assertEquals(
                "Processtap niet gevonden",
                exception.getMessage()
        );

        verify(processtapRepository)
                .findById(processtapId);

        verify(
                processtapRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                wijzigingRepository,
                afspraakMapper
        );
    }

    @Test
    void weigertWanneerGebruikerNietVerantwoordelijkIs() {

        UUID processtapId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();
        UUID andereGebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(verantwoordelijkeId);

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Operationele afspraak",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        when(processtapRepository.findById(processtapId))
                .thenReturn(
                        Optional.of(processtap)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> workflowService.startProcesstap(
                                processtapId,
                                andereGebruikerId
                        )
                );

        assertEquals(
                "Alleen de verantwoordelijke kan deze processtap starten",
                exception.getMessage()
        );

        assertEquals(
                ProcesstapStatus.NIET_GESTART,
                processtap.getStatus()
        );

        assertNull(
                processtap.getGestartOp()
        );

        assertTrue(
                afspraak.getWijzigingen().isEmpty()
        );

        verify(processtapRepository)
                .findById(processtapId);

        verify(
                processtapRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                wijzigingRepository,
                afspraakMapper
        );
    }

    @Test
    void verantwoordelijkeKanProcesstapAfronden() {

        UUID processtapId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Operationele afspraak",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        processtap.start();

        when(processtapRepository.findById(processtapId))
                .thenReturn(
                        Optional.of(processtap)
                );

        when(processtapRepository.save(processtap))
                .thenReturn(processtap);

        when(wijzigingRepository.save(any(Wijziging.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(afspraakMapper.naarDetailResponse(afspraak))
                .thenReturn(response);

        AfspraakDetailResponse resultaat =
                workflowService.rondProcesstapAf(
                        processtapId,
                        gebruikerId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                ProcesstapStatus.AFGEROND,
                processtap.getStatus()
        );

        assertNotNull(
                processtap.getAfgerondOp()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        Wijziging wijziging =
                afspraak.getWijzigingen().get(0);

        assertEquals(
                "IN_UITVOERING",
                wijziging.getOudeWaarde()
        );

        assertEquals(
                "AFGEROND",
                wijziging.getNieuweWaarde()
        );

        assertSame(
                verantwoordelijke,
                wijziging.getGewijzigdDoor()
        );

        verify(processtapRepository)
                .save(processtap);

        verify(wijzigingRepository)
                .save(wijziging);

        verify(afspraakMapper)
                .naarDetailResponse(afspraak);
    }

    @Test
    void weigertAfrondenWanneerGebruikerNietVerantwoordelijkIs() {

        UUID processtapId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();
        UUID andereGebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(verantwoordelijkeId);

        OperationeleAfspraak afspraak =
                new OperationeleAfspraak(
                        "Operationele afspraak",
                        "Beschrijving",
                        "Reden",
                        null,
                        null,
                        LocalDate.of(2026, 10, 3),
                        LocalDate.of(2026, 10, 31),
                        initiatiefnemer
                );

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        processtap.start();

        when(processtapRepository.findById(processtapId))
                .thenReturn(
                        Optional.of(processtap)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> workflowService.rondProcesstapAf(
                                processtapId,
                                andereGebruikerId
                        )
                );

        assertEquals(
                "Alleen de verantwoordelijke kan deze processtap afronden",
                exception.getMessage()
        );

        assertEquals(
                ProcesstapStatus.IN_UITVOERING,
                processtap.getStatus()
        );

        assertNull(
                processtap.getAfgerondOp()
        );

        assertTrue(
                afspraak.getWijzigingen().isEmpty()
        );

        verify(processtapRepository)
                .findById(processtapId);

        verify(
                processtapRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                wijzigingRepository,
                afspraakMapper
        );
    }
}