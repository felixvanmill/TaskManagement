package nl.outokumpu.afspraken.config;

import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.enums.GebruikersRol;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.ApplicationArguments;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BootstrapAdminInitializerTest {

    private AfdelingRepository afdelingRepository;
    private GebruikerRepository gebruikerRepository;
    private PasswordEncoder passwordEncoder;
    private ApplicationArguments applicationArguments;

    @BeforeEach
    void setUp() {

        afdelingRepository =
                mock(AfdelingRepository.class);

        gebruikerRepository =
                mock(GebruikerRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        applicationArguments =
                mock(ApplicationArguments.class);
    }

    @Test
    void doetNietsAlsBootstrapIsUitgeschakeld() {

        BootstrapAdminInitializer initializer =
                maakInitializer(
                        false,
                        "",
                        "",
                        "",
                        ""
                );

        initializer.run(
                applicationArguments
        );

        verifyNoInteractions(
                afdelingRepository,
                gebruikerRepository,
                passwordEncoder
        );
    }

    @Test
    void maaktNieuweBeheerderEnAfdelingAan() {

        when(
                gebruikerRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                afdelingRepository.findByNaamIgnoreCase(
                        "Capacity Management"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                afdelingRepository.save(
                        any(Afdeling.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                passwordEncoder.encode(
                        "SterkWachtwoord123!"
                )
        ).thenReturn(
                "bcrypt-hash"
        );

        when(
                gebruikerRepository.save(
                        any(Gebruiker.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        BootstrapAdminInitializer initializer =
                maakInitializer(
                        true,
                        "Initial Admin",
                        "admin@example.com",
                        "SterkWachtwoord123!",
                        "Capacity Management"
                );

        initializer.run(
                applicationArguments
        );

        verify(
                gebruikerRepository
        ).save(
                argThat(gebruiker ->
                        gebruiker.getNaam()
                                .equals("Initial Admin")
                                &&
                                gebruiker.getEmail()
                                        .equals("admin@example.com")
                                &&
                                gebruiker.getRol()
                                        == GebruikersRol.BEHEERDER
                                &&
                                gebruiker.isActief()
                                &&
                                gebruiker.getWachtwoordHash()
                                        .equals("bcrypt-hash")
                                &&
                                gebruiker.getAfdeling()
                                        .getNaam()
                                        .equals("Capacity Management")
                )
        );
    }

    @Test
    void gebruiktBestaandeAfdeling() {

        Afdeling bestaandeAfdeling =
                new Afdeling(
                        "Capacity Management"
                );

        when(
                gebruikerRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                afdelingRepository.findByNaamIgnoreCase(
                        "Capacity Management"
                )
        ).thenReturn(
                Optional.of(
                        bestaandeAfdeling
                )
        );

        when(
                passwordEncoder.encode(
                        "SterkWachtwoord123!"
                )
        ).thenReturn(
                "bcrypt-hash"
        );

        BootstrapAdminInitializer initializer =
                maakInitializer(
                        true,
                        "Initial Admin",
                        "admin@example.com",
                        "SterkWachtwoord123!",
                        "Capacity Management"
                );

        initializer.run(
                applicationArguments
        );

        verify(
                afdelingRepository,
                never()
        ).save(
                any()
        );

        verify(
                gebruikerRepository
        ).save(
                argThat(gebruiker ->
                        gebruiker.getAfdeling()
                                == bestaandeAfdeling
                )
        );
    }

    @Test
    void bestaandeGebruikerWordtNietOverschreven() {

        Gebruiker bestaandeGebruiker =
                mock(Gebruiker.class);

        when(
                gebruikerRepository.findByEmailIgnoreCase(
                        "admin@example.com"
                )
        ).thenReturn(
                Optional.of(
                        bestaandeGebruiker
                )
        );

        BootstrapAdminInitializer initializer =
                maakInitializer(
                        true,
                        "Initial Admin",
                        "admin@example.com",
                        "SterkWachtwoord123!",
                        "Capacity Management"
                );

        initializer.run(
                applicationArguments
        );

        verify(
                gebruikerRepository,
                never()
        ).save(
                any()
        );

        verifyNoInteractions(
                afdelingRepository,
                passwordEncoder
        );
    }

    @Test
    void weigertTeKortWachtwoord() {

        BootstrapAdminInitializer initializer =
                maakInitializer(
                        true,
                        "Initial Admin",
                        "admin@example.com",
                        "kort",
                        "Capacity Management"
                );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                initializer.run(
                                        applicationArguments
                                )
                );

        assertEquals(
                "BOOTSTRAP_ADMIN_PASSWORD moet minimaal 12 tekens bevatten",
                exception.getMessage()
        );
    }

    private BootstrapAdminInitializer maakInitializer(
            boolean enabled,
            String naam,
            String email,
            String wachtwoord,
            String afdeling
    ) {

        return new BootstrapAdminInitializer(
                afdelingRepository,
                gebruikerRepository,
                passwordEncoder,
                enabled,
                naam,
                email,
                wachtwoord,
                afdeling
        );
    }
}