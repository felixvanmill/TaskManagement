package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.Wijziging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WijzigingRepository extends JpaRepository<Wijziging, UUID> {

    List<Wijziging> findByAfspraakIdOrderByGewijzigdOpAsc(UUID afspraakId);
}