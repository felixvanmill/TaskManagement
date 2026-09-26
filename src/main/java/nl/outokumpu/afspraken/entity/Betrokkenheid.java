package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.BetrokkenRol;

import java.util.UUID;

@Entity
@Table(
        name = "betrokkenheden",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_afspraak_gebruiker_rol",
                        columnNames = {
                                "afspraak_id",
                                "gebruiker_id",
                                "rol"
                        }
                )
        }
)
public class Betrokkenheid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afspraak_id", nullable = false)
    private OperationeleAfspraak afspraak;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gebruiker_id", nullable = false)
    private Gebruiker gebruiker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BetrokkenRol rol;

    protected Betrokkenheid() {
        // Vereist door JPA
    }

    public Betrokkenheid(
            OperationeleAfspraak afspraak,
            Gebruiker gebruiker,
            BetrokkenRol rol
    ) {
        this.afspraak = afspraak;
        this.gebruiker = gebruiker;
        this.rol = rol;
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

    public BetrokkenRol getRol() {
        return rol;
    }
}
