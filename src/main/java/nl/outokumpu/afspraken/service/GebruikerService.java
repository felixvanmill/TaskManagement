package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.dto.response.GebruikerResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.repository.GebruikerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GebruikerService {

    private final GebruikerRepository gebruikerRepository;

    public GebruikerService(GebruikerRepository gebruikerRepository) {
        this.gebruikerRepository = gebruikerRepository;
    }

    public List<GebruikerResponse> vindAlleGebruikers() {
        return gebruikerRepository.findAll()
                .stream()
                .map(this::naarResponse)
                .toList();
    }

    private GebruikerResponse naarResponse(Gebruiker gebruiker) {

        AfdelingResponse afdelingResponse = new AfdelingResponse(
                gebruiker.getAfdeling().getId(),
                gebruiker.getAfdeling().getNaam()
        );

        return new GebruikerResponse(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getEmail(),
                afdelingResponse
        );
    }
}