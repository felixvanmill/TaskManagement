package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.request.UpdateGebruikerToegangRequest;
import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerBeheerResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GebruikerService {

    private final AfspraakMapper afspraakMapper;

    private final GebruikerRepository gebruikerRepository;

    public GebruikerService(
            GebruikerRepository gebruikerRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.gebruikerRepository =
                gebruikerRepository;

        this.afspraakMapper = afspraakMapper;
    }

    public List<GebruikerResponse> vindAlleGebruikers() {

        return gebruikerRepository
                .findAll()
                .stream()
                .map(afspraakMapper::naarGebruikerResponse)
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

        Gebruiker opgeslagenGebruiker =
                gebruikerRepository.save(
                        gebruiker
                );

        return naarBeheerResponse(
                opgeslagenGebruiker
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

        return afspraakMapper.naarAfdelingResponse(
                gebruiker.getAfdeling()
        );
    }
}