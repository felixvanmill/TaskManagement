package nl.outokumpu.afspraken.repository;

import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.enums.AfspraakStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OperationeleAfspraakRepository
        extends JpaRepository<OperationeleAfspraak, UUID>,
        JpaSpecificationExecutor<OperationeleAfspraak> {

    List<OperationeleAfspraak>
    findByStatusNotOrderByLaatstGewijzigdOpDesc(
            AfspraakStatus status
    );

    List<OperationeleAfspraak>
    findByStatusOrderByLaatstGewijzigdOpDesc(
            AfspraakStatus status
    );

    @Query("""
            SELECT a
            FROM OperationeleAfspraak a
            WHERE LOWER(a.titel)
                    LIKE LOWER(CONCAT('%', :zoekterm, '%'))
               OR LOWER(a.beschrijving)
                    LIKE LOWER(CONCAT('%', :zoekterm, '%'))
               OR LOWER(a.reden)
                    LIKE LOWER(CONCAT('%', :zoekterm, '%'))
               OR LOWER(a.achtergrond)
                    LIKE LOWER(CONCAT('%', :zoekterm, '%'))
               OR LOWER(a.aannames)
                    LIKE LOWER(CONCAT('%', :zoekterm, '%'))
            ORDER BY a.laatstGewijzigdOp DESC
            """)
    List<OperationeleAfspraak> zoekOpTrefwoord(
            @Param("zoekterm")
            String zoekterm
    );
}