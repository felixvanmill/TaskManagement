package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.BevestigingResponse;
import nl.outokumpu.afspraken.entity.Bevestiging;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.Beslissing;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.repository.BevestigingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BevestigingServiceTest {

    private BevestigingRepository bevestigingRepository;

    private BevestigingService bevestigingService;

    @BeforeEach
    void setUp() {

        bevestigingRepository =
                mock(BevestigingRepository.class);

        bevestigingService =
                new BevestigingService(
                        bevestigingRepository
                );
    }

    @Test
    void betrokkenGebruikerKanAfspraakAlsGezienMarkeren() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        Gebruiker gebruiker =
                mock(Gebruiker.class);

        when(gebruiker.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                maakAfspraak();

        Bevestiging bevestiging =
                afspraak.voegBevestigingToe(
                        gebruiker
                );

        when(
                bevestigingRepository
                        .findByAfspraakIdAndGebruikerId(
                                afspraakId,
                                gebruikerId
                        )
        ).thenReturn(
                Optional.of(bevestiging)
        );

        when(
                bevestigingRepository.save(
                        bevestiging
                )
        ).thenReturn(
                bevestiging
        );

        BevestigingResponse resultaat =
                bevestigingService.markeerGezien(
                        afspraakId,
                        gebruikerId
                );

        assertTrue(
                resultaat.gezien()
        );

        assertNotNull(
                resultaat.gezienOp()
        );

        assertEquals(
                Beslissing.GEEN,
                resultaat.beslissing()
        );

        assertEquals(
                gebruikerId,
                resultaat.gebruiker().id()
        );

        verify(bevestigingRepository)
                .save(bevestiging);
    }

    @Test
    void goedkeurderKanAfspraakGoedkeuren() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        Gebruiker goedkeurder =
                mock(Gebruiker.class);

        when(goedkeurder.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                maakAfspraak();

        afspraak.voegBetrokkenheidToe(
                goedkeurder,
                BetrokkenRol.GOEDKEURDER
        );

        Bevestiging bevestiging =
                afspraak.voegBevestigingToe(
                        goedkeurder
                );

        when(
                bevestigingRepository
                        .findByAfspraakIdAndGebruikerId(
                                afspraakId,
                                gebruikerId
                        )
        ).thenReturn(
                Optional.of(bevestiging)
        );

        when(
                bevestigingRepository.save(
                        bevestiging
                )
        ).thenReturn(
                bevestiging
        );

        BevestigingResponse resultaat =
                bevestigingService.registreerBeslissing(
                        afspraakId,
                        gebruikerId,
                        Beslissing.GOEDGEKEURD
                );

        assertEquals(
                Beslissing.GOEDGEKEURD,
                resultaat.beslissing()
        );

        assertNotNull(
                resultaat.beslistOp()
        );

        verify(bevestigingRepository)
                .save(bevestiging);
    }

    @Test
    void goedkeurderKanAfspraakAfwijzen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        Gebruiker goedkeurder =
                mock(Gebruiker.class);

        when(goedkeurder.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                maakAfspraak();

        afspraak.voegBetrokkenheidToe(
                goedkeurder,
                BetrokkenRol.GOEDKEURDER
        );

        Bevestiging bevestiging =
                afspraak.voegBevestigingToe(
                        goedkeurder
                );

        when(
                bevestigingRepository
                        .findByAfspraakIdAndGebruikerId(
                                afspraakId,
                                gebruikerId
                        )
        ).thenReturn(
                Optional.of(bevestiging)
        );

        when(
                bevestigingRepository.save(
                        bevestiging
                )
        ).thenReturn(
                bevestiging
        );

        BevestigingResponse resultaat =
                bevestigingService.registreerBeslissing(
                        afspraakId,
                        gebruikerId,
                        Beslissing.AFGEWEZEN
                );

        assertEquals(
                Beslissing.AFGEWEZEN,
                resultaat.beslissing()
        );

        assertNotNull(
                resultaat.beslistOp()
        );
    }

    @Test
    void gewoneBetrokkeneKanGeenBeslissingRegistreren() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        Gebruiker betrokkene =
                mock(Gebruiker.class);

        when(betrokkene.getId())
                .thenReturn(gebruikerId);

        OperationeleAfspraak afspraak =
                maakAfspraak();

        afspraak.voegBetrokkenheidToe(
                betrokkene,
                BetrokkenRol.BETROKKENE
        );

        Bevestiging bevestiging =
                afspraak.voegBevestigingToe(
                        betrokkene
                );

        when(
                bevestigingRepository
                        .findByAfspraakIdAndGebruikerId(
                                afspraakId,
                                gebruikerId
                        )
        ).thenReturn(
                Optional.of(bevestiging)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                bevestigingService
                                        .registreerBeslissing(
                                                afspraakId,
                                                gebruikerId,
                                                Beslissing.GOEDGEKEURD
                                        )
                );

        assertEquals(
                "Alleen een aangewezen goedkeurder kan een beslissing registreren",
                exception.getMessage()
        );

        assertEquals(
                Beslissing.GEEN,
                bevestiging.getBeslissing()
        );

        verify(
                bevestigingRepository,
                never()
        ).save(any());
    }

    @Test
    void weigertWanneerBevestigingNietBestaat() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID gebruikerId =
                UUID.randomUUID();

        when(
                bevestigingRepository
                        .findByAfspraakIdAndGebruikerId(
                                afspraakId,
                                gebruikerId
                        )
        ).thenReturn(
                Optional.empty()
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                bevestigingService.markeerGezien(
                                        afspraakId,
                                        gebruikerId
                                )
                );

        assertEquals(
                "Bevestiging niet gevonden",
                exception.getMessage()
        );

        verify(
                bevestigingRepository,
                never()
        ).save(any());
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
                LocalDate.of(2026, 10, 9),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );
    }
}