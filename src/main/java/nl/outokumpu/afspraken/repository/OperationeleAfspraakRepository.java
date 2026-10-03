package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationeleAfspraakRepository
        extends JpaRepository<OperationeleAfspraak, UUID> {
}