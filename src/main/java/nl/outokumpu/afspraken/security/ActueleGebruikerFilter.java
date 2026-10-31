package nl.outokumpu.afspraken.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Revalidates session users against the database before authorization.
 * Account removal, deactivation or a changed password ends the session; role changes refresh it.
 */
@Component
public class ActueleGebruikerFilter extends OncePerRequestFilter {

    private final GebruikerRepository gebruikerRepository;

    public ActueleGebruikerFilter(GebruikerRepository gebruikerRepository) {
        this.gebruikerRepository = gebruikerRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof GebruikerPrincipal principal)) {
            filterChain.doFilter(request, response);

            return;
        }

        Optional<Gebruiker> gebruikerOptional = gebruikerRepository.findById(principal.getId());

        if (gebruikerOptional.isEmpty()) {
            beeindigSessie(request, response);

            return;
        }

        Gebruiker gebruiker = gebruikerOptional.get();

        /*
         * A deactivated account must lose access even when its session is still active.
         */
        if (!gebruiker.isActief()) {
            beeindigSessie(request, response);

            return;
        }

        /*
         * A password change invalidates existing sessions and requires a fresh login.
         */
        if (!Objects.equals(principal.getPassword(), gebruiker.getWachtwoordHash())) {
            beeindigSessie(request, response);

            return;
        }

        /*
         * Role changes apply immediately without requiring another login.
         */
        if (principal.getRol() != gebruiker.getRol()) {
            verversAuthenticatie(request, authentication, gebruiker);
        }

        filterChain.doFilter(request, response);
    }

    private void verversAuthenticatie(
            HttpServletRequest request,
            Authentication oudeAuthenticatie,
            Gebruiker gebruiker) {
        GebruikerPrincipal nieuwPrincipal = new GebruikerPrincipal(
                gebruiker.getId(),
                gebruiker.getEmail(),
                gebruiker.getWachtwoordHash(),
                gebruiker.getRol(),
                gebruiker.isActief());

        UsernamePasswordAuthenticationToken nieuweAuthenticatie = new UsernamePasswordAuthenticationToken(
                nieuwPrincipal,
                null,
                nieuwPrincipal.getAuthorities());

        nieuweAuthenticatie.setDetails(oudeAuthenticatie.getDetails());

        SecurityContext context = SecurityContextHolder.getContext();

        context.setAuthentication(nieuweAuthenticatie);

        /*
         * Update the stored security context so subsequent requests use the refreshed role.
         */
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        }
    }

    private void beeindigSessie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}