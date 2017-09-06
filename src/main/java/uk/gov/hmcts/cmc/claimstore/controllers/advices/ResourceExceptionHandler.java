package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;

import java.util.List;

@ControllerAdvice
public class ResourceExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> internalServiceError(Exception exception) {
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>("Internal server error",
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR);
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
            builder.append(error.getField())
                .append(" : ")
                .append(error.getDefaultMessage())
                .append(", ");
        }

        builder.delete(builder.length() - 2, builder.length());
        return new ResponseEntity<>(builder.toString(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
