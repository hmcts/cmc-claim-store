package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_CASE_IDENTIFIER_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_LETTER_TYPE_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_LETTER_TYPE_VALUE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.XEROX_TYPE_PARAMETER;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintServiceTest {
    private static final String AUTH_VALUE = "AuthValue";
    private static final Claim CLAIM = SampleClaim.getDefault();
    private static final Map<String, Object> additionalData = new HashMap<>();
    private static final Map<String, Object> pinContents = new HashMap<>();
    private static final Document defendantLetterDocument = new Document("pinTemplate", pinContents);
    private static final Map<String, Object> claimContents = new HashMap<>();
    private static final Document sealedClaimDocument = new Document("sealedClaimTemplate", claimContents);

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AppInsights appInsights;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private BulkPrintService bulkPrintService;
    @Mock
    private BulkPrintStaffNotificationService bulkPrintStaffNotificationService;
    private Letter letter;

    @Before
    public void beforeEachTest() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_VALUE);
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, ADDITIONAL_DATA_LETTER_TYPE_VALUE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, CLAIM.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, CLAIM.getReferenceNumber());

        List<Document> documents = Arrays.asList(defendantLetterDocument, sealedClaimDocument);
        letter = new Letter(documents, XEROX_TYPE_PARAMETER, additionalData);
    }

    @Test
    public void shouldSendLetterWithDocumentsAsInGivenOrder() {
        //given
        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            false
        );

        //when
        bulkPrintService.print(CLAIM, defendantLetterDocument, sealedClaimDocument);
        //then
        List<Document> documents = Arrays.asList(defendantLetterDocument, sealedClaimDocument);

        verify(sendLetterApi).sendLetter(eq(AUTH_VALUE),
            eq(new Letter(documents, XEROX_TYPE_PARAMETER, additionalData)));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotifyStaffOnPrintFailure() {
        //given
        doThrow(new RuntimeException("send Letter failed"))
            .when(sendLetterApi)
            .sendLetter(eq(AUTH_VALUE), eq(letter));

        //when
        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            false
        );

        try {
            bulkPrintService.print(CLAIM, defendantLetterDocument, sealedClaimDocument);
        } finally {
            //then
            verify(sendLetterApi).sendLetter(eq(AUTH_VALUE), eq(letter));
        }
    }

    @Test(expected = RuntimeException.class)
    public void recoveryThrowWhenAsyncEnabled() {
        //given
        RuntimeException exception = new RuntimeException("send Letter failed");

        //when
        bulkPrintService = new BulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            bulkPrintStaffNotificationService,
            appInsights,
            true
        );

        try {
            bulkPrintService
                .notifyStaffForBulkPrintFailure(exception, CLAIM, defendantLetterDocument, sealedClaimDocument);
        } finally {
            //then
            verify(bulkPrintStaffNotificationService).notifyFailedBulkPrint(
                eq(defendantLetterDocument),
                eq(sealedClaimDocument),
                eq(CLAIM)
            );

            verify(appInsights).trackEvent(eq(BULK_PRINT_FAILED), eq(REFERENCE_NUMBER), eq(CLAIM.getReferenceNumber()));
        }
    }
}
