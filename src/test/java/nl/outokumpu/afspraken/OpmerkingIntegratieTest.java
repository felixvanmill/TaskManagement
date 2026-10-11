package nl.outokumpu.afspraken;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Opmerking;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.OpmerkingRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OpmerkingIntegratieTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AfdelingRepository afdelingRepository;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private OperationeleAfspraakRepository afspraakRepository;

    @Autowired
    private OpmerkingRepository opmerkingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void anoniemeGebruikerKanOpmerkingenNietOpvragen()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/afspraken/{afspraakId}/opmerkingen",
                                UUID.randomUUID()
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void ingelogdeGebruikerKanOpmerkingToevoegenEnTeruglezen()
            throws Exception {

        Gebruiker gebruiker =
                maakGebruiker(
                        "Opmerking Gebruiker",
                        "opmerking-integratie@example.com",
                        "SterkWachtwoord123!"
                );

        OperationeleAfspraak afspraak =
                maakAfspraak(
                        "Integratie afspraak",
                        gebruiker
                );

        MockHttpSession session =
                login(
                        gebruiker.getEmail(),
                        "SterkWachtwoord123!"
                );

        String json = """
                {
                  "tekst": "Deze informatie moet centraal bij de afspraak blijven."
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/afspraken/{afspraakId}/opmerkingen",
                                afspraak.getId()
                        )
                                .session(session)
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.tekst")
                                .value(
                                        "Deze informatie moet centraal bij de afspraak blijven."
                                )
                )
                .andExpect(
                        jsonPath("$.auteur.id")
                                .value(
                                        gebruiker.getId()
                                                .toString()
                                )
                )
                .andExpect(
                        jsonPath("$.auteur.email")
                                .value(
                                        "opmerking-integratie@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$.aangemaaktOp")
                                .exists()
                );

        List<Opmerking> opgeslagen =
                opmerkingRepository
                        .findByAfspraakIdOrderByAangemaaktOpAsc(
                                afspraak.getId()
                        );

        assertEquals(
                1,
                opgeslagen.size()
        );

        assertEquals(
                gebruiker.getId(),
                opgeslagen.get(0)
                        .getAuteur()
                        .getId()
        );

        mockMvc.perform(
                        get(
                                "/api/afspraken/{afspraakId}/opmerkingen",
                                afspraak.getId()
                        )
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].tekst")
                                .value(
                                        "Deze informatie moet centraal bij de afspraak blijven."
                                )
                );
    }

    @Test
    void opmerkingenVanVerschillendeAfsprakenBlijvenGescheiden()
            throws Exception {

        Gebruiker gebruiker =
                maakGebruiker(
                        "Planner",
                        "opmerking-scheiding@example.com",
                        "SterkWachtwoord123!"
                );

        OperationeleAfspraak eersteAfspraak =
                maakAfspraak(
                        "Eerste afspraak",
                        gebruiker
                );

        OperationeleAfspraak tweedeAfspraak =
                maakAfspraak(
                        "Tweede afspraak",
                        gebruiker
                );

        MockHttpSession session =
                login(
                        gebruiker.getEmail(),
                        "SterkWachtwoord123!"
                );

        mockMvc.perform(
                        post(
                                "/api/afspraken/{afspraakId}/opmerkingen",
                                eersteAfspraak.getId()
                        )
                                .session(session)
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "tekst": "Alleen voor afspraak één"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                );

        mockMvc.perform(
                        get(
                                "/api/afspraken/{afspraakId}/opmerkingen",
                                tweedeAfspraak.getId()
                        )
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );
    }

    private MockHttpSession login(
            String email,
            String wachtwoord
    ) throws Exception {

        String json = """
                {
                  "email": "%s",
                  "wachtwoord": "%s"
                }
                """.formatted(
                email,
                wachtwoord
        );

        MvcResult resultaat =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(json)
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) resultaat
                        .getRequest()
                        .getSession(false);

        assertNotNull(
                session
        );

        return session;
    }

    private Gebruiker maakGebruiker(
            String naam,
            String email,
            String wachtwoord
    ) {

        Afdeling afdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Opmerking Test "
                                        + UUID.randomUUID()
                        )
                );

        Gebruiker gebruiker =
                new Gebruiker(
                        naam,
                        email,
                        afdeling
                );

        gebruiker.configureerToegang(
                passwordEncoder.encode(
                        wachtwoord
                ),
                GebruikersRol.GEBRUIKER,
                true
        );

        return gebruikerRepository
                .saveAndFlush(
                        gebruiker
                );
    }

    private OperationeleAfspraak maakAfspraak(
            String titel,
            Gebruiker initiatiefnemer
    ) {

        return afspraakRepository
                .saveAndFlush(
                        new OperationeleAfspraak(
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
                        )
                );
    }
}