package nl.outokumpu.afspraken.controller;

import nl.outokumpu.afspraken.dto.response.AuthResponse;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        authService =
                mock(AuthService.class);

        AuthController controller =
                new AuthController(
                        authService
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();
    }

    @Test
    void loginMaaktSecuritySessionAan()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(
                authentication.getName()
        ).thenReturn(
                "admin@example.com"
        );

        when(
                authService.login(
                        any()
                )
        ).thenReturn(
                authentication
        );

        when(
                authService
                        .vindIngelogdeGebruiker(
                                "admin@example.com"
                        )
        ).thenReturn(
                new AuthResponse(
                        UUID.randomUUID(),
                        "Admin",
                        "admin@example.com",
                        GebruikersRol.BEHEERDER,
                        null
                )
        );

        String json = """
                {
                  "email": "admin@example.com",
                  "wachtwoord": "SterkWachtwoord123!"
                }
                """;

        MvcResult result =
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
                        .andExpect(
                                jsonPath("$.email")
                                        .value(
                                                "admin@example.com"
                                        )
                        )
                        .andExpect(
                                jsonPath("$.rol")
                                        .value(
                                                "BEHEERDER"
                                        )
                        )
                        .andReturn();

        assertNotNull(
                result.getRequest()
                        .getSession(false)
        );

        assertNotNull(
                result.getRequest()
                        .getSession(false)
                        .getAttribute(
                                HttpSessionSecurityContextRepository
                                        .SPRING_SECURITY_CONTEXT_KEY
                        )
        );
    }

    @Test
    void geeftHuidigeGebruikerTerug()
            throws Exception {

        Authentication authentication =
                mock(Authentication.class);

        when(
                authentication.getName()
        ).thenReturn(
                "user@example.com"
        );

        when(
                authService
                        .vindIngelogdeGebruiker(
                                "user@example.com"
                        )
        ).thenReturn(
                new AuthResponse(
                        UUID.randomUUID(),
                        "Gebruiker",
                        "user@example.com",
                        GebruikersRol.GEBRUIKER,
                        null
                )
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "user@example.com"
                                )
                );

        verify(
                authService
        ).vindIngelogdeGebruiker(
                "user@example.com"
        );
    }

    @Test
    void logoutBeeindigtSessie()
            throws Exception {

        MockHttpSession session =
                new MockHttpSession();

        mockMvc.perform(
                        post("/api/auth/logout")
                                .session(session)
                )
                .andExpect(
                        status().isNoContent()
                );

        assertTrue(
                session.isInvalid()
        );
    }

    @Test
    void geeftCsrfTokenTerug()
            throws Exception {

        DefaultCsrfToken token =
                new DefaultCsrfToken(
                        "X-XSRF-TOKEN",
                        "_csrf",
                        "test-token"
                );

        mockMvc.perform(
                        get("/api/auth/csrf")
                                .requestAttr(
                                        CsrfToken.class.getName(),
                                        token
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.headerNaam")
                                .value(
                                        "X-XSRF-TOKEN"
                                )
                )
                .andExpect(
                        jsonPath("$.parameterNaam")
                                .value(
                                        "_csrf"
                                )
                )
                .andExpect(
                        jsonPath("$.token")
                                .value(
                                        "test-token"
                                )
                );
    }
}