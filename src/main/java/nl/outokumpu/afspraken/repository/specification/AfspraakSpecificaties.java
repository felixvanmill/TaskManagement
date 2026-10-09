package nl.outokumpu.afspraken.repository.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import nl.outokumpu.afspraken.dto.request.AfspraakFilterRequest;
import nl.outokumpu.afspraken.entity.Capaciteitswissel;
import nl.outokumpu.afspraken.entity.OperationeleAfspraak;
import nl.outokumpu.afspraken.entity.Orderverplaatsing;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AfspraakSpecificaties {

    private AfspraakSpecificaties() {
    }

    public static Specification<OperationeleAfspraak> metFilters(
            AfspraakFilterRequest filter
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            /*
             * Door joins met processtappen en afdelingen kunnen
             * dezelfde afspraken meerdere keren in het SQL-resultaat
             * voorkomen.
             */
            query.distinct(true);

            if (filter.type() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("type"),
                                filter.type()
                        )
                );
            }

            if (filter.status() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                filter.status()
                        )
                );
            }

            if (filter.verantwoordelijkeId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.join(
                                                "processtappen",
                                                JoinType.INNER
                                        )
                                        .get("verantwoordelijke")
                                        .get("id"),
                                filter.verantwoordelijkeId()
                        )
                );
            }

            if (filter.afdelingId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.join(
                                                "betrokkenAfdelingen",
                                                JoinType.INNER
                                        )
                                        .get("id"),
                                filter.afdelingId()
                        )
                );
            }

            if (filter.fabriek() != null
                    && !filter.fabriek().isBlank()) {

                String patroon =
                        "%"
                                + filter.fabriek()
                                .trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                Root<Capaciteitswissel> capaciteitswissel =
                        criteriaBuilder.treat(
                                root,
                                Capaciteitswissel.class
                        );

                Root<Orderverplaatsing> orderverplaatsing =
                        criteriaBuilder.treat(
                                root,
                                Orderverplaatsing.class
                        );

                Predicate capaciteitFabriek =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        capaciteitswissel
                                                .<String>get("fabriek")
                                ),
                                patroon
                        );

                Predicate orderVanFabriek =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        orderverplaatsing
                                                .<String>get("vanFabriek")
                                ),
                                patroon
                        );

                Predicate orderNaarFabriek =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        orderverplaatsing
                                                .<String>get("naarFabriek")
                                ),
                                patroon
                        );

                predicates.add(
                        criteriaBuilder.or(
                                capaciteitFabriek,
                                orderVanFabriek,
                                orderNaarFabriek
                        )
                );
            }

            /*
             * Periode werkt als overlap:
             *
             * afspraak:  |----------|
             * filter:        |----------|
             *
             * Ook zo'n afspraak hoort dus bij de resultaten.
             */

            if (filter.periodeVan() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.<LocalDate>get("deadline"),
                                filter.periodeVan()
                        )
                );
            }

            if (filter.periodeTot() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.<LocalDate>get("ingangsdatum"),
                                filter.periodeTot()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}