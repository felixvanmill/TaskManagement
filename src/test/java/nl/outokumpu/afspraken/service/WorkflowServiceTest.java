package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Orderverplaatsing;
import nl.outokumpu.afspraken.entity.Processtap;
import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkflowServiceTest {

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
                maakAfspraak(initiatiefnemer);

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        when(processtapRepository.findById(processtapId))
                .thenReturn(Optional.of(processtap));

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
                        () ->
                                workflowService.startProcesstap(
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
                afspraakRepository,
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
                maakAfspraak(initiatiefnemer);

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        when(processtapRepository.findById(processtapId))
                .thenReturn(Optional.of(processtap));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                workflowService.startProcesstap(
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
                afspraakRepository,
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
                maakAfspraak(initiatiefnemer);

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        processtap.start();

        when(processtapRepository.findById(processtapId))
                .thenReturn(Optional.of(processtap));

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
                maakAfspraak(initiatiefnemer);

        Processtap processtap =
                afspraak.voegProcesstapToe(
                        "Eerste beoordeling",
                        1,
                        LocalDate.of(2026, 10, 20),
                        verantwoordelijke
                );

        processtap.start();

        when(processtapRepository.findById(processtapId))
                .thenReturn(Optional.of(processtap));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                workflowService.rondProcesstapAf(
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
                afspraakRepository,
                wijzigingRepository,
                afspraakMapper
        );
    }

    @Test
    void huidigeVerantwoordelijkeKanAfspraakstatusWijzigen() {

        UUID afspraakId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                maakAfspraak(initiatiefnemer);

        afspraak.voegProcesstapToe(
                "Eerste beoordeling",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(Optional.of(afspraak));

        when(afspraakRepository.save(afspraak))
                .thenReturn(afspraak);

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
                workflowService.wijzigAfspraakStatus(
                        afspraakId,
                        AfspraakStatus.IN_BEHANDELING,
                        gebruikerId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                AfspraakStatus.IN_BEHANDELING,
                afspraak.getStatus()
        );

        assertEquals(
                1,
                afspraak.getWijzigingen().size()
        );

        Wijziging wijziging =
                afspraak.getWijzigingen().get(0);

        assertEquals(
                "Afspraakstatus",
                wijziging.getOnderdeel()
        );

        assertEquals(
                "OPEN",
                wijziging.getOudeWaarde()
        );

        assertEquals(
                "IN_BEHANDELING",
                wijziging.getNieuweWaarde()
        );

        assertSame(
                verantwoordelijke,
                wijziging.getGewijzigdDoor()
        );

        verify(afspraakRepository)
                .save(afspraak);

        verify(wijzigingRepository)
                .save(wijziging);

        verify(afspraakMapper)
                .naarDetailResponse(afspraak);
    }

    @Test
    void andereGebruikerKanAfspraakstatusNietWijzigen() {

        UUID afspraakId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();
        UUID andereGebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(verantwoordelijkeId);

        OperationeleAfspraak afspraak =
                maakAfspraak(initiatiefnemer);

        afspraak.voegProcesstapToe(
                "Eerste beoordeling",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(Optional.of(afspraak));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                workflowService.wijzigAfspraakStatus(
                                        afspraakId,
                                        AfspraakStatus.IN_BEHANDELING,
                                        andereGebruikerId
                                )
                );

        assertEquals(
                "Alleen de huidige verantwoordelijke kan de afspraakstatus wijzigen",
                exception.getMessage()
        );

        assertEquals(
                AfspraakStatus.OPEN,
                afspraak.getStatus()
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

    @Test
    void huidigeVerantwoordelijkeKanUitvoeringsstatusWijzigen() {

        UUID afspraakId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(gebruikerId);

        Orderverplaatsing orderverplaatsing =
                maakOrderverplaatsing(
                        initiatiefnemer
                );

        orderverplaatsing.voegProcesstapToe(
                "Order uitvoeren",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(orderverplaatsing)
                );

        when(afspraakRepository.save(orderverplaatsing))
                .thenReturn(orderverplaatsing);

        when(wijzigingRepository.save(any(Wijziging.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        AfspraakDetailResponse response =
                mock(AfspraakDetailResponse.class);

        when(
                afspraakMapper.naarDetailResponse(
                        orderverplaatsing
                )
        ).thenReturn(response);

        AfspraakDetailResponse resultaat =
                workflowService.wijzigUitvoeringsstatus(
                        afspraakId,
                        UitvoeringsStatus.IN_UITVOERING,
                        gebruikerId
                );

        assertSame(
                response,
                resultaat
        );

        assertEquals(
                UitvoeringsStatus.IN_UITVOERING,
                orderverplaatsing.getUitvoeringsstatus()
        );

        assertEquals(
                1,
                orderverplaatsing.getWijzigingen().size()
        );

        Wijziging wijziging =
                orderverplaatsing
                        .getWijzigingen()
                        .get(0);

        assertEquals(
                "Uitvoeringsstatus",
                wijziging.getOnderdeel()
        );

        assertEquals(
                "NIET_UITGEVOERD",
                wijziging.getOudeWaarde()
        );

        assertEquals(
                "IN_UITVOERING",
                wijziging.getNieuweWaarde()
        );

        assertSame(
                verantwoordelijke,
                wijziging.getGewijzigdDoor()
        );

        verify(afspraakRepository)
                .save(orderverplaatsing);

        verify(wijzigingRepository)
                .save(wijziging);

        verify(afspraakMapper)
                .naarDetailResponse(orderverplaatsing);
    }

    @Test
    void weigertUitvoeringsstatusVoorNietOrderverplaatsing() {

        UUID afspraakId = UUID.randomUUID();
        UUID gebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        OperationeleAfspraak afspraak =
                maakAfspraak(initiatiefnemer);

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(afspraak)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                workflowService.wijzigUitvoeringsstatus(
                                        afspraakId,
                                        UitvoeringsStatus.IN_UITVOERING,
                                        gebruikerId
                                )
                );

        assertEquals(
                "Afspraak is geen orderverplaatsing",
                exception.getMessage()
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

    @Test
    void andereGebruikerKanUitvoeringsstatusNietWijzigen() {

        UUID afspraakId = UUID.randomUUID();
        UUID verantwoordelijkeId = UUID.randomUUID();
        UUID andereGebruikerId = UUID.randomUUID();

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        Gebruiker verantwoordelijke =
                mock(Gebruiker.class);

        when(verantwoordelijke.getId())
                .thenReturn(verantwoordelijkeId);

        Orderverplaatsing orderverplaatsing =
                maakOrderverplaatsing(
                        initiatiefnemer
                );

        orderverplaatsing.voegProcesstapToe(
                "Order uitvoeren",
                1,
                LocalDate.of(2026, 10, 20),
                verantwoordelijke
        );

        when(afspraakRepository.findById(afspraakId))
                .thenReturn(
                        Optional.of(orderverplaatsing)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                workflowService.wijzigUitvoeringsstatus(
                                        afspraakId,
                                        UitvoeringsStatus.IN_UITVOERING,
                                        andereGebruikerId
                                )
                );

        assertEquals(
                "Alleen de huidige verantwoordelijke kan de uitvoeringsstatus wijzigen",
                exception.getMessage()
        );

        assertEquals(
                UitvoeringsStatus.NIET_UITGEVOERD,
                orderverplaatsing.getUitvoeringsstatus()
        );

        assertTrue(
                orderverplaatsing
                        .getWijzigingen()
                        .isEmpty()
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

    private OperationeleAfspraak maakAfspraak(
            Gebruiker initiatiefnemer
    ) {

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

    private Orderverplaatsing maakOrderverplaatsing(
            Gebruiker initiatiefnemer
    ) {

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