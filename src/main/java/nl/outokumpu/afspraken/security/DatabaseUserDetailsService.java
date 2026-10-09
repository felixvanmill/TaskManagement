package nl.outokumpu.afspraken.security;

import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private final GebruikerRepository gebruikerRepository;

    public DatabaseUserDetailsService(
            GebruikerRepository gebruikerRepository
    ) {
        this.gebruikerRepository =
                gebruikerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        Gebruiker gebruiker =
                gebruikerRepository
                        .findByEmailIgnoreCase(
                                email
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Gebruiker niet gevonden"
                                )
                        );

        if (gebruiker.getWachtwoordHash() == null
                || gebruiker.getWachtwoordHash().isBlank()) {

            throw new UsernameNotFoundException(
                    "Gebruiker heeft geen lokale inloggegevens"
            );
        }

        return new GebruikerPrincipal(
                gebruiker.getId(),
                gebruiker.getEmail(),
                gebruiker.getWachtwoordHash(),
                gebruiker.getRol(),
                gebruiker.isActief()
        );
    }
}