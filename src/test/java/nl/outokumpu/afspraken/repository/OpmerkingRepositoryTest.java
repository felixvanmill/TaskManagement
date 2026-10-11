package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Opmerking;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OpmerkingRepositoryTest {

    @Autowired
    private OpmerkingRepository opmerkingRepository;

    @Autowired
    private OperationeleAfspraakRepository afspraakRepository;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private AfdelingRepository afdelingRepository;

    @Test
    void slaatOpmerkingOpEnVindtDezePerAfspraak() {

        Afdeling afdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Capacity Management"
                        )
                );

        Gebruiker gebruiker =
                gebruikerRepository.saveAndFlush(
                        new Gebruiker(
                                "Test Gebruiker",
                                "opmerking@example.com",
                                afdeling
                        )
                );

        OperationeleAfspraak afspraak =
                afspraakRepository.saveAndFlush(
                        new OperationeleAfspraak(
                                "Test afspraak",
                                "Beschrijving",
                                "Reden",
                                null,
                                null,
                                LocalDate.of(
                                        2026,
                                        10,
                                        11
                                ),
                                LocalDate.of(
                                        2026,
                                        10,
                                        31
                                ),
                                gebruiker
                        )
                );

        Opmerking opmerking =
                opmerkingRepository.saveAndFlush(
                        new Opmerking(
                                afspraak,
                                gebruiker,
                                "Deze afspraak moet nog worden afgestemd."
                        )
                );

        List<Opmerking> resultaat =
                opmerkingRepository
                        .findByAfspraakIdOrderByAangemaaktOpAsc(
                                afspraak.getId()
                        );

        assertEquals(
                1,
                resultaat.size()
        );

        Opmerking gevonden =
                resultaat.get(0);

        assertEquals(
                opmerking.getId(),
                gevonden.getId()
        );

        assertEquals(
                "Deze afspraak moet nog worden afgestemd.",
                gevonden.getTekst()
        );

        assertEquals(
                gebruiker.getId(),
                gevonden.getAuteur()
                        .getId()
        );

        assertEquals(
                afspraak.getId(),
                gevonden.getAfspraak()
                        .getId()
        );

        assertNotNull(
                gevonden.getAangemaaktOp()
        );
    }

    @Test
    void haaltAlleenOpmerkingenVanGevraagdeAfspraakOp() {

        Afdeling afdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Planning"
                        )
                );

        Gebruiker gebruiker =
                gebruikerRepository.saveAndFlush(
                        new Gebruiker(
                                "Planner",
                                "planner-opmerking@example.com",
                                afdeling
                        )
                );

        OperationeleAfspraak eersteAfspraak =
                afspraakRepository.saveAndFlush(
                        maakAfspraak(
                                "Eerste afspraak",
                                gebruiker
                        )
                );

        OperationeleAfspraak tweedeAfspraak =
                afspraakRepository.saveAndFlush(
                        maakAfspraak(
                                "Tweede afspraak",
                                gebruiker
                        )
                );

        opmerkingRepository.saveAndFlush(
                new Opmerking(
                        eersteAfspraak,
                        gebruiker,
                        "Opmerking één"
                )
        );

        opmerkingRepository.saveAndFlush(
                new Opmerking(
                        tweedeAfspraak,
                        gebruiker,
                        "Opmerking twee"
                )
        );

        List<Opmerking> resultaat =
                opmerkingRepository
                        .findByAfspraakIdOrderByAangemaaktOpAsc(
                                eersteAfspraak.getId()
                        );

        assertEquals(
                1,
                resultaat.size()
        );

        assertEquals(
                "Opmerking één",
                resultaat.get(0)
                        .getTekst()
        );
    }

    private OperationeleAfspraak maakAfspraak(
            String titel,
            Gebruiker initiatiefnemer
    ) {

        return new OperationeleAfspraak(
                titel,
                "Beschrijving",
                "Reden",
                null,
                null,
                LocalDate.of(
                        2026,
                        10,
                        11
                ),
                LocalDate.of(
                        2026,
                        10,
                        31
                ),
                initiatiefnemer
        );
    }
}