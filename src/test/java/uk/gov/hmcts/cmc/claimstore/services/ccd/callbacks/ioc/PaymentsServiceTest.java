package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeOutcome;
import uk.gov.hmcts.reform.payments.client.PaymentRequest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.Fee;

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
    private static final String SERVICE = "service";
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

    private CCDCase ccdCase;
    private BigDecimal totalAmount = BigDecimal.valueOf(12L);

    @Before
    public void setUp() {
        paymentsService = new PaymentsService(
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
    }

    @Test
    public void shouldMakePayment() {
        FeeOutcome feeOutcome = FeeOutcome.builder()
            .feeAmount(BigDecimal.TEN)
            .build();

        when(feesClient.lookupFee("online", "issue", totalAmount))
            .thenReturn(feeOutcome);

        paymentsService.makePayment(
            BEARER_TOKEN,
            ccdCase,
            totalAmount
        );

        Fee[] fees = new Fee[] {
            Fee.builder()
                .ccdCaseNumber(ccdCase.getId().toString())
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(feeOutcome.getVersion())
                .build()
        };

        PaymentRequest expectedPaymentRequest =
            PaymentRequest.builder()
                .siteId(SITE_ID)
                .description(DESCRIPTION)
                .currency(CURRENCY)
                .service(SERVICE)
                .fees(fees)
                .amount(BigDecimal.valueOf(22L))
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
        when(feesClient.lookupFee("online", "issue", totalAmount))
            .thenThrow(IllegalStateException.class);

        paymentsService.makePayment(
            BEARER_TOKEN,
            ccdCase,
            totalAmount
        );
    }

    @Test(expected = IllegalStateException.class)
    public void shouldBubbleUpExceptionIfPaymentCreationFails() {
        FeeOutcome feeOutcome = FeeOutcome.builder()
            .feeAmount(BigDecimal.TEN)
            .build();

        when(feesClient.lookupFee("online", "issue", totalAmount))
            .thenReturn(feeOutcome);
        when(paymentsClient.createPayment(
            eq(BEARER_TOKEN),
            any(PaymentRequest.class),
            anyString()))
            .thenThrow(IllegalStateException.class);

        paymentsService.makePayment(
            BEARER_TOKEN,
            ccdCase,
            totalAmount
        );
    }
}
