package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wijzigingen")
public class Wijziging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afspraak_id", nullable = false, updatable = false)
    private OperationeleAfspraak afspraak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gewijzigd_door_id", nullable = false, updatable = false)
    private Gebruiker gewijzigdDoor;

    @Column(nullable = false, updatable = false)
    private Instant gewijzigdOp;

    @Column(nullable = false, length = 100, updatable = false)
    private String onderdeel;

    @Column(length = 5000, updatable = false)
    private String oudeWaarde;

    @Column(length = 5000, updatable = false)
    private String nieuweWaarde;

    protected Wijziging() {
        // Vereist door JPA
    }

    public Wijziging(
            OperationeleAfspraak afspraak,
            Gebruiker gewijzigdDoor,
            String onderdeel,
            String oudeWaarde,
            String nieuweWaarde
    ) {
        this.afspraak = afspraak;
        this.gewijzigdDoor = gewijzigdDoor;
        this.onderdeel = onderdeel;
        this.oudeWaarde = oudeWaarde;
        this.nieuweWaarde = nieuweWaarde;
    }

    @PrePersist
    protected void bijAanmaken() {
        if (this.gewijzigdOp == null) {
            this.gewijzigdOp = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public OperationeleAfspraak getAfspraak() {
        return afspraak;
    }

    public Gebruiker getGewijzigdDoor() {
        return gewijzigdDoor;
    }

    public Instant getGewijzigdOp() {
        return gewijzigdOp;
    }

    public String getOnderdeel() {
        return onderdeel;
    }

    public String getOudeWaarde() {
        return oudeWaarde;
    }

    public String getNieuweWaarde() {
        return nieuweWaarde;
    }
}
