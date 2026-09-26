
package nl.outokumpu.afspraken.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "afdelingen")
public class Afdeling {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String naam;

    protected Afdeling() {
        // Vereist door JPA
    }

    public Afdeling(String naam) {
        this.naam = naam;
    }

    public UUID getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }
}
