package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.repository.AfdelingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AfdelingServiceTest {

    private AfdelingRepository afdelingRepository;
    private AfdelingService afdelingService;

    @BeforeEach
    void setUp() {
        afdelingRepository = mock(AfdelingRepository.class);
        afdelingService = new AfdelingService(afdelingRepository);
    }

    @Test
    void geeftAlleAfdelingenAlsResponseTerug() {

        UUID afdelingId = UUID.randomUUID();

        Afdeling afdeling = Mockito.mock(Afdeling.class);

        when(afdeling.getId()).thenReturn(afdelingId);
        when(afdeling.getNaam()).thenReturn("Capacity Planning");

        when(afdelingRepository.findAll())
                .thenReturn(List.of(afdeling));

        List<AfdelingResponse> resultaat =
                afdelingService.vindAlleAfdelingen();

        assertEquals(1, resultaat.size());

        assertEquals(
                afdelingId,
                resultaat.get(0).id()
        );

        assertEquals(
                "Capacity Planning",
                resultaat.get(0).naam()
        );

        verify(afdelingRepository, times(1))
                .findAll();
    }
}