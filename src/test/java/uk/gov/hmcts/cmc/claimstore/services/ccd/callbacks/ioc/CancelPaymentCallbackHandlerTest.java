package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CANCEL_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getAmountBreakDown;

@ExtendWith(MockitoExtension.class)
class CancelPaymentCallbackHandlerTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final Long CASE_ID = 42L;
    private static final String PAY_REFERENCE = "RC-1234-1234-1234-1234";

    @Mock
    private PaymentsService paymentsService;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private CancelPaymentCallbackHandler handler;
    private CaseDetailsConverter caseDetailsConverter;
    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        final JsonMapper jsonMapper = new JsonMapper(new JacksonConfiguration().objectMapper());
        caseDetailsConverter = new CaseDetailsConverter(caseMapper, jsonMapper, workingDayIndicator, 12);
        handler = new CancelPaymentCallbackHandler(paymentsService, caseDetailsConverter, caseMapper);
    }

    @Nested
    @DisplayName("Failures")
    class FailedCallbackTests {
        @BeforeEach
        void setUp() {
        }

        @Test
        void shouldThrowNPEWhenClaimIsMissing() {
            callbackParams = buildCallbackParams(null);

            assertThatThrownBy(() -> handler.handle(callbackParams))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenPaymentMissing() {
            final Claim claim = SampleClaim.getDefault();
            final CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown()).toBuilder()
                .feeAmountInPennies("1000")
                .build();
            when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

            Map<String, Object> ccdCaseMap = caseDetailsConverter.convertToMap(ccdCase);
            callbackParams = buildCallbackParams(ccdCaseMap);

            assertThatThrownBy(() -> handler.handle(callbackParams))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No payment found in claim with external id " + SampleClaim.EXTERNAL_ID);
        }

        @Test
        void shouldPropagateExceptionThrownByPaymentsService() {
            final Claim claim = SampleClaim.getDefaultWithoutResponse(SampleClaim.DEFENDANT_EMAIL).toBuilder()
                .claimData(SampleClaimData.submittedByClaimantBuilder()
                    .withPayment(Payment.builder()
                        .status(PaymentStatus.INITIATED)
                        .reference(PAY_REFERENCE)
                        .build())
                    .build())
                .build();
            final Map<String, Object> ccdCaseMap =
                caseDetailsConverter.convertToMap(SampleData.getCCDCitizenCase(getAmountBreakDown()).toBuilder()
                    .feeAmountInPennies("1000")
                    .paymentStatus(PaymentStatus.INITIATED.getStatus())
                    .build());
            callbackParams = buildCallbackParams(ccdCaseMap);

            when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
            doThrow(new RuntimeException("Should be propagated"))
                .when(paymentsService).retrievePayment(anyString(), eq(claim.getClaimData()));

            assertThatThrownBy(() -> handler.handle(callbackParams))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Should be propagated");
        }
    }

    @SuppressWarnings("unused") // used for distinguishing parameterized tests
    @Nested
    @DisplayName("Successful callbacks")
    class SuccessfulCallbacks {

        @BeforeEach
        void setUp(TestInfo testInfo) {
            final PaymentStatus paymentStatus = PaymentStatus.fromValue(testInfo.getDisplayName());
            final Claim claim = SampleClaim.getDefaultWithoutResponse(SampleClaim.DEFENDANT_EMAIL).toBuilder()
                .claimData(SampleClaimData.submittedByClaimantBuilder()
                    .withPayment(Payment.builder()
                        .status(paymentStatus)
                        .reference(PAY_REFERENCE)
                        .build())
                    .build())
                .build();
            final CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown()).toBuilder()
                .feeAmountInPennies("1000")
                .paymentStatus(paymentStatus.getStatus())
                .build();
            final Map<String, Object> ccdCaseMap = caseDetailsConverter.convertToMap(ccdCase);

            when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
            when(caseMapper.to(any(Claim.class))).thenReturn(ccdCase);

            when(paymentsService.retrievePayment(anyString(), any(ClaimData.class)))
                .thenReturn(Optional.of(Payment.builder()
                    .status(paymentStatus)
                    .reference(PAY_REFERENCE)
                    .build()));

            callbackParams = buildCallbackParams(ccdCaseMap);
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {
            "INITIATED", "PENDING"
        })
        void nonCancellablePaymentsShouldNotInvokePaymentsService(PaymentStatus paymentStatus) {
            handler.handle(callbackParams);
            verifyNoMoreInteractions(paymentsService);
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = PaymentStatus.class, mode = EnumSource.Mode.INCLUDE, names = {
            "INITIATED", "PENDING"
        })
        void cancellablePaymentsShouldInvokePaymentsService(PaymentStatus paymentStatus) {
            handler.handle(callbackParams);
            verify(paymentsService).cancelPayment(anyString(), eq(PAY_REFERENCE));
        }
    }

    @Test
    void shouldOnlyHandleAboutToSubmitCallbackType() {
        assertThat(handler.callbacks())
            .containsOnlyKeys(CallbackType.ABOUT_TO_SUBMIT);
    }

    @Test
    void shouldOnlyHandleCancelPaymentEvents() {
        assertThat(handler.handledEvents())
            .containsOnly(CANCEL_CLAIM_PAYMENT_CITIZEN);
    }

    @Test
    void shouldAcceptOnlyCitizenRoles() {
        assertThat(handler.getSupportedRoles())
            .containsOnly(Role.CITIZEN);
    }

    private static CallbackParams buildCallbackParams(Map<String, Object> ccdCaseMap) {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CANCEL_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .data(ccdCaseMap)
                .build())
            .build();

        return CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
