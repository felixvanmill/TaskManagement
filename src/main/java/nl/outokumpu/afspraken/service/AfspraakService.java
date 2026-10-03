package nl.outokumpu.afspraken.service;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.request.CreateAfspraakRequest;
import nl.outokumpu.afspraken.entity.Afdeling;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.Gebruiker;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.BetrokkenRol;
import nl.outokumpu.afspraken.repository.AfdelingRepository;
import nl.outokumpu.afspraken.repository.GebruikerRepository;
import nl.outokumpu.afspraken.repository.OperationeleAfspraakRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AfspraakService {

    private final OperationeleAfspraakRepository afspraakRepository;
    private final GebruikerRepository gebruikerRepository;
    private final AfdelingRepository afdelingRepository;

    public AfspraakService(
            OperationeleAfspraakRepository afspraakRepository,
            GebruikerRepository gebruikerRepository,
            AfdelingRepository afdelingRepository
    ) {
        this.afspraakRepository = afspraakRepository;
        this.gebruikerRepository = gebruikerRepository;
        this.afdelingRepository = afdelingRepository;
    }

    public OperationeleAfspraak createAfspraak(
            CreateAfspraakRequest request,
            UUID initiatiefnemerId
    ) {

        Gebruiker initiatiefnemer = gebruikerRepository
                .findById(initiatiefnemerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Initiatiefnemer niet gevonden"
                        )
                );

        Gebruiker verantwoordelijke = gebruikerRepository
                .findById(request.verantwoordelijkeId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Verantwoordelijke niet gevonden"
                        )
                );

        OperationeleAfspraak afspraak =
                maakAfspraakOpBasisVanType(
                        request,
                        initiatiefnemer
                );

        afspraak.voegProcesstapToe(
                request.eersteProcesstapNaam(),
                1,
                request.eersteProcesstapDeadline(),
                verantwoordelijke
        );

        List<Gebruiker> betrokkenGebruikers =
                gebruikerRepository.findAllById(
                        request.betrokkeneIds()
                );

        controleerAlleIdsGevonden(
                request.betrokkeneIds(),
                betrokkenGebruikers.size(),
                "Een of meer betrokken gebruikers zijn niet gevonden"
        );

        for (Gebruiker gebruiker : betrokkenGebruikers) {
            afspraak.voegBetrokkenheidToe(
                    gebruiker,
                    BetrokkenRol.BETROKKENE
            );
        }

        List<Gebruiker> goedkeurders =
                gebruikerRepository.findAllById(
                        request.goedkeurderIds()
                );

        controleerAlleIdsGevonden(
                request.goedkeurderIds(),
                goedkeurders.size(),
                "Een of meer goedkeurders zijn niet gevonden"
        );

        for (Gebruiker goedkeurder : goedkeurders) {

            afspraak.voegBetrokkenheidToe(
                    goedkeurder,
                    BetrokkenRol.GOEDKEURDER
            );

            afspraak.voegBevestigingToe(
                    goedkeurder
            );
        }

        List<Afdeling> betrokkenAfdelingen =
                afdelingRepository.findAllById(
                        request.afdelingIds()
                );

        controleerAlleIdsGevonden(
                request.afdelingIds(),
                betrokkenAfdelingen.size(),
                "Een of meer afdelingen zijn niet gevonden"
        );

        for (Afdeling afdeling : betrokkenAfdelingen) {
            afspraak.voegAfdelingToe(afdeling);
        }

        return afspraakRepository.save(afspraak);
    }

    private OperationeleAfspraak maakAfspraakOpBasisVanType(
            CreateAfspraakRequest request,
            Gebruiker initiatiefnemer
    ) {

        return switch (request.type()) {

            case ALGEMEEN -> {

                if (request.capaciteitswissel() != null
                        || request.orderverplaatsing() != null) {

                    throw new IllegalArgumentException(
                            "Een algemene afspraak mag geen typespecifieke gegevens bevatten"
                    );
                }

                yield new OperationeleAfspraak(
                        request.titel(),
                        request.beschrijving(),
                        request.reden(),
                        request.achtergrond(),
                        request.aannames(),
                        request.ingangsdatum(),
                        request.deadline(),
                        initiatiefnemer
                );
            }

            case CAPACITEITSWISSEL -> {

                CapaciteitswisselData data =
                        request.capaciteitswissel();

                if (data == null) {
                    throw new IllegalArgumentException(
                            "Gegevens voor capaciteitswissel zijn verplicht"
                    );
                }

                if (request.orderverplaatsing() != null) {
                    throw new IllegalArgumentException(
                            "Een capaciteitswissel mag geen orderverplaatsingsgegevens bevatten"
                    );
                }

                yield new Capaciteitswissel(
                        request.titel(),
                        request.beschrijving(),
                        request.reden(),
                        request.achtergrond(),
                        request.aannames(),
                        request.ingangsdatum(),
                        request.deadline(),
                        initiatiefnemer,
                        data.fabriek(),
                        data.finishType(),
                        data.grade(),
                        data.capaciteitVan(),
                        data.capaciteitNaar(),
                        data.capaciteitEenheid(),
                        data.periodeVan(),
                        data.periodeTot()
                );
            }

            case ORDERVERPLAATSING ->
                    throw new IllegalArgumentException(
                            "Orderverplaatsing wordt in deze stap nog niet ondersteund"
                    );
        };
    }

    private void controleerAlleIdsGevonden(
            List<UUID> gevraagdeIds,
            int gevondenAantal,
            String foutmelding
    ) {

        long uniekAantalIds =
                gevraagdeIds.stream()
                        .distinct()
                        .count();

        if (gevondenAantal != uniekAantalIds) {
            throw new IllegalArgumentException(
                    foutmelding
            );
        }
    }
}