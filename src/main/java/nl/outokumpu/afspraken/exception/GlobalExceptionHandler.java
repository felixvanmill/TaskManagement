package nl.outokumpu.afspraken.exception;

import jakarta.servlet.http.HttpServletRequest;

import nl.outokumpu.afspraken.dto.response.ApiErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> veldFouten =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        veldFouten.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        ApiErrorResponse response =
                new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Validatie mislukt",
                        request.getRequestURI(),
                        veldFouten
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {

        return maakResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {

        return maakResponse(
                HttpStatus.BAD_REQUEST,
                "Verplichte header ontbreekt: "
                        + exception.getHeaderName(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        return maakResponse(
                HttpStatus.BAD_REQUEST,
                "Ongeldige waarde voor parameter: "
                        + exception.getName(),
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleOnleesbareRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {

        return maakResponse(
                HttpStatus.BAD_REQUEST,
                "Request body bevat ongeldige gegevens",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleOnverwachteFout(
            Exception exception,
            HttpServletRequest request
    ) {

        /*
         * Geen technische foutdetails naar de client sturen.
         * Logging voegen we later centraal toe.
         */
        return maakResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Er is een onverwachte fout opgetreden",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> maakResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        request.getRequestURI(),
                        Map.of()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}