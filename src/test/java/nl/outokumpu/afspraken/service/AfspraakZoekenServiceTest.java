package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AfspraakZoekenServiceTest {

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
    void zoektAfsprakenOpTrefwoord() {

        OperationeleAfspraak eersteAfspraak =
                maakAfspraak(
                        "Capaciteitsplanning"
                );

        OperationeleAfspraak tweedeAfspraak =
                maakAfspraak(
                        "Productieafspraak"
                );

        AfspraakSummaryResponse eersteResponse =
                mock(AfspraakSummaryResponse.class);

        AfspraakSummaryResponse tweedeResponse =
                mock(AfspraakSummaryResponse.class);

        when(
                afspraakRepository
                        .zoekOpTrefwoord(
                                "planning"
                        )
        ).thenReturn(
                List.of(
                        eersteAfspraak,
                        tweedeAfspraak
                )
        );

        when(
                afspraakMapper.naarSummaryResponse(
                        eersteAfspraak
                )
        ).thenReturn(
                eersteResponse
        );

        when(
                afspraakMapper.naarSummaryResponse(
                        tweedeAfspraak
                )
        ).thenReturn(
                tweedeResponse
        );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.zoekAfspraken(
                        "  planning  "
                );

        assertEquals(
                2,
                resultaat.size()
        );

        assertSame(
                eersteResponse,
                resultaat.get(0)
        );

        assertSame(
                tweedeResponse,
                resultaat.get(1)
        );

        verify(afspraakRepository)
                .zoekOpTrefwoord(
                        "planning"
                );

        verify(afspraakMapper)
                .naarSummaryResponse(
                        eersteAfspraak
                );

        verify(afspraakMapper)
                .naarSummaryResponse(
                        tweedeAfspraak
                );
    }

    @Test
    void weigertLegeZoekterm() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                afspraakService
                                        .zoekAfspraken(
                                                "   "
                                        )
                );

        assertEquals(
                "Zoekterm is verplicht",
                exception.getMessage()
        );

        verifyNoInteractions(
                afspraakRepository,
                afspraakMapper
        );
    }

    private OperationeleAfspraak maakAfspraak(
            String titel
    ) {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        return new OperationeleAfspraak(
                titel,
                "Beschrijving",
                "Reden",
                null,
                null,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );
    }
}