package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import com.google.common.base.Throwables;
import feign.FeignException;
import jakarta.servlet.ServletException;
import org.postgresql.util.PSQLException;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsExceptionLogger;
import uk.gov.hmcts.cmc.claimstore.exceptions.*;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FAILED_DEPENDENCY;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class ResourceExceptionHandler {
    private static final CharSequence UNIQUE_CONSTRAINT_MESSAGE = "duplicate key value violates unique constraint";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
    private static final String NOTIFICATION_CLIENT_EX_MESSAGE = "Error occurred during handling notification";
    private final AppInsightsExceptionLogger logger;

    @Autowired
    public ResourceExceptionHandler(AppInsightsExceptionLogger logger) {
        this.logger = logger;
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> internalServiceError(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = UnableToExecuteStatementException.class)
    public ResponseEntity<Object> unableToExecuteStatement(UnableToExecuteStatementException exception) {
        logger.error(exception);

        Optional<Throwable> cause = Optional.ofNullable(Throwables.getRootCause(exception))
            .filter(c -> c != exception);

        Optional<String> exceptionName = cause.map(c -> c.getClass().getName());
        Optional<String> message = cause.map(Throwable::getMessage);

        if (exceptionName.isPresent() && exceptionName.get().contains(PSQLException.class.getName())
            && message.isPresent() && message.get().contains(UNIQUE_CONSTRAINT_MESSAGE)) {
            return new ResponseEntity<>(message.get(), HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(INTERNAL_SERVER_ERROR, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<Object> httpClientErrorException(HttpClientErrorException exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatusCode());
    }

    @ExceptionHandler(value = ForbiddenActionException.class)
    public ResponseEntity<Object> forbidden(Exception exception) {
        logger.error(exception);
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ExceptionForClient(HttpStatus.FORBIDDEN.value(), exception.getMessage()));
    }

    @ExceptionHandler(value = DocumentDownloadForbiddenException.class)
    public ResponseEntity<Object> forbiddenDocumentDownload(DocumentDownloadForbiddenException exception) {
        logger.error(exception);
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .build();
    }

    @ExceptionHandler(value = {
        DefendantLinkingException.class, ClaimantLinkException.class
    })
    public ResponseEntity<Object> partyNotLinkedWithClaim(Exception exception) {
        logger.error(exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
            .body(new ExceptionForClient(HttpStatus.FORBIDDEN.value(), exception.getMessage()));
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<Object> notFoundClaim(Exception exception) {
        logger.debug(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = ConflictException.class)
    public ResponseEntity<Object> conflict(Exception exception) {
        logger.error(exception);
        return ResponseEntity.status(HttpStatus.CONFLICT.value())
            .body(new ExceptionForClient(HttpStatus.CONFLICT.value(), exception.getMessage()));
    }

    @ExceptionHandler(value = OnHoldClaimAccessAttemptException.class)
    public ResponseEntity<Object> onHoldClaim(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> methodNotSupported(HttpRequestMethodNotSupportedException exception) {
        logger.trace(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(value = {
        CallbackException.class,
        HttpMediaTypeNotSupportedException.class,
        ServletRequestBindingException.class})
    public ResponseEntity<Object> badRequest(Exception exception) {
        logger.trace(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ClaimCreationDisabledException.class)
    public ResponseEntity<Object> claimCreationDisabled(Exception exception) {
        logger.trace(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(value = InvalidApplicationException.class)
    public ResponseEntity<Object> invalidApplicationException(InvalidApplicationException exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> validatedMethod(MethodArgumentNotValidException exception) {
        logger.error(exception);

        BindingResult result = exception.getBindingResult();
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
        return new ResponseEntity<>(builder.toString(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private StringBuilder addError(StringBuilder builder, String key, String message) {
        return builder.append(key)
            .append(" : ")
            .append(message)
            .append(", ");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> methodArgumentConversionFailure(MethodArgumentTypeMismatchException exception) {
        logger.debug(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalSettlementStatementException.class)
    public ResponseEntity<String> illegalSettlementStatement(IllegalSettlementStatementException exception) {
        logger.debug(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> badRequestException(BadRequestException exception) {
        logger.debug(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException exc) {
        logger.warn("Error communicating with an API", exc);
        String errorMessage = exc.status() < HttpStatus.INTERNAL_SERVER_ERROR.value() ? exc
            .getMessage() : INTERNAL_SERVER_ERROR;
        return ResponseEntity
            .status(exc.status())
            .body(new ExceptionForClient(exc.status(), errorMessage));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<String> duplicateKeyException(DuplicateKeyException exception) {
        logger.debug(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<String> notificationException(NotificationException exception) {
        logger.debug(exception);
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CoreCaseDataStoreException.class)
    public ResponseEntity<Object> handleCoreCaseDataStoreException(Exception exception) {
        logger.error(exception);
        return ResponseEntity
            .status(FAILED_DEPENDENCY)
            .body(new ExceptionForClient(FAILED_DEPENDENCY.value(), exception.getMessage()));
    }

    @ExceptionHandler(FeignException.InternalServerError.class)
    public ResponseEntity<Object> handleFeignExceptionInternalServerError(
        FeignException.InternalServerError exception
    ) {
        logger.error(exception);
        return ResponseEntity
            .status(FAILED_DEPENDENCY)
            .body(new ExceptionForClient(FAILED_DEPENDENCY.value(), exception.getMessage()));
    }

    @ExceptionHandler({FeignException.UnprocessableEntity.class, UnprocessableEntityException.class})
    public ResponseEntity<Object> handleUnprocessableEntity(Exception exception) {
        logger.error(exception);
        return ResponseEntity
            .status(UNPROCESSABLE_ENTITY)
            .body(new ExceptionForClient(UNPROCESSABLE_ENTITY.value(), exception.getMessage()));
    }

    @ExceptionHandler({FeignException.GatewayTimeout.class, SocketTimeoutException.class})
    public ResponseEntity<String> handleFeignExceptionGatewayTimeout(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(),
            new HttpHeaders(), HttpStatus.GATEWAY_TIMEOUT);
    }

    @ExceptionHandler(NotificationClientException.class)
    public ResponseEntity<Object> handleNotificationClientException(Exception exception) {
        logger.error(exception);
        return ResponseEntity.status(BAD_REQUEST).body(
            new NotificationClientException(
                NOTIFICATION_CLIENT_EX_MESSAGE,
                exception
            ));
    }

    @ExceptionHandler({ServletException.class, FeignException.BadRequest.class})
    public ResponseEntity<String> handleNestedServletExceptionBadRequest(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(),
            new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> handleHttpMediaTypeNotAcceptableException(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(),
            new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(FeignException.UnsupportedMediaType.class)
    public ResponseEntity<String> handleHttpUnsupportedMediaTypeException(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(),
            new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(DocumentManagementException.class)
    public ResponseEntity<String> handleDocumentManagementException(Exception exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(),
            new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        logger.error(exception);
        return new ResponseEntity<>(exception.getMessage(),
            new HttpHeaders(), HttpStatus.PRECONDITION_FAILED);
    }
}
