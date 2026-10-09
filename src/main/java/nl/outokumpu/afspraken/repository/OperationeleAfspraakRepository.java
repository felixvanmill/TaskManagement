package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OperationeleAfspraakRepository
        extends JpaRepository<OperationeleAfspraak, UUID> {

    List<OperationeleAfspraak>
    findByStatusNotOrderByLaatstGewijzigdOpDesc(
            AfspraakStatus status
    );

    List<OperationeleAfspraak>
    findByStatusOrderByLaatstGewijzigdOpDesc(
            AfspraakStatus status
    );
}