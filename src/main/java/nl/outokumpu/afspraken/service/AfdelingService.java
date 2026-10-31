package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfdelingResponse;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.AfdelingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Provides department reference data for agreement assignment.
 */
@Service
@Transactional(readOnly = true)
public class AfdelingService {

    private final AfspraakMapper afspraakMapper;
    private final AfdelingRepository afdelingRepository;

    public AfdelingService(AfdelingRepository afdelingRepository, AfspraakMapper afspraakMapper) {
        this.afdelingRepository = afdelingRepository;
        this.afspraakMapper = afspraakMapper;
    }

    public List<AfdelingResponse> vindAlleAfdelingen() {
        return afdelingRepository.findAll().stream()
                .map(afspraakMapper::naarAfdelingResponse)
                .toList();
    }
}