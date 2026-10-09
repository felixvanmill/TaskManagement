package nl.outokumpu.afspraken.security;

import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void wachtwoordWordtNietAlsLeesbareTekstOpgeslagen() {

        PasswordEncoder encoder =
                new SecurityConfig()
                        .passwordEncoder();

        String wachtwoord =
                "SterkWachtwoord123!";

        String hash =
                encoder.encode(
                        wachtwoord
                );

        assertNotEquals(
                wachtwoord,
                hash
        );

        assertTrue(
                encoder.matches(
                        wachtwoord,
                        hash
                )
        );
    }
}