package nl.outokumpu.afspraken.mapper;

import nl.outokumpu.afspraken.dto.data.CapaciteitswisselData;
import nl.outokumpu.afspraken.dto.data.OrderverplaatsingData;
import nl.outokumpu.afspraken.dto.response.*;
import nl.outokumpu.afspraken.entity.*;
import nl.outokumpu.afspraken.enums.BetrokkenRol;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AfspraakMapper {

    public AfspraakDetailResponse naarDetailResponse(
            OperationeleAfspraak afspraak
    ) {

        CapaciteitswisselData capaciteitswisselData = null;
        OrderverplaatsingData orderverplaatsingData = null;

        if (afspraak instanceof Capaciteitswissel capaciteitswissel) {
            capaciteitswisselData =
                    new CapaciteitswisselData(
                            capaciteitswissel.getFabriek(),
                            capaciteitswissel.getFinishType(),
                            capaciteitswissel.getGrade(),
                            capaciteitswissel.getCapaciteitVan(),
                            capaciteitswissel.getCapaciteitNaar(),
                            capaciteitswissel.getCapaciteitEenheid(),
                            capaciteitswissel.getPeriodeVan(),
                            capaciteitswissel.getPeriodeTot()
                    );
        }

        if (afspraak instanceof Orderverplaatsing orderverplaatsing) {
            orderverplaatsingData =
                    new OrderverplaatsingData(
                            orderverplaatsing.getVanFabriek(),
                            orderverplaatsing.getNaarFabriek(),
                            orderverplaatsing.getFinishType(),
                            orderverplaatsing.getTotaalVolumeTons(),
                            orderverplaatsing.getGewensteVerplaatsingsdatum(),
                            orderverplaatsing.getUitvoeringsstatus()
                    );
        }

        List<AfdelingResponse> afdelingen =
                afspraak.getBetrokkenAfdelingen()
                        .stream()
                        .map(this::naarAfdelingResponse)
                        .toList();

        List<GebruikerResponse> betrokkenGebruikers =
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(betrokkenheid ->
                                betrokkenheid.getRol()
                                        == BetrokkenRol.BETROKKENE
                        )
                        .map(Betrokkenheid::getGebruiker)
                        .map(this::naarGebruikerResponse)
                        .toList();

        List<GebruikerResponse> goedkeurders =
                afspraak.getBetrokkenheden()
                        .stream()
                        .filter(betrokkenheid ->
                                betrokkenheid.getRol()
                                        == BetrokkenRol.GOEDKEURDER
                        )
                        .map(Betrokkenheid::getGebruiker)
                        .map(this::naarGebruikerResponse)
                        .toList();

        List<ProcesstapResponse> processtappen =
                afspraak.getProcesstappen()
                        .stream()
                        .map(processtap ->
                                new ProcesstapResponse(
                                        processtap.getId(),
                                        processtap.getNaam(),
                                        processtap.getVolgorde(),
                                        processtap.getStatus(),
                                        processtap.getDeadline(),
                                        processtap.getGestartOp(),
                                        processtap.getAfgerondOp(),
                                        naarGebruikerResponse(
                                                processtap.getVerantwoordelijke()
                                        )
                                )
                        )
                        .toList();

        List<BevestigingResponse> bevestigingen =
                afspraak.getBevestigingen()
                        .stream()
                        .map(bevestiging ->
                                new BevestigingResponse(
                                        bevestiging.getId(),
                                        naarGebruikerResponse(
                                                bevestiging.getGebruiker()
                                        ),
                                        bevestiging.isGezien(),
                                        bevestiging.getGezienOp(),
                                        bevestiging.getBeslissing(),
                                        bevestiging.getBeslistOp()
                                )
                        )
                        .toList();

        List<WijzigingResponse> wijzigingen =
                afspraak.getWijzigingen()
                        .stream()
                        .map(wijziging ->
                                new WijzigingResponse(
                                        wijziging.getId(),
                                        naarGebruikerResponse(
                                                wijziging.getGewijzigdDoor()
                                        ),
                                        wijziging.getGewijzigdOp(),
                                        wijziging.getOnderdeel(),
                                        wijziging.getOudeWaarde(),
                                        wijziging.getNieuweWaarde()
                                )
                        )
                        .toList();

        return new AfspraakDetailResponse(
                afspraak.getId(),
                afspraak.getTitel(),
                afspraak.getBeschrijving(),
                afspraak.getReden(),
                afspraak.getAchtergrond(),
                afspraak.getAannames(),
                afspraak.getType(),
                afspraak.getStatus(),
                afspraak.getIngangsdatum(),
                afspraak.getDeadline(),
                afspraak.getAangemaaktOp(),
                afspraak.getLaatstGewijzigdOp(),
                afspraak.getAfgerondOp(),
                naarGebruikerResponse(
                        afspraak.getInitiatiefnemer()
                ),
                afdelingen,
                betrokkenGebruikers,
                goedkeurders,
                processtappen,
                bevestigingen,
                wijzigingen,
                capaciteitswisselData,
                orderverplaatsingData
        );
    }

    public AfspraakSummaryResponse naarSummaryResponse(
            OperationeleAfspraak afspraak
    ) {
        return new AfspraakSummaryResponse(
                afspraak.getId(),
                afspraak.getTitel(),
                afspraak.getType(),
                afspraak.getStatus(),
                afspraak.getIngangsdatum(),
                afspraak.getDeadline(),
                naarGebruikerResponse(
                        afspraak.getInitiatiefnemer()
                ),
                afspraak.getLaatstGewijzigdOp()
        );
    }

    private GebruikerResponse naarGebruikerResponse(
            Gebruiker gebruiker
    ) {
        return new GebruikerResponse(
                gebruiker.getId(),
                gebruiker.getNaam(),
                gebruiker.getEmail(),
                naarAfdelingResponse(
                        gebruiker.getAfdeling()
                )
        );
    }

    private AfdelingResponse naarAfdelingResponse(
            Afdeling afdeling
    ) {
        return new AfdelingResponse(
                afdeling.getId(),
                afdeling.getNaam()
        );
    }
}