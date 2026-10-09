package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.AfspraakFilterRequest;
import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AfspraakFilterServiceTest {

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
    void filtertAfsprakenViaSpecification() {

        OperationeleAfspraak afspraak =
                maakAfspraak();

        AfspraakSummaryResponse response =
                mock(AfspraakSummaryResponse.class);

        AfspraakFilterRequest filter =
                new AfspraakFilterRequest(
                        AfspraakType.ALGEMEEN,
                        AfspraakStatus.OPEN,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                );

        when(
                afspraakRepository.findAll(
                        any(Specification.class),
                        any(Sort.class)
                )
        ).thenReturn(
                List.of(afspraak)
        );

        when(
                afspraakMapper.naarSummaryResponse(
                        afspraak
                )
        ).thenReturn(
                response
        );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.filterAfspraken(
                        filter
                );

        assertEquals(
                1,
                resultaat.size()
        );

        assertSame(
                response,
                resultaat.get(0)
        );

        verify(afspraakRepository)
                .findAll(
                        any(Specification.class),
                        any(Sort.class)
                );

        verify(afspraakMapper)
                .naarSummaryResponse(
                        afspraak
                );
    }

    @Test
    void weigertOngeldigeFilterperiode() {

        AfspraakFilterRequest filter =
                new AfspraakFilterRequest(
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 10, 1)
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                afspraakService.filterAfspraken(
                                        filter
                                )
                );

        assertEquals(
                "Periode van mag niet na periode tot liggen",
                exception.getMessage()
        );

        verifyNoInteractions(
                afspraakRepository,
                afspraakMapper
        );
    }

    private OperationeleAfspraak maakAfspraak() {

        Gebruiker initiatiefnemer =
                mock(Gebruiker.class);

        return new OperationeleAfspraak(
                "Test afspraak",
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