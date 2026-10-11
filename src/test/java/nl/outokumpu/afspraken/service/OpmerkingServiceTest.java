package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.CreateOpmerkingRequest;
import nl.outokumpu.afspraken.dto.response.OpmerkingResponse;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Opmerking;

import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.OpmerkingRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpmerkingServiceTest {

    private OpmerkingRepository opmerkingRepository;
    private OperationeleAfspraakRepository afspraakRepository;
    private GebruikerRepository gebruikerRepository;

    private OpmerkingService opmerkingService;

    @BeforeEach
    void setUp() {

        opmerkingRepository =
                mock(
                        OpmerkingRepository.class
                );

        afspraakRepository =
                mock(
                        OperationeleAfspraakRepository.class
                );

        gebruikerRepository =
                mock(
                        GebruikerRepository.class
                );

        opmerkingService =
                new OpmerkingService(
                        opmerkingRepository,
                        afspraakRepository,
                        gebruikerRepository
                );
    }

    @Test
    void voegtOpmerkingToeAanAfspraak() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID auteurId =
                UUID.randomUUID();

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        Afdeling afdeling =
                new Afdeling(
                        "Capacity Management"
                );

        Gebruiker auteur =
                new Gebruiker(
                        "Felix",
                        "felix@example.com",
                        afdeling
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
                gebruikerRepository.findById(
                        auteurId
                )
        ).thenReturn(
                Optional.of(
                        auteur
                )
        );

        when(
                opmerkingRepository.save(
                        any(Opmerking.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        CreateOpmerkingRequest request =
                new CreateOpmerkingRequest(
                        "Dit moet nog worden afgestemd."
                );

        OpmerkingResponse resultaat =
                opmerkingService
                        .voegOpmerkingToe(
                                afspraakId,
                                auteurId,
                                request
                        );

        assertEquals(
                "Dit moet nog worden afgestemd.",
                resultaat.tekst()
        );

        assertEquals(
                "Felix",
                resultaat.auteur()
                        .naam()
        );

        assertEquals(
                "felix@example.com",
                resultaat.auteur()
                        .email()
        );

        assertEquals(
                "Capacity Management",
                resultaat.auteur()
                        .afdeling()
                        .naam()
        );

        verify(
                opmerkingRepository
        ).save(
                any(Opmerking.class)
        );
    }

    @Test
    void onbekendeAfspraakWordtGeweigerdBijToevoegen() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID auteurId =
                UUID.randomUUID();

        when(
                afspraakRepository.findById(
                        afspraakId
                )
        ).thenReturn(
                Optional.empty()
        );

        CreateOpmerkingRequest request =
                new CreateOpmerkingRequest(
                        "Opmerking"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                opmerkingService
                                        .voegOpmerkingToe(
                                                afspraakId,
                                                auteurId,
                                                request
                                        )
                );

        assertEquals(
                "Afspraak niet gevonden",
                exception.getMessage()
        );

        verifyNoInteractions(
                gebruikerRepository,
                opmerkingRepository
        );
    }

    @Test
    void onbekendeAuteurWordtGeweigerd() {

        UUID afspraakId =
                UUID.randomUUID();

        UUID auteurId =
                UUID.randomUUID();

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
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
                gebruikerRepository.findById(
                        auteurId
                )
        ).thenReturn(
                Optional.empty()
        );

        CreateOpmerkingRequest request =
                new CreateOpmerkingRequest(
                        "Opmerking"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                opmerkingService
                                        .voegOpmerkingToe(
                                                afspraakId,
                                                auteurId,
                                                request
                                        )
                );

        assertEquals(
                "Auteur niet gevonden",
                exception.getMessage()
        );

        verify(
                opmerkingRepository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void geeftOpmerkingenVanAfspraakTerug() {

        UUID afspraakId =
                UUID.randomUUID();

        OperationeleAfspraak afspraak =
                mock(
                        OperationeleAfspraak.class
                );

        Afdeling afdeling =
                new Afdeling(
                        "Planning"
                );

        Gebruiker auteur =
                new Gebruiker(
                        "Planner",
                        "planner@example.com",
                        afdeling
                );

        Opmerking eerste =
                new Opmerking(
                        afspraak,
                        auteur,
                        "Eerste opmerking"
                );

        Opmerking tweede =
                new Opmerking(
                        afspraak,
                        auteur,
                        "Tweede opmerking"
                );

        when(
                afspraakRepository.existsById(
                        afspraakId
                )
        ).thenReturn(
                true
        );

        when(
                opmerkingRepository
                        .findByAfspraakIdOrderByAangemaaktOpAsc(
                                afspraakId
                        )
        ).thenReturn(
                List.of(
                        eerste,
                        tweede
                )
        );

        List<OpmerkingResponse> resultaat =
                opmerkingService
                        .vindOpmerkingen(
                                afspraakId
                        );

        assertEquals(
                2,
                resultaat.size()
        );

        assertEquals(
                "Eerste opmerking",
                resultaat.get(0)
                        .tekst()
        );

        assertEquals(
                "Tweede opmerking",
                resultaat.get(1)
                        .tekst()
        );
    }

    @Test
    void onbekendeAfspraakWordtGeweigerdBijOpvragen() {

        UUID afspraakId =
                UUID.randomUUID();

        when(
                afspraakRepository.existsById(
                        afspraakId
                )
        ).thenReturn(
                false
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                opmerkingService
                                        .vindOpmerkingen(
                                                afspraakId
                                        )
                );

        assertEquals(
                "Afspraak niet gevonden",
                exception.getMessage()
        );

        verifyNoInteractions(
                opmerkingRepository
        );
    }
}