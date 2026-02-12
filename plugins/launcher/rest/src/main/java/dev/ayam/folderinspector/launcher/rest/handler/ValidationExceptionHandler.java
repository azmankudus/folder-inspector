package dev.ayam.folderinspector.launcher.rest.handler;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for validation errors.
 * Returns 400 Bad Request with detailed error messages.
 */
@Produces
@Singleton
@Requires(classes = {ConstraintViolationException.class, ExceptionHandler.class})
public class ValidationExceptionHandler 
    implements ExceptionHandler<ConstraintViolationException, HttpResponse<Map<String, Object>>> {

  @Override
  public HttpResponse<Map<String, Object>> handle(
      HttpRequest request, 
      ConstraintViolationException exception) {
    
    var errors = exception.getConstraintViolations().stream()
        .collect(Collectors.toMap(
            v -> getPropertyName(v),
            ConstraintViolation::getMessage,
            (a, b) -> a + "; " + b
        ));

    return HttpResponse.status(HttpStatus.BAD_REQUEST)
        .body(Map.of(
            "error", "Validation failed",
            "status", 400,
            "violations", errors
        ));
  }

  private String getPropertyName(ConstraintViolation<?> violation) {
    String path = violation.getPropertyPath().toString();
    int dotIndex = path.lastIndexOf('.');
    return dotIndex > 0 ? path.substring(dotIndex + 1) : path;
  }
}
