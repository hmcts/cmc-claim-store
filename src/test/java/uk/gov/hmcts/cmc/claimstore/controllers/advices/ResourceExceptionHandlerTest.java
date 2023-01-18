package uk.gov.hmcts.cmc.claimstore.controllers.advices;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsExceptionLogger;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DuplicateKeyException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.exceptions.OnHoldClaimAccessAttemptException;
import uk.gov.hmcts.cmc.claimstore.exceptions.UnprocessableEntityException;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.exceptions.IllegalSettlementStatementException;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.net.SocketTimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResourceExceptionHandlerTest {
    @Mock
    private AppInsightsExceptionLogger appInsightsExceptionLogger;

    private ResourceExceptionHandler handler;

    @Before
    public void setUp() {
        handler = new ResourceExceptionHandler(appInsightsExceptionLogger);
    }

    @Test
    public void testBadRequest() {
        testTemplate(
            "expected exception for bad request",
            CallbackException::new,
            handler::badRequest,
            HttpStatus.BAD_REQUEST,
            AppInsightsExceptionLogger::trace
        );
    }

    @Test
    public void testBadRequestException() {
        testTemplate(
            "expected exception for bad request exception",
            BadRequestException::new,
            handler::badRequestException,
            HttpStatus.BAD_REQUEST,
            AppInsightsExceptionLogger::debug
        );
    }

    @Test
    public void testConflict() {
        testTemplate(
            "expected exception for conflict",
            ConflictException::new,
            handler::conflict,
            HttpStatus.CONFLICT,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testForbidden() {
        testTemplate(
            "expected exception for forbidden",
            ForbiddenActionException::new,
            handler::forbidden,
            HttpStatus.FORBIDDEN,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testHttpClientErrorException() {
        testTemplate(
            "expected exception for http client error exception",
            m -> new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT, m),
            handler::httpClientErrorException,
            HttpStatus.I_AM_A_TEAPOT,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testIllegalSettlementStatement() {
        testTemplate(
            "expected exception for illegal settlement statement",
            IllegalSettlementStatementException::new,
            handler::illegalSettlementStatement,
            HttpStatus.BAD_REQUEST,
            AppInsightsExceptionLogger::debug
        );
    }

    @Test
    public void testInternalServiceError() {
        testTemplate(
            "Internal server error",
            RuntimeException::new,
            handler::internalServiceError,
            HttpStatus.INTERNAL_SERVER_ERROR,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testInvalidApplicationException() {
        testTemplate(
            "expected exception for invalid application exception",
            m -> new InvalidApplicationException(m, null),
            handler::invalidApplicationException,
            HttpStatus.INTERNAL_SERVER_ERROR,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testMethodNotSupported() {
        testTemplate(
            "expected exception for method not supported",
            m -> new HttpRequestMethodNotSupportedException("method", m),
            handler::methodNotSupported,
            HttpStatus.NOT_IMPLEMENTED,
            AppInsightsExceptionLogger::trace
        );
    }

    @Test
    public void testNotFoundClaim() {
        testTemplate(
            "expected exception for not found claim",
            NotFoundException::new,
            handler::notFoundClaim,
            HttpStatus.NOT_FOUND,
            AppInsightsExceptionLogger::debug
        );
    }

    @Test
    public void testOnHoldClaim() {
        testTemplate(
            "expected exception for on hold claim",
            OnHoldClaimAccessAttemptException::new,
            handler::onHoldClaim,
            HttpStatus.CONFLICT,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testHandleCoreCaseDataStoreException() {
        testTemplate(
            "expected exception for core case data api",
            CoreCaseDataStoreException::new,
            handler::handleCoreCaseDataStoreException,
            HttpStatus.FAILED_DEPENDENCY,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testHandleUnprocessableEntity() {
        testTemplate(
            "expected exception for on unprocessable entity",
            UnprocessableEntityException::new,
            handler::handleUnprocessableEntity,
            HttpStatus.UNPROCESSABLE_ENTITY,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testPartyNotLinkedWithClaim() {
        testTemplate(
            "expected exception for party not linked with claim",
            DefendantLinkingException::new,
            handler::partyNotLinkedWithClaim,
            HttpStatus.FORBIDDEN,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testUnableToExecuteStatementWithNoRootCause() {
        testTemplate(
            "Internal server error",
            m -> new UnableToExecuteStatementException((Exception) null, null),
            handler::unableToExecuteStatement,
            HttpStatus.INTERNAL_SERVER_ERROR,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testUnableToExecuteStatementWithAUniqueConstraintRootCause() {
        testTemplate(
            "expected exception: duplicate key value violates unique constraint",
            m -> new UnableToExecuteStatementException(new PSQLException(m, PSQLState.UNKNOWN_STATE), null),
            handler::unableToExecuteStatement,
            HttpStatus.CONFLICT,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testDuplicateKeyException() {
        testTemplate(
            "expected exception for duplicate key exception",
            DuplicateKeyException::new,
            handler::duplicateKeyException,
            HttpStatus.CONFLICT,
            AppInsightsExceptionLogger::debug
        );
    }

    @Test
    public void testNotificationException() {
        testTemplate(
            "expected exception for notification exception",
            NotificationException::new,
            handler::notificationException,
            HttpStatus.BAD_REQUEST,
            AppInsightsExceptionLogger::debug
        );
    }

    @Test
    public void testFeignExceptionGatewayTimeoutException() {
        testTemplate(
            "expected exception for notification exception",
            SocketTimeoutException::new,
            handler::handleFeignExceptionGatewayTimeout,
            HttpStatus.GATEWAY_TIMEOUT,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testNestedServletExceptionBadRequest() {
        testTemplate(
            "expected exception for notification exception",
            SocketTimeoutException::new,
            handler::handleNestedServletExceptionBadRequest,
            HttpStatus.BAD_REQUEST,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testHttpMediaTypeNotAcceptableException() {
        testTemplate(
            "expected exception for notification exception",
            SocketTimeoutException::new,
            handler::handleHttpMediaTypeNotAcceptableException,
            HttpStatus.UNPROCESSABLE_ENTITY,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testHttpUnsupportedMediaTypeException() {
        testTemplate(
            "expected exception for notification exception",
            HttpMediaTypeNotAcceptableException::new,
            handler::handleHttpUnsupportedMediaTypeException,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testDocumentManagementException() {
        testTemplate(
            "expected exception for notification exception",
            HttpMediaTypeNotAcceptableException::new,
            handler::handleDocumentManagementException,
            HttpStatus.UNPROCESSABLE_ENTITY,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testHandleIllegalArgumentException() {
        testTemplate(
            "require non null object passed in mapper's arguments",
            IllegalArgumentException::new,
            handler::handleIllegalArgumentException,
            HttpStatus.PRECONDITION_FAILED,
            AppInsightsExceptionLogger::error
        );
    }

    @Test
    public void testNotificationClientException() {
        testTemplate(
            "Error occurred during handling notification",
            m -> new NotificationClientException(
                "Error occurred during handling notification",
                null
            ),
            handler::handleNotificationClientException,
            HttpStatus.BAD_REQUEST,
            AppInsightsExceptionLogger::error
        );
    }

    private <E extends Exception> void testTemplate(
        String message,
        Function<String, E> exceptionBuilder,
        Function<E, ResponseEntity<?>> method,
        HttpStatus expectedStatus,
        BiConsumer<AppInsightsExceptionLogger, E> verification
    ) {
        E exception = exceptionBuilder.apply(message);
        ResponseEntity<?> result = method.apply(exception);
        assertThat(result.getStatusCode()).isSameAs(expectedStatus);
        assertThat(result.getBody()).isNotNull()
            .extracting(Object::toString).asString().contains(message);
        verification.accept(verify(appInsightsExceptionLogger), exception);
    }
}
