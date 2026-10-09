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

import org.springframework.http.HttpHeaders;
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
class SecurityWebConfigIntegratieTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GebruikerRepository gebruikerRepository;

    @Autowired
    private AfdelingRepository afdelingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void corsStaatReactFrontendToe()
            throws Exception {

        mockMvc.perform(
                        options("/api/afdelingen")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://localhost:5173"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                "http://localhost:5173"
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                                "true"
                        )
                );
    }

    @Test
    void csrfBlokkeertWijzigingZonderToken()
            throws Exception {

        maakGebruiker(
                "Beheerder",
                "csrf-admin@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.BEHEERDER,
                true
        );

        Gebruiker doelGebruiker =
                maakGebruiker(
                        "Doel",
                        "csrf-doel@example.com",
                        "SterkWachtwoord123!",
                        GebruikersRol.GEBRUIKER,
                        true
                );

        MockHttpSession session =
                login(
                        "csrf-admin@example.com",
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
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(
                        status().isForbidden()
                );

        Gebruiker ongewijzigd =
                gebruikerRepository
                        .findById(
                                doelGebruiker.getId()
                        )
                        .orElseThrow();

        assertEquals(
                GebruikersRol.GEBRUIKER,
                ongewijzigd.getRol()
        );

        assertTrue(
                ongewijzigd.isActief()
        );
    }

    @Test
    void logoutZonderCsrfWordtGeweigerd()
            throws Exception {

        maakGebruiker(
                "Gebruiker",
                "logout-csrf@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                true
        );

        MockHttpSession session =
                login(
                        "logout-csrf@example.com",
                        "SterkWachtwoord123!"
                );

        mockMvc.perform(
                        post("/api/auth/logout")
                                .session(session)
                )
                .andExpect(
                        status().isForbidden()
                );

        /*
         * Omdat logout geweigerd is,
         * moet dezelfde sessie nog geldig zijn.
         */
        mockMvc.perform(
                        get("/api/auth/me")
                                .session(session)
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void logoutMetCsrfBeeindigtSessie()
            throws Exception {

        maakGebruiker(
                "Gebruiker",
                "logout@example.com",
                "SterkWachtwoord123!",
                GebruikersRol.GEBRUIKER,
                true
        );

        MockHttpSession session =
                login(
                        "logout@example.com",
                        "SterkWachtwoord123!"
                );

        mockMvc.perform(
                        post("/api/auth/logout")
                                .session(session)
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        assertTrue(
                session.isInvalid()
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
                                "Security Web "
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