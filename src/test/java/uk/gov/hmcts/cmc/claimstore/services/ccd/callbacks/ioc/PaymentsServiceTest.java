package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.InitiatePaymentCaseMapper;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.domain.models.ioc.InitiatePaymentRequest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInitiatePaymentRequest;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeOutcome;
import uk.gov.hmcts.reform.payments.client.CardPaymentRequest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;

import java.math.BigDecimal;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentsServiceTest {
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String FRONTEND_URL = "http://frontend.test";
    private static final String SERVICE = "CMC";
    private static final String SITE_ID = "siteId";
    private static final String CURRENCY = "currency";
    private static final String DESCRIPTION = "description";

    private PaymentsService paymentsService;

    @Mock
    private PaymentsClient paymentsClient;
    @Mock
    private FeesClient feesClient;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private InitiatePaymentCaseMapper initiatePaymentCaseMapper;

    private CCDCase ccdCase;
    private FeeOutcome feeOutcome = FeeOutcome.builder()
        .feeAmount(BigDecimal.TEN)
        .build();

    @Before
    public void setUp() {
        paymentsService = new PaymentsService(
            initiatePaymentCaseMapper,
            paymentsClient,
            feesClient,
            notificationsProperties,
            SERVICE,
            SITE_ID,
            CURRENCY,
            DESCRIPTION
        );
        when(notificationsProperties.getFrontendBaseUrl()).thenReturn(FRONTEND_URL);
        ccdCase = SampleData.getCCDCitizenCase(
            SampleData.getAmountBreakDown()
        );
        InitiatePaymentRequest initiatePaymentRequest =
            SampleInitiatePaymentRequest.builder()
                .build();
        when(initiatePaymentCaseMapper.from(ccdCase)).thenReturn(initiatePaymentRequest);
        when(feesClient.lookupFee(eq("online"), eq("issue"), any(BigDecimal.class)))
            .thenReturn(feeOutcome);
    }

    @Test
    public void shouldMakePayment() {
        paymentsService.createPayment(
            BEARER_TOKEN,
            ccdCase
        );

        FeeDto[] fees = new FeeDto[] {
            FeeDto.builder()
                .ccdCaseNumber(ccdCase.getId().toString())
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(feeOutcome.getVersion())
                .build()
        };

        CardPaymentRequest expectedPaymentRequest =
            CardPaymentRequest.builder()
                .siteId(SITE_ID)
                .description(DESCRIPTION)
                .currency(CURRENCY)
                .service(SERVICE)
                .fees(fees)
                .amount(new BigDecimal("51.90"))
                .ccdCaseNumber(ccdCase.getId().toString())
                .caseReference(ccdCase.getExternalId())
                .build();

        verify(paymentsClient).createPayment(
            BEARER_TOKEN,
            expectedPaymentRequest,
            format("%s/claim/pay/%s/receiver", FRONTEND_URL, ccdCase.getExternalId()));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldBubbleUpExceptionIfFeeLookupFails() {
        when(feesClient.lookupFee(eq("online"), eq("issue"), any(BigDecimal.class)))
            .thenThrow(IllegalStateException.class);

        paymentsService.createPayment(
            BEARER_TOKEN,
            ccdCase
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldBubbleUpExceptionIfPaymentCreationFails() {
        when(paymentsClient.createPayment(
            eq(BEARER_TOKEN),
            any(CardPaymentRequest.class),
            anyString()))
            .thenThrow(IllegalStateException.class);

        paymentsService.createPayment(
            BEARER_TOKEN,
            ccdCase
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfAmountIsNotCalculated() {
        InitiatePaymentRequest initiatePaymentRequest =
            InitiatePaymentRequest.builder().build();
        when(initiatePaymentCaseMapper.from(ccdCase)).thenReturn(initiatePaymentRequest);

        paymentsService.createPayment(
            BEARER_TOKEN,
            ccdCase
        );
    }
}
