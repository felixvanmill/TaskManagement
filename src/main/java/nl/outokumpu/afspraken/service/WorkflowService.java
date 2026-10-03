package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.response.AfspraakDetailResponse;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Processtap;
import nl.outokumpu.afspraken.entity.Wijziging;
import nl.outokumpu.afspraken.enums.AfspraakStatus;
import nl.outokumpu.afspraken.enums.ProcesstapStatus;
import nl.outokumpu.afspraken.mapper.AfspraakMapper;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;
import nl.outokumpu.afspraken.repository.ProcesstapRepository;
import nl.outokumpu.afspraken.repository.WijzigingRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import nl.outokumpu.afspraken.entity.Orderverplaatsing;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

@Service
@Transactional
public class WorkflowService {

    private final ProcesstapRepository processtapRepository;
    private final OperationeleAfspraakRepository afspraakRepository;
    private final WijzigingRepository wijzigingRepository;
    private final AfspraakMapper afspraakMapper;

    public WorkflowService(
            ProcesstapRepository processtapRepository,
            OperationeleAfspraakRepository afspraakRepository,
            WijzigingRepository wijzigingRepository,
            AfspraakMapper afspraakMapper
    ) {
        this.processtapRepository = processtapRepository;
        this.afspraakRepository = afspraakRepository;
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

    public AfspraakDetailResponse wijzigAfspraakStatus(
            UUID afspraakId,
            AfspraakStatus nieuweStatus,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(afspraakId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Afspraak niet gevonden"
                                )
                        );

        Gebruiker huidigeVerantwoordelijke =
                bepaalHuidigeVerantwoordelijke(
                        afspraak
                );

        controleerVerantwoordelijke(
                gebruikerId,
                huidigeVerantwoordelijke,
                "Alleen de huidige verantwoordelijke kan de afspraakstatus wijzigen"
        );

        String oudeStatus =
                afspraak.getStatus().name();

        afspraak.wijzigStatus(
                nieuweStatus
        );

        Wijziging wijziging =
                afspraak.registreerWijziging(
                        huidigeVerantwoordelijke,
                        "Afspraakstatus",
                        oudeStatus,
                        afspraak.getStatus().name()
                );

        afspraakRepository.save(
                afspraak
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                afspraak
        );
    }

    public AfspraakDetailResponse wijzigUitvoeringsstatus(
            UUID afspraakId,
            UitvoeringsStatus nieuweStatus,
            UUID gebruikerId
    ) {

        OperationeleAfspraak afspraak =
                afspraakRepository.findById(afspraakId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Afspraak niet gevonden"
                                )
                        );

        if (!(afspraak instanceof Orderverplaatsing orderverplaatsing)) {
            throw new IllegalArgumentException(
                    "Afspraak is geen orderverplaatsing"
            );
        }

        Gebruiker huidigeVerantwoordelijke =
                bepaalHuidigeVerantwoordelijke(
                        orderverplaatsing
                );

        controleerVerantwoordelijke(
                gebruikerId,
                huidigeVerantwoordelijke,
                "Alleen de huidige verantwoordelijke kan de uitvoeringsstatus wijzigen"
        );

        String oudeStatus =
                orderverplaatsing
                        .getUitvoeringsstatus()
                        .name();

        orderverplaatsing.wijzigUitvoeringsstatus(
                nieuweStatus
        );

        Wijziging wijziging =
                orderverplaatsing.registreerWijziging(
                        huidigeVerantwoordelijke,
                        "Uitvoeringsstatus",
                        oudeStatus,
                        orderverplaatsing
                                .getUitvoeringsstatus()
                                .name()
                );

        afspraakRepository.save(
                orderverplaatsing
        );

        wijzigingRepository.save(
                wijziging
        );

        return afspraakMapper.naarDetailResponse(
                orderverplaatsing
        );
    }

    private Gebruiker bepaalHuidigeVerantwoordelijke(
            OperationeleAfspraak afspraak
    ) {

        List<Processtap> processtappen =
                afspraak.getProcesstappen();

        if (processtappen.isEmpty()) {
            throw new IllegalStateException(
                    "Afspraak heeft geen processtappen"
            );
        }

        return processtappen.stream()
                .filter(processtap ->
                        processtap.getStatus()
                                != ProcesstapStatus.AFGEROND
                )
                .findFirst()
                .orElse(
                        processtappen.get(
                                processtappen.size() - 1
                        )
                )
                .getVerantwoordelijke();
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