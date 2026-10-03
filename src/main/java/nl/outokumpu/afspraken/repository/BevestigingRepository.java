package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Bevestiging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BevestigingRepository extends JpaRepository<Bevestiging, UUID> {

    List<Bevestiging> findByAfspraakId(UUID afspraakId);
}