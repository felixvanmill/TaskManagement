package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfspraakSummaryResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
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

class AfspraakOverzichtServiceTest {

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
    void haaltAlleenActieveAfsprakenOp() {

        OperationeleAfspraak openAfspraak =
                maakAfspraak(
                        "Open afspraak"
                );

        OperationeleAfspraak inBehandeling =
                maakAfspraak(
                        "Afspraak in behandeling"
                );

        inBehandeling.wijzigStatus(
                AfspraakStatus.IN_BEHANDELING
        );

        AfspraakSummaryResponse openResponse =
                mock(AfspraakSummaryResponse.class);

        AfspraakSummaryResponse behandelingResponse =
                mock(AfspraakSummaryResponse.class);

        when(
                afspraakRepository
                        .findByStatusNotOrderByLaatstGewijzigdOpDesc(
                                AfspraakStatus.AFGEROND
                        )
        ).thenReturn(
                List.of(
                        openAfspraak,
                        inBehandeling
                )
        );

        when(
                afspraakMapper.naarSummaryResponse(
                        openAfspraak
                )
        ).thenReturn(
                openResponse
        );

        when(
                afspraakMapper.naarSummaryResponse(
                        inBehandeling
                )
        ).thenReturn(
                behandelingResponse
        );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.vindActieveAfspraken();

        assertEquals(
                2,
                resultaat.size()
        );

        assertSame(
                openResponse,
                resultaat.get(0)
        );

        assertSame(
                behandelingResponse,
                resultaat.get(1)
        );

        verify(afspraakRepository)
                .findByStatusNotOrderByLaatstGewijzigdOpDesc(
                        AfspraakStatus.AFGEROND
                );

        verify(afspraakMapper)
                .naarSummaryResponse(
                        openAfspraak
                );

        verify(afspraakMapper)
                .naarSummaryResponse(
                        inBehandeling
                );
    }

    @Test
    void haaltAlleenAfgerondeAfsprakenOpVoorArchief() {

        OperationeleAfspraak afgerondeAfspraak =
                maakAfspraak(
                        "Afgeronde afspraak"
                );

        afgerondeAfspraak.wijzigStatus(
                AfspraakStatus.AFGEROND
        );

        AfspraakSummaryResponse response =
                mock(AfspraakSummaryResponse.class);

        when(
                afspraakRepository
                        .findByStatusOrderByLaatstGewijzigdOpDesc(
                                AfspraakStatus.AFGEROND
                        )
        ).thenReturn(
                List.of(
                        afgerondeAfspraak
                )
        );

        when(
                afspraakMapper.naarSummaryResponse(
                        afgerondeAfspraak
                )
        ).thenReturn(
                response
        );

        List<AfspraakSummaryResponse> resultaat =
                afspraakService.vindAfgerondeAfspraken();

        assertEquals(
                1,
                resultaat.size()
        );

        assertSame(
                response,
                resultaat.get(0)
        );

        verify(afspraakRepository)
                .findByStatusOrderByLaatstGewijzigdOpDesc(
                        AfspraakStatus.AFGEROND
                );

        verify(afspraakMapper)
                .naarSummaryResponse(
                        afgerondeAfspraak
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
                LocalDate.of(2026, 10, 9),
                LocalDate.of(2026, 10, 31),
                initiatiefnemer
        );
    }
}