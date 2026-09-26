package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.AfspraakType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "capaciteitswissels")
@PrimaryKeyJoinColumn(name = "id")
public class Capaciteitswissel extends OperationeleAfspraak {

    @Column(nullable = false, length = 100)
    private String fabriek;

    @Column(nullable = false, length = 100)
    private String finishType;

    @Column(nullable = false, length = 100)
    private String grade;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal capaciteitVan;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal capaciteitNaar;

    @Column(nullable = false, length = 50)
    private String capaciteitEenheid;

    @Column(nullable = false)
    private LocalDate periodeVan;

    @Column(nullable = false)
    private LocalDate periodeTot;

    protected Capaciteitswissel() {
        // Vereist door JPA
    }

    public Capaciteitswissel(
            String titel,
            String beschrijving,
            String reden,
            String achtergrond,
            String aannames,
            LocalDate ingangsdatum,
            LocalDate deadline,
            Gebruiker initiatiefnemer,
            String fabriek,
            String finishType,
            String grade,
            BigDecimal capaciteitVan,
            BigDecimal capaciteitNaar,
            String capaciteitEenheid,
            LocalDate periodeVan,
            LocalDate periodeTot
    ) {
        super(
                AfspraakType.CAPACITEITSWISSEL,
                titel,
                beschrijving,
                reden,
                achtergrond,
                aannames,
                ingangsdatum,
                deadline,
                initiatiefnemer
        );

        this.fabriek = fabriek;
        this.finishType = finishType;
        this.grade = grade;
        this.capaciteitVan = capaciteitVan;
        this.capaciteitNaar = capaciteitNaar;
        this.capaciteitEenheid = capaciteitEenheid;
        this.periodeVan = periodeVan;
        this.periodeTot = periodeTot;
    }

    public String getFabriek() {
        return fabriek;
    }

    public String getFinishType() {
        return finishType;
    }

    public String getGrade() {
        return grade;
    }

    public BigDecimal getCapaciteitVan() {
        return capaciteitVan;
    }

    public BigDecimal getCapaciteitNaar() {
        return capaciteitNaar;
    }

    public String getCapaciteitEenheid() {
        return capaciteitEenheid;
    }

    public LocalDate getPeriodeVan() {
        return periodeVan;
    }

    public LocalDate getPeriodeTot() {
        return periodeTot;
    }
}
