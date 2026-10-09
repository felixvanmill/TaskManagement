package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.repository.AfdelingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AfdelingService {

    private final AfdelingRepository afdelingRepository;

    public AfdelingService(AfdelingRepository afdelingRepository) {
        this.afdelingRepository = afdelingRepository;
    }

    public List<AfdelingResponse> vindAlleAfdelingen() {
        return afdelingRepository.findAll()
                .stream()
                .map(this::naarResponse)
                .toList();
    }

    private AfdelingResponse naarResponse(Afdeling afdeling) {
        return new AfdelingResponse(
                afdeling.getId(),
                afdeling.getNaam()
        );
    }
}