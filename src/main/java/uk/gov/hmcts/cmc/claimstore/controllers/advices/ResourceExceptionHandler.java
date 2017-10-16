package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import com.google.common.base.Throwables;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;

@ControllerAdvice
public class ResourceExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);
    private static final CharSequence UNIQUE_CONSTRAINT_MESSAGE = "duplicate key value violates unique constraint";

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> internalServiceError(Exception exception) {
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>("Internal server error", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = UnableToExecuteStatementException.class)
    public ResponseEntity<Object> unableToExecuteStatement(UnableToExecuteStatementException exception) {
        logger.error(exception.getMessage(), exception);

        final Optional<Throwable> cause = Optional.ofNullable(Throwables.getRootCause(exception))
            .filter(c -> c != exception);

        final Optional<String> exceptionName = cause.map(c -> c.getClass().getName());
        final Optional<String> message = cause.map(Throwable::getMessage);

        if (exceptionName.isPresent() && exceptionName.get().contains(PSQLException.class.getName())
            && message.isPresent() && message.get().contains(UNIQUE_CONSTRAINT_MESSAGE)) {
            return new ResponseEntity<>(message.get(), HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>("Internal server error", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<Object> httpClientErrorException(HttpClientErrorException exception) {
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(),
            HttpStatus.valueOf(exception.getRawStatusCode()));
    }

    @ExceptionHandler(value = ForbiddenActionException.class)
    public ResponseEntity<Object> forbidden(Exception exception) {
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<Object> notFoundClaim(Exception exception) {
        logger.debug(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = ConflictException.class)
    public ResponseEntity<Object> conflict(Exception exception) {
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> methodNotSupported(HttpRequestMethodNotSupportedException exception) {
        logger.trace(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(value = {
        HttpMediaTypeNotSupportedException.class,
        InvalidApplicationException.class,
        ServletRequestBindingException.class})
    public ResponseEntity<Object> badRequest(Exception exception) {
        logger.trace(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> validatedMethod(MethodArgumentNotValidException exception) {
        logger.error(exception.getMessage(), exception);

        final BindingResult result = exception.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<FieldError> errors = result.getFieldErrors();

        for (FieldError error : errors) {
            builder = addError(builder, error.getField(), error.getDefaultMessage());
        }

        ObjectError objectError = result.getGlobalError();

        if (objectError != null) {
            builder = addError(builder, objectError.getObjectName(), objectError.getDefaultMessage());
        }

        builder.delete(builder.length() - 2, builder.length());
        return new ResponseEntity<>(builder.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private StringBuilder addError(StringBuilder builder, String key, String message) {
        return builder.append(key)
            .append(" : ")
            .append(message)
            .append(", ");
    }
}
