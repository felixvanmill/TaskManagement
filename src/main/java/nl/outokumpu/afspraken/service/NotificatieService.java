
package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.Notificatie;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.NotificatieType;
import nl.outokumpu.afspraken.repository.NotificatieRepository;
import nl.outokumpu.afspraken.dto.response.NotificatieResponse;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Processtap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificatieService {

    private final NotificatieRepository notificatieRepository;

    public NotificatieService(
            NotificatieRepository notificatieRepository
    ) {
        this.notificatieRepository = notificatieRepository;
    }


    public List<NotificatieResponse> vindActieveNotificaties(
            UUID gebruikerId
    ) {
        return notificatieRepository
                .findByOntvanger_IdAndAfgehandeldOpIsNullOrderByAangemaaktOpDesc(
                        gebruikerId
                )
                .stream()
                .map(notificatie -> new NotificatieResponse(
                        notificatie.getId(),
                        notificatie.getAfspraak().getId(),
                        notificatie.getType(),
                        notificatie.getBericht(),
                        notificatie.getAangemaaktOp(),
                        notificatie.getGelezenOp()
                ))
                .toList();
    }


    public long telOngelezenNotificaties(
            UUID gebruikerId
    ) {
        return notificatieRepository
                .countByOntvanger_IdAndGelezenOpIsNullAndAfgehandeldOpIsNull(
                        gebruikerId
                );
    }

    @Transactional
    public Notificatie maakNotificatie(
            Gebruiker ontvanger,
            OperationeleAfspraak afspraak,
            NotificatieType type,
            String bericht
    ) {
        Notificatie notificatie = new Notificatie(
                ontvanger,
                afspraak,
                type,
                bericht
        );

        return notificatieRepository.save(notificatie);
    }


    @Transactional
    public Notificatie maakTaakNotificatie(
            Gebruiker ontvanger,
            OperationeleAfspraak afspraak,
            Processtap processtap,
            String bericht
    ) {
        Notificatie notificatie = new Notificatie(
                ontvanger,
                afspraak,
                processtap,
                NotificatieType.TAAK_TOEGEWEZEN,
                bericht
        );

        return notificatieRepository.save(notificatie);
    }


    @Transactional
    public void markeerGelezen(
            UUID notificatieId,
            UUID gebruikerId
    ) {
        Notificatie notificatie =
                vindEigenNotificatie(notificatieId, gebruikerId);

        notificatie.markeerGelezen();
    }

    @Transactional
    public void handelAf(
            UUID notificatieId,
            UUID gebruikerId
    ) {
        Notificatie notificatie =
                vindEigenNotificatie(notificatieId, gebruikerId);

        notificatie.handelAf();
    }


    @Transactional
    public void handelNotificatiesVoorAfspraakAf(
            UUID gebruikerId,
            UUID afspraakId,
            NotificatieType type
    ) {
        List<Notificatie> notificaties =
                notificatieRepository
                        .findByOntvanger_IdAndAfspraak_IdAndTypeAndAfgehandeldOpIsNull(
                                gebruikerId,
                                afspraakId,
                                type
                        );

        for (Notificatie notificatie : notificaties) {
            notificatie.handelAf();
        }
    }


    @Transactional
    public void handelTaakNotificatiesAf(
            UUID gebruikerId,
            UUID processtapId
    ) {
        List<Notificatie> notificaties =
                notificatieRepository
                        .findByOntvanger_IdAndProcesstap_IdAndTypeAndAfgehandeldOpIsNull(
                                gebruikerId,
                                processtapId,
                                NotificatieType.TAAK_TOEGEWEZEN
                        );

        for (Notificatie notificatie : notificaties) {
            notificatie.handelAf();
        }
    }



    private Notificatie vindEigenNotificatie(
            UUID notificatieId,
            UUID gebruikerId
    ) {
        return notificatieRepository
                .findByIdAndOntvanger_Id(
                        notificatieId,
                        gebruikerId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notificatie niet gevonden"
                        )
                );
    }
}
