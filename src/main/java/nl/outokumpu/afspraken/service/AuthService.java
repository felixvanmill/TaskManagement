package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.LoginRequest;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.AuthResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.exception.AuthenticatieException;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticates local credentials and retrieves user details for authentication responses.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AfspraakMapper afspraakMapper;
    private final AuthenticationManager authenticationManager;
    private final GebruikerRepository gebruikerRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            GebruikerRepository gebruikerRepository,
            AfspraakMapper afspraakMapper) {
        this.authenticationManager = authenticationManager;
        this.gebruikerRepository = gebruikerRepository;
        this.afspraakMapper = afspraakMapper;
    }

    /**
     * Authenticates credentials without revealing whether the email address or password caused a failure.
     * Session creation is handled by the caller, not by this method.
     *
     * @param request the local login credentials
     * @return the authenticated security identity
     * @throws AuthenticatieException if authentication fails
     */
    public Authentication login(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.wachtwoord()));
        } catch (AuthenticationException exception) {
            /*
             * Keep the failure generic so it does not reveal whether
             * the email address or password was incorrect.
             */
            throw new AuthenticatieException("Ongeldige inloggegevens");
        }
    }

    /**
     * Loads user details by a case-insensitive email lookup, allowing a missing department in the response.
     *
     * @param email the authenticated user's email address
     * @return the user's authentication response, including their role
     * @throws AuthenticatieException if the user no longer exists
     */
    public AuthResponse vindIngelogdeGebruiker(String email) {
        Gebruiker gebruiker = gebruikerRepository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new AuthenticatieException("Ingelogde gebruiker niet gevonden"));

        AfdelingResponse afdelingResponse = naarAfdelingResponse(gebruiker.getAfdeling());

        return new AuthResponse(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getEmail(),
                gebruiker.getRol(),
                afdelingResponse);
    }

    private AfdelingResponse naarAfdelingResponse(Afdeling afdeling) {
        if (afdeling == null) {
            return null;
        }

        return afspraakMapper.naarAfdelingResponse(afdeling);
    }
}