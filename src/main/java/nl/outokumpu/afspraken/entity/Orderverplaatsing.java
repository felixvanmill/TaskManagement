package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.AfspraakType;
import nl.outokumpu.afspraken.enums.UitvoeringsStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orderverplaatsingen")
@PrimaryKeyJoinColumn(name = "id")
public class Orderverplaatsing extends OperationeleAfspraak {

    @Column(nullable = false, length = 100)
    private String vanFabriek;

    @Column(nullable = false, length = 100)
    private String naarFabriek;

    @Column(nullable = false, length = 100)
    private String finishType;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal totaalVolumeTons;

    @Column(nullable = false)
    private LocalDate gewensteVerplaatsingsdatum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UitvoeringsStatus uitvoeringsstatus;

    protected Orderverplaatsing() {
        // Vereist door JPA
    }

    public Orderverplaatsing(
            String titel,
            String beschrijving,
            String reden,
            String achtergrond,
            String aannames,
            LocalDate ingangsdatum,
            LocalDate deadline,
            Gebruiker initiatiefnemer,
            String vanFabriek,
            String naarFabriek,
            String finishType,
            BigDecimal totaalVolumeTons,
            LocalDate gewensteVerplaatsingsdatum
    ) {
        super(
                AfspraakType.ORDERVERPLAATSING,
                titel,
                beschrijving,
                reden,
                achtergrond,
                aannames,
                ingangsdatum,
                deadline,
                initiatiefnemer
        );

        this.vanFabriek = vanFabriek;
        this.naarFabriek = naarFabriek;
        this.finishType = finishType;
        this.totaalVolumeTons = totaalVolumeTons;
        this.gewensteVerplaatsingsdatum = gewensteVerplaatsingsdatum;
        this.uitvoeringsstatus =
                UitvoeringsStatus.NIET_UITGEVOERD;
    }

    public void wijzigUitvoeringsstatus(
            UitvoeringsStatus nieuweStatus
    ) {

        if (nieuweStatus == null) {
            throw new IllegalArgumentException(
                    "Nieuwe uitvoeringsstatus is verplicht"
            );
        }

        if (this.uitvoeringsstatus == nieuweStatus) {
            throw new IllegalArgumentException(
                    "Nieuwe uitvoeringsstatus moet verschillen van de huidige status"
            );
        }

        this.uitvoeringsstatus =
                nieuweStatus;
    }

    public void wijzigOrdergegevens(
            String vanFabriek,
            String naarFabriek,
            String finishType,
            BigDecimal totaalVolumeTons,
            LocalDate gewensteVerplaatsingsdatum
    ) {

        if (vanFabriek == null || vanFabriek.isBlank()) {
            throw new IllegalArgumentException(
                    "Fabriek van herkomst is verplicht"
            );
        }

        if (naarFabriek == null || naarFabriek.isBlank()) {
            throw new IllegalArgumentException(
                    "Bestemmingsfabriek is verplicht"
            );
        }

        if (finishType == null || finishType.isBlank()) {
            throw new IllegalArgumentException(
                    "Finish type is verplicht"
            );
        }

        if (totaalVolumeTons == null) {
            throw new IllegalArgumentException(
                    "Totaal volume is verplicht"
            );
        }

        if (gewensteVerplaatsingsdatum == null) {
            throw new IllegalArgumentException(
                    "Gewenste verplaatsingsdatum is verplicht"
            );
        }

        this.vanFabriek = vanFabriek;
        this.naarFabriek = naarFabriek;
        this.finishType = finishType;
        this.totaalVolumeTons = totaalVolumeTons;
        this.gewensteVerplaatsingsdatum =
                gewensteVerplaatsingsdatum;
    }

    public String getVanFabriek() {
        return vanFabriek;
    }

    public String getNaarFabriek() {
        return naarFabriek;
    }

    public String getFinishType() {
        return finishType;
    }

    public BigDecimal getTotaalVolumeTons() {
        return totaalVolumeTons;
    }

    public LocalDate getGewensteVerplaatsingsdatum() {
        return gewensteVerplaatsingsdatum;
    }

    public UitvoeringsStatus getUitvoeringsstatus() {
        return uitvoeringsstatus;
    }
}