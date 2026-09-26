
package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.Beslissing;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bevestigingen",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bevestiging_afspraak_gebruiker",
                        columnNames = {"afspraak_id", "gebruiker_id"}
                )
        }
)
public class Bevestiging {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afspraak_id", nullable = false)
    private OperationeleAfspraak afspraak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gebruiker_id", nullable = false)
    private Gebruiker gebruiker;

    @Column(nullable = false)
    private boolean gezien;

    private Instant gezienOp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Beslissing beslissing;

    private Instant beslistOp;

    @Version
    private Long versie;

    protected Bevestiging() {
        // Vereist door JPA
    }

    public Bevestiging(
            OperationeleAfspraak afspraak,
            Gebruiker gebruiker
    ) {
        this.afspraak = afspraak;
        this.gebruiker = gebruiker;
        this.gezien = false;
        this.beslissing = Beslissing.GEEN;
    }

    public UUID getId() {
        return id;
    }

    public OperationeleAfspraak getAfspraak() {
        return afspraak;
    }

    public Gebruiker getGebruiker() {
        return gebruiker;
    }

    public boolean isGezien() {
        return gezien;
    }

    public Instant getGezienOp() {
        return gezienOp;
    }

    public Beslissing getBeslissing() {
        return beslissing;
    }

    public Instant getBeslistOp() {
        return beslistOp;
    }

    public Long getVersie() {
        return versie;
    }
}
