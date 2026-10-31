package nl.outokumpu.afspraken.config;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Optionally creates the initial administrator outside the test profile.
 * An existing email is left unchanged, so repeated startup does not reset the account.
 */
@Component
@Profile("!test")
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final AfdelingRepository afdelingRepository;
    private final GebruikerRepository gebruikerRepository;
    private final PasswordEncoder passwordEncoder;

    private final boolean enabled;
    private final String naam;
    private final String email;
    private final String wachtwoord;
    private final String afdelingNaam;

    public BootstrapAdminInitializer(
            AfdelingRepository afdelingRepository,
            GebruikerRepository gebruikerRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${app.bootstrap-admin.naam:}") String naam,
            @Value("${app.bootstrap-admin.email:}") String email,
            @Value("${app.bootstrap-admin.wachtwoord:}") String wachtwoord,
            @Value("${app.bootstrap-admin.afdeling:}") String afdelingNaam) {
        this.afdelingRepository = afdelingRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.naam = naam;
        this.email = email;
        this.wachtwoord = wachtwoord;
        this.afdelingNaam = afdelingNaam;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        controleerConfiguratie();

        if (gebruikerRepository.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }

        Afdeling afdeling = afdelingRepository.findByNaamIgnoreCase(afdelingNaam).orElseGet(() ->
                afdelingRepository.save(new Afdeling(afdelingNaam)));

        Gebruiker beheerder = new Gebruiker(naam, email, afdeling);

        beheerder.configureerToegang(passwordEncoder.encode(wachtwoord), GebruikersRol.BEHEERDER, true);

        gebruikerRepository.save(beheerder);
    }

    private void controleerConfiguratie() {
        if (naam == null || naam.isBlank()) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_NAME is verplicht wanneer bootstrap is ingeschakeld");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL is verplicht wanneer bootstrap is ingeschakeld");
        }

        if (wachtwoord == null || wachtwoord.length() < 12) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD moet minimaal 12 tekens bevatten");
        }

        if (afdelingNaam == null || afdelingNaam.isBlank()) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_DEPARTMENT is verplicht wanneer bootstrap is ingeschakeld");
        }
    }
}