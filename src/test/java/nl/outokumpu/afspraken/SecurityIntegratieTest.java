package nl.outokumpu.afspraken;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegratieTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private AfdelingRepository afdelingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void anoniemeGebruikerKrijgtGeenToegangTotBeveiligdeApi()
            throws Exception {

        mockMvc.perform(
                        get("/api/afdelingen")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void gebruikerKanInloggenEnBeveiligdeApiGebruiken()
            throws Exception {

        maakGebruiker(
                "Security Gebruiker",
                "security@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                true
        );

        MockHttpSession session =
                login(
                        "security@example.com",
                        "SterkWachtwoord123!"
                );

        mockMvc.perform(
                        get("/api/auth/me")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "security@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$.rol")
                                .value(
                                        "GEBRUIKER"
                                )
                );

        mockMvc.perform(
                        get("/api/afdelingen")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void verkeerdWachtwoordGeeftUnauthorized()
            throws Exception {

        maakGebruiker(
                "Security Gebruiker",
                "verkeerd@example.com",
                "JuistWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                true
        );

        String loginJson = """
                {
                  "email": "verkeerd@example.com",
                  "wachtwoord": "FoutWachtwoord123!"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginJson)
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ongeldige inloggegevens"
                                )
                );
    }

    @Test
    void inactieveGebruikerKanNietInloggen()
            throws Exception {

        maakGebruiker(
                "Inactieve Gebruiker",
                "inactief@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                false
        );

        String loginJson = """
                {
                  "email": "inactief@example.com",
                  "wachtwoord": "SterkWachtwoord123!"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginJson)
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void gewoneGebruikerKrijgtGeenToegangTotGebruikersbeheer()
            throws Exception {

        maakGebruiker(
                "Gewone Gebruiker",
                "gebruiker@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                true
        );

        MockHttpSession session =
                login(
                        "gebruiker@example.com",
                        "SterkWachtwoord123!"
                );

        mockMvc.perform(
                        get("/api/gebruikers/beheer")
                                .session(session)
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void beheerderKrijgtToegangTotGebruikersbeheer()
            throws Exception {

        maakGebruiker(
                "Beheerder",
                "beheerder@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.BEHEERDER,
                true
        );

        MockHttpSession session =
                login(
                        "beheerder@example.com",
                        "SterkWachtwoord123!"
                );

        mockMvc.perform(
                        get("/api/gebruikers/beheer")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$")
                                .isArray()
                );
    }

    @Test
    void gewoneGebruikerKanToegangVanAndereGebruikerNietWijzigen()
            throws Exception {

        maakGebruiker(
                "Gewone Gebruiker",
                "normaal@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                true
        );

        Gebruiker doelGebruiker =
                maakGebruiker(
                        "Doel Gebruiker",
                        "doel@example.com",
                        "DoelWachtwoord123!",
                        GebruikersRol.GEBRUIKER,
                        true
                );

        MockHttpSession session =
                login(
                        "normaal@example.com",
                        "SterkWachtwoord123!"
                );

        String json = """
                {
                  "rol": "BEHEERDER",
                  "actief": false
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/gebruikers/{id}/toegang",
                                doelGebruiker.getId()
                        )
                                .session(session)
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isForbidden()
                );

        Gebruiker ongewijzigdeGebruiker =
                gebruikerRepository
                        .findById(
                                doelGebruiker.getId()
                        )
                        .orElseThrow();

        assertEquals(
                GebruikersRol.GEBRUIKER,
                ongewijzigdeGebruiker.getRol()
        );

        assertTrue(
                ongewijzigdeGebruiker.isActief()
        );
    }

    @Test
    void beheerderKanRolEnToegangVanGebruikerWijzigen()
            throws Exception {

        maakGebruiker(
                "Beheerder",
                "admin@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.BEHEERDER,
                true
        );

        Gebruiker doelGebruiker =
                maakGebruiker(
                        "Doel Gebruiker",
                        "wijzigen@example.com",
                        "DoelWachtwoord123!",
                        GebruikersRol.GEBRUIKER,
                        true
                );

        MockHttpSession session =
                login(
                        "admin@example.com",
                        "SterkWachtwoord123!"
                );

        String json = """
                {
                  "rol": "BEHEERDER",
                  "actief": false
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/gebruikers/{id}/toegang",
                                doelGebruiker.getId()
                        )
                                .session(session)
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.rol")
                                .value(
                                        "BEHEERDER"
                                )
                )
                .andExpect(
                        jsonPath("$.actief")
                                .value(false)
                );

        Gebruiker gewijzigdeGebruiker =
                gebruikerRepository
                        .findById(
                                doelGebruiker.getId()
                        )
                        .orElseThrow();

        assertEquals(
                GebruikersRol.BEHEERDER,
                gewijzigdeGebruiker.getRol()
        );

        assertFalse(
                gewijzigdeGebruiker.isActief()
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
            String wachtwoord,
            GebruikersRol rol,
            boolean actief
    ) {

        Afdeling afdeling =
                afdelingRepository.saveAndFlush(
                        new Afdeling(
                                "Security Test "
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
                rol,
                actief
        );

        return gebruikerRepository
                .saveAndFlush(
                        gebruiker
                );
    }
}