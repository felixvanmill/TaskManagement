package nl.outokumpu.afspraken.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            DatabaseUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider
    ) throws Exception {

        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository
                        .withHttpOnlyFalse();

        http
                .authenticationProvider(
                        authenticationProvider
                )

                .csrf(csrf ->
                        csrf
                                .csrfTokenRepository(
                                        csrfRepository
                                )
                                /*
                                 * Een nog niet ingelogde React-client
                                 * moet kunnen inloggen zonder reeds
                                 * een CSRF-session te bezitten.
                                 */
                                .ignoringRequestMatchers(
                                        "/api/auth/login"
                                )
                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(
                                        "/api/auth/login",
                                        "/api/auth/csrf"
                                )
                                .permitAll()

                                .anyRequest()
                                .authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(
                                        (
                                                request,
                                                response,
                                                exception
                                        ) ->
                                                response.sendError(
                                                        HttpServletResponse.SC_UNAUTHORIZED
                                                )
                                )
                                .accessDeniedHandler(
                                        (
                                                request,
                                                response,
                                                exception
                                        ) ->
                                                response.sendError(
                                                        HttpServletResponse.SC_FORBIDDEN
                                                )
                                )
                )

                .requestCache(cache ->
                        cache.disable()
                )

                .formLogin(
                        AbstractHttpConfigurer::disable
                )

                .httpBasic(
                        AbstractHttpConfigurer::disable
                )

                /*
                 * We gebruiken onze eigen JSON logout-endpoint.
                 */
                .logout(
                        AbstractHttpConfigurer::disable
                );

        return http.build();
    }
}