package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.InitiatePaymentCaseMapper;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInitiatePaymentRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.payments.client.models.Payment;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INITIATE_CLAIM_PAYMENT_CITIZEN;

@RunWith(MockitoJUnitRunner.class)
public class InitiatePaymentCallbackHandlerTest {
    private static final String BEARER_TOKEN = "Bearer let me in";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private InitiatePaymentCaseMapper initiatePaymentCaseMapper;
    @Mock
    private MoneyMapper moneyMapper;
    @Mock
    private PaymentsService paymentsService;

    private CallbackRequest callbackRequest;

    private InitiatePaymentCallbackHandler handler;

    @Before
    public void setUp() {
        handler = new InitiatePaymentCallbackHandler(
            paymentsService,
            initiatePaymentCaseMapper,
            caseDetailsConverter,
            moneyMapper);
        callbackRequest = CallbackRequest
            .builder()
            .eventId(INITIATE_CLAIM_PAYMENT_CITIZEN.getValue())
            .caseDetails(CaseDetails.builder()
                .id(3L)
                .data(ImmutableMap.of("data", "existingData"))
                .build())
            .build();
    }

    @Test
    public void shouldCreatePaymentOnAboutToSubmitEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
        CCDCase ccdCase = SampleData.getCCDCitizenCase(SampleData.getAmountBreakDown());
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
            .thenReturn(ccdCase);
        InitiatePaymentRequest initiatePaymentRequest =
            SampleInitiatePaymentRequest.builder().build();
        when(initiatePaymentCaseMapper.from(ccdCase)).thenReturn(initiatePaymentRequest);

        Payment payment = Payment.builder()
            .amount(BigDecimal.TEN)
            .reference("reference")
            .status("status")
            .dateCreated("2019-10-10")
            .build();

        when(paymentsService.makePayment(
            eq(BEARER_TOKEN),
            eq(ccdCase),
            any(BigDecimal.class))).thenReturn(payment);

        when(moneyMapper.to(payment.getAmount())).thenReturn("amount");

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            handler
                .handle(callbackParams);

        assertThat(response.getData()).contains(
            entry("data", "existingData"),
            entry("id", 3L),
//            entry("paymentId", payment.getId()),
            entry("paymentAmount", "amount"),
            entry("paymentReference", payment.getReference()),
            entry("paymentStatus", payment.getStatus()),
            entry("paymentDateCreated", payment.getDateCreated())
//            entry("paymentNextUrl", payment.getNextUrl())
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfAmountIsNotCalculated() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();

        CCDCase ccdCase = SampleData.getCCDCitizenCase(SampleData.getAmountBreakDown());
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
            .thenReturn(ccdCase);
        InitiatePaymentRequest initiatePaymentRequest =
            InitiatePaymentRequest.builder().build();
        when(initiatePaymentCaseMapper.from(ccdCase)).thenReturn(initiatePaymentRequest);
        handler
                .handle(callbackParams);
    }
}
