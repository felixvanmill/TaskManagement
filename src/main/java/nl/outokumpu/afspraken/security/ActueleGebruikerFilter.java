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

@Component
public class ActueleGebruikerFilter
        extends OncePerRequestFilter {

    private final GebruikerRepository gebruikerRepository;

    public ActueleGebruikerFilter(
            GebruikerRepository gebruikerRepository
    ) {
        this.gebruikerRepository =
                gebruikerRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                instanceof GebruikerPrincipal principal)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        Optional<Gebruiker> gebruikerOptional =
                gebruikerRepository.findById(
                        principal.getId()
                );

        if (gebruikerOptional.isEmpty()) {

            beeindigSessie(
                    request,
                    response
            );

            return;
        }

        Gebruiker gebruiker =
                gebruikerOptional.get();

        /*
         * Een gedeactiveerd account krijgt direct
         * geen toegang meer, ook wanneer er nog
         * een bestaande sessie aanwezig is.
         */
        if (!gebruiker.isActief()) {

            beeindigSessie(
                    request,
                    response
            );

            return;
        }

        /*
         * Wanneer het wachtwoord door een beheerder
         * is gewijzigd, trekken we bestaande sessies
         * in en moet opnieuw worden ingelogd.
         */
        if (!Objects.equals(
                principal.getPassword(),
                gebruiker.getWachtwoordHash()
        )) {

            beeindigSessie(
                    request,
                    response
            );

            return;
        }

        /*
         * Een gewijzigde rol moet onmiddellijk gelden.
         * De gebruiker hoeft hiervoor niet opnieuw
         * in te loggen.
         */
        if (principal.getRol()
                != gebruiker.getRol()) {

            verversAuthenticatie(
                    request,
                    authentication,
                    gebruiker
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }

    private void verversAuthenticatie(
            HttpServletRequest request,
            Authentication oudeAuthenticatie,
            Gebruiker gebruiker
    ) {

        GebruikerPrincipal nieuwPrincipal =
                new GebruikerPrincipal(
                        gebruiker.getId(),
                        gebruiker.getEmail(),
                        gebruiker.getWachtwoordHash(),
                        gebruiker.getRol(),
                        gebruiker.isActief()
                );

        UsernamePasswordAuthenticationToken
                nieuweAuthenticatie =
                new UsernamePasswordAuthenticationToken(
                        nieuwPrincipal,
                        null,
                        nieuwPrincipal.getAuthorities()
                );

        nieuweAuthenticatie.setDetails(
                oudeAuthenticatie.getDetails()
        );

        SecurityContext context =
                SecurityContextHolder
                        .getContext();

        context.setAuthentication(
                nieuweAuthenticatie
        );

        /*
         * Ook de server-side session wordt bijgewerkt,
         * zodat volgende requests dezelfde actuele rol
         * gebruiken.
         */
        HttpSession session =
                request.getSession(false);

        if (session != null) {

            session.setAttribute(
                    HttpSessionSecurityContextRepository
                            .SPRING_SECURITY_CONTEXT_KEY,
                    context
            );
        }
    }

    private void beeindigSessie(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        SecurityContextHolder
                .clearContext();

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED
        );
    }
}