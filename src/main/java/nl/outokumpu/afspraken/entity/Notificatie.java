package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import nl.outokumpu.afspraken.enums.NotificatieType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificaties")
public class Notificatie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ontvanger_id", nullable = false)
    private Gebruiker ontvanger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "afspraak_id", nullable = false)
    private OperationeleAfspraak afspraak;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificatieType type;

    @Column(nullable = false, length = 500)
    private String bericht;

    @Column(nullable = false)
    private Instant aangemaaktOp;

    private Instant gelezenOp;

    private Instant afgehandeldOp;

    protected Notificatie() {
        // Vereist door JPA
    }

    public Notificatie(
            Gebruiker ontvanger,
            OperationeleAfspraak afspraak,
            NotificatieType type,
            String bericht
    ) {
        if (ontvanger == null || afspraak == null
                || type == null || bericht == null
                || bericht.isBlank()) {
            throw new IllegalArgumentException(
                    "Notificatiegegevens zijn verplicht"
            );
        }

        this.ontvanger = ontvanger;
        this.afspraak = afspraak;
        this.type = type;
        this.bericht = bericht;
        this.aangemaaktOp = Instant.now();
    }

    public void markeerGelezen() {
        if (gelezenOp == null) {
            gelezenOp = Instant.now();
        }
    }

    public void handelAf() {
        if (afgehandeldOp == null) {
            afgehandeldOp = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Gebruiker getOntvanger() {
        return ontvanger;
    }

    public OperationeleAfspraak getAfspraak() {
        return afspraak;
    }

    public NotificatieType getType() {
        return type;
    }

    public String getBericht() {
        return bericht;
    }

    public Instant getAangemaaktOp() {
        return aangemaaktOp;
    }

    public Instant getGelezenOp() {
        return gelezenOp;
    }

    public Instant getAfgehandeldOp() {
        return afgehandeldOp;
    }
}