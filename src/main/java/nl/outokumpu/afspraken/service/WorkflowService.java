package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.Processtap;
import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class WorkflowService {

    private final ProcesstapRepository processtapRepository;
    private final WijzigingRepository wijzigingRepository;
    private final AfspraakMapper afspraakMapper;

    public WorkflowService(
            ProcesstapRepository processtapRepository,
            WijzigingRepository wijzigingRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.processtapRepository = processtapRepository;
        this.wijzigingRepository = wijzigingRepository;
        this.afspraakMapper = afspraakMapper;
    }

    public AfspraakDetailResponse startProcesstap(
            UUID processtapId,
            UUID gebruikerId
    ) {

        Processtap processtap =
                processtapRepository.findById(processtapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Processtap niet gevonden"
                                )
                        );

        Gebruiker verantwoordelijke =
                processtap.getVerantwoordelijke();

        controleerVerantwoordelijke(
                gebruikerId,
                verantwoordelijke,
                "Alleen de verantwoordelijke kan deze processtap starten"
        );

        String oudeStatus =
                processtap.getStatus().name();

        processtap.start();

        Wijziging wijziging =
                processtap.getAfspraak()
                        .registreerWijziging(
                                verantwoordelijke,
                                "Processtapstatus: " + processtap.getNaam(),
                                oudeStatus,
                                processtap.getStatus().name()
                        );

        processtapRepository.save(
                processtap
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                processtap.getAfspraak()
        );
    }

    public AfspraakDetailResponse rondProcesstapAf(
            UUID processtapId,
            UUID gebruikerId
    ) {

        Processtap processtap =
                processtapRepository.findById(processtapId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Processtap niet gevonden"
                                )
                        );

        Gebruiker verantwoordelijke =
                processtap.getVerantwoordelijke();

        controleerVerantwoordelijke(
                gebruikerId,
                verantwoordelijke,
                "Alleen de verantwoordelijke kan deze processtap afronden"
        );

        String oudeStatus =
                processtap.getStatus().name();

        processtap.rondAf();

        Wijziging wijziging =
                processtap.getAfspraak()
                        .registreerWijziging(
                                verantwoordelijke,
                                "Processtapstatus: " + processtap.getNaam(),
                                oudeStatus,
                                processtap.getStatus().name()
                        );

        processtapRepository.save(
                processtap
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                processtap.getAfspraak()
        );
    }

    private void controleerVerantwoordelijke(
            UUID gebruikerId,
            Gebruiker verantwoordelijke,
            String foutmelding
    ) {

        if (gebruikerId == null
                || !gebruikerId.equals(verantwoordelijke.getId())) {

            throw new IllegalArgumentException(
                    foutmelding
            );
        }
    }
}