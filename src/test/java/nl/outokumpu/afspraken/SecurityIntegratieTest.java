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

import static org.junit.jupiter.api.Assertions.*;

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

        String loginJson = """
                {
                  "email": "security@example.com",
                  "wachtwoord": "SterkWachtwoord123!"
                }
                """;

        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                loginJson
                                        )
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
                        )
                        .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult
                        .getRequest()
                        .getSession(
                                false
                        );

        assertNotNull(
                session
        );

        /*
         * Dezelfde server-side session wordt nu gebruikt
         * voor een beveiligde request.
         */
        mockMvc.perform(
                        get("/api/auth/me")
                                .session(
                                        session
                                )
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
                                .session(
                                        session
                                )
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
                                .content(
                                        loginJson
                                )
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
                                .content(
                                        loginJson
                                )
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
                                "Security Test Afdeling"
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