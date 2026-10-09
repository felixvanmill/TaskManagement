package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateGebruikerToegangRequest;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerBeheerResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GebruikerService {

    private final GebruikerRepository gebruikerRepository;
    private final PasswordEncoder passwordEncoder;

    public GebruikerService(
            GebruikerRepository gebruikerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.gebruikerRepository =
                gebruikerRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    public List<GebruikerResponse> vindAlleGebruikers() {

        return gebruikerRepository
                .findAll()
                .stream()
                .map(this::naarResponse)
                .toList();
    }

    @PreAuthorize("hasRole('BEHEERDER')")
    public List<GebruikerBeheerResponse>
    vindAlleGebruikersVoorBeheer() {

        return gebruikerRepository
                .findAll()
                .stream()
                .map(this::naarBeheerResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('BEHEERDER')")
    public GebruikerBeheerResponse wijzigToegang(
            UUID gebruikerId,
            UpdateGebruikerToegangRequest request
    ) {

        Gebruiker gebruiker =
                gebruikerRepository
                        .findById(
                                gebruikerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Gebruiker niet gevonden"
                                )
                        );

        gebruiker.wijzigRol(
                request.rol()
        );

        gebruiker.wijzigToegang(
                request.actief()
        );

        if (request.nieuwWachtwoord() != null) {

            if (request.nieuwWachtwoord()
                    .isBlank()) {

                throw new IllegalArgumentException(
                        "Nieuw wachtwoord mag niet leeg zijn"
                );
            }

            String wachtwoordHash =
                    passwordEncoder.encode(
                            request.nieuwWachtwoord()
                    );

            gebruiker.wijzigWachtwoordHash(
                    wachtwoordHash
            );
        }

        Gebruiker opgeslagenGebruiker =
                gebruikerRepository.save(
                        gebruiker
                );

        return naarBeheerResponse(
                opgeslagenGebruiker
        );
    }

    private GebruikerResponse naarResponse(
            Gebruiker gebruiker
    ) {

        AfdelingResponse afdelingResponse =
                naarAfdelingResponse(
                        gebruiker
                );

        return new GebruikerResponse(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getEmail(),
                afdelingResponse
        );
    }

    private GebruikerBeheerResponse naarBeheerResponse(
            Gebruiker gebruiker
    ) {

        boolean lokaleToegang =
                gebruiker.getWachtwoordHash() != null
                        && !gebruiker
                        .getWachtwoordHash()
                        .isBlank();

        return new GebruikerBeheerResponse(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getEmail(),
                gebruiker.getRol(),
                gebruiker.isActief(),
                lokaleToegang,
                naarAfdelingResponse(
                        gebruiker
                )
        );
    }

    private AfdelingResponse naarAfdelingResponse(
            Gebruiker gebruiker
    ) {

        return new AfdelingResponse(
                gebruiker.getAfdeling()
                        .getId(),
                gebruiker.getAfdeling()
                        .getNaam()
        );
    }
}