package nl.outokumpu.afspraken;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import nl.outokumpu.afspraken.dto.request.CreateAfspraakRequest;
import nl.outokumpu.afspraken.enums.AfspraakType;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidatieTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void weigertLegeTitelBijAanmakenAfspraak() {

        CreateAfspraakRequest request = new CreateAfspraakRequest(
                "   ",
                "Beschrijving",
                "Reden",
                null,
                null,
                AfspraakType.ALGEMEEN,
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 31),
                UUID.randomUUID(),
                "Eerste beoordeling",
                LocalDate.of(2026, 10, 15),
                List.of(), // betrokkeneIds
                List.of(), // goedkeurderIds
                List.of(), // afdelingIds
                null,
                null
        );

        Set<ConstraintViolation<CreateAfspraakRequest>> overtredingen =
                validator.validate(request);

        assertTrue(
                overtredingen.stream()
                        .anyMatch(overtreding ->
                                overtreding.getPropertyPath()
                                        .toString()
                                        .equals("titel"))
        );
    }

    @Test
    void accepteertGeldigeCreateAfspraakRequest() {

        CreateAfspraakRequest request = new CreateAfspraakRequest(
                "Nieuwe operationele afspraak",
                "Beschrijving van de afspraak",
                "Operationele reden",
                "Achtergrondinformatie",
                "Aanname voor planning",
                AfspraakType.ALGEMEEN,
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 31),
                UUID.randomUUID(),
                "Eerste beoordeling",
                LocalDate.of(2026, 10, 15),
                List.of(), // betrokkeneIds
                List.of(), // goedkeurderIds
                List.of(), // afdelingIds
                null,
                null
        );

        Set<ConstraintViolation<CreateAfspraakRequest>> overtredingen =
                validator.validate(request);

        assertTrue(overtredingen.isEmpty());
    }

    @Test
    void weigertOntbrekendAfspraakType() {

        CreateAfspraakRequest request = new CreateAfspraakRequest(
                "Nieuwe operationele afspraak",
                "Beschrijving",
                "Reden",
                null,
                null,
                null,
                LocalDate.of(2026, 10, 3),
                LocalDate.of(2026, 10, 31),
                UUID.randomUUID(),
                "Eerste beoordeling",
                LocalDate.of(2026, 10, 15),
                List.of(), // betrokkeneIds
                List.of(), // goedkeurderIds
                List.of(), // afdelingIds
                null,
                null
        );

        Set<ConstraintViolation<CreateAfspraakRequest>> overtredingen =
                validator.validate(request);

        assertTrue(
                overtredingen.stream()
                        .anyMatch(overtreding ->
                                overtreding.getPropertyPath()
                                        .toString()
                                        .equals("type"))
        );
    }
}