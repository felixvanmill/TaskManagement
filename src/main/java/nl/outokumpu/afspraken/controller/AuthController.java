package nl.outokumpu.afspraken.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import nl.outokumpu.afspraken.dto.request.LoginRequest;
import nl.outokumpu.afspraken.dto.response.AuthResponse;
import nl.outokumpu.afspraken.dto.response.CsrfResponse;
import nl.outokumpu.afspraken.service.AuthService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;

import org.springframework.web.bind.annotation.*;

/**
 * Manages session-based login, current-user details, CSRF token retrieval and logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService =
                authService;
    }

    /**
     * Authenticates local credentials and replaces any existing session to prevent session fixation.
     * Login is available without prior authentication and is exempt from CSRF validation;
     * clients should retrieve a CSRF token after the new session is established.
     *
     * @param request the validated login credentials
     * @param servletRequest the request whose session will hold the authenticated security context
     * @return the authenticated user's details; the servlet container manages the session cookie
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {

        Authentication authentication =
                authService.login(
                        request
                );

        HttpSession bestaandeSessie =
                servletRequest.getSession(
                        false
                );

        if (bestaandeSessie != null) {
            bestaandeSessie.invalidate();
        }

        SecurityContext context =
                SecurityContextHolder
                        .createEmptyContext();

        context.setAuthentication(
                authentication
        );

        SecurityContextHolder.setContext(
                context
        );

        HttpSession nieuweSessie =
                servletRequest.getSession(
                        true
                );

        nieuweSessie.setAttribute(
                HttpSessionSecurityContextRepository
                        .SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        AuthResponse response =
                authService
                        .vindIngelogdeGebruiker(
                                authentication.getName()
                        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> huidigeGebruiker(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                authService
                        .vindIngelogdeGebruiker(
                                authentication.getName()
                        )
        );
    }

    /**
     * Returns the CSRF token and its header and parameter names for subsequent state-changing requests.
     * This endpoint is available before login so clients can obtain a token without prior authentication.
     *
     * @param request the request carrying the token supplied by Spring Security
     * @return the current CSRF token metadata
     * @throws IllegalStateException if the security filter has not made a token available
     */
    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(
            HttpServletRequest request
    ) {

        CsrfToken token =
                (CsrfToken) request.getAttribute(
                        CsrfToken.class.getName()
                );

        if (token == null) {

            token =
                    (CsrfToken) request.getAttribute(
                            "_csrf"
                    );
        }

        if (token == null) {

            throw new IllegalStateException(
                    "CSRF-token is niet beschikbaar"
            );
        }

        return ResponseEntity.ok(
                new CsrfResponse(
                        token.getHeaderName(),
                        token.getParameterName(),
                        token.getToken()
                )
        );
    }

    /**
     * Invalidates the current session, if present, and clears the thread's security context.
     * The endpoint requires authentication and a valid CSRF token under the security configuration.
     *
     * @param servletRequest the request carrying the session to invalidate
     * @return HTTP 204 without a response body
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest servletRequest
    ) {

        HttpSession session =
                servletRequest.getSession(
                        false
                );

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder
                .clearContext();

        return ResponseEntity
                .noContent()
                .build();
    }
}