package cl.duoc.digitalfix.config;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<Map<String, Object>> downstream(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        String body = exception.getResponseBodyAsString();
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", OffsetDateTime.now(),
                "status", status,
                "error", body.isBlank() ? "Downstream service rejected the request" : body));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> badRequest(IllegalArgumentException exception) {
        return Map.of("timestamp", OffsetDateTime.now(), "status", 400, "error", exception.getMessage());
    }
}
