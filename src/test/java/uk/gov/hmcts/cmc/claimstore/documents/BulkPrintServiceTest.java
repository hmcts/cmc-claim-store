package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.DocumentReadyToPrintEvent;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_CASE_IDENTIFIER_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_LETTER_TYPE_KEY;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.ADDITIONAL_DATA_LETTER_TYPE_VALUE;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.XEROX_TYPE_PARAMETER;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintServiceTest {
    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AppInsights appInsights;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private BulkPrintService bulkPrintService;
    private BulkPrintStaffNotificationService bulkPrintStaffNotificationService;

    @Before
    public void beforeEachTest() {
        bulkPrintService = new BulkPrintService(
            sendLetterApi, authTokenGenerator, bulkPrintStaffNotificationService, appInsights
        );
    }

    @Test
    public void shouldSendLetterWithDocumentsAsInGivenOrder() {
        //given
        String authValue = "AuthValue";
        when(authTokenGenerator.generate()).thenReturn(authValue);
        Map<String, Object> pinContents = new HashMap<>();
        Document defendantLetterDocument = new Document("pinTemplate", pinContents);
        Map<String, Object> claimContents = new HashMap<>();
        Document sealedClaimDocument = new Document("sealedClaimTemplate", claimContents);
        Claim claim = SampleClaim.getDefault();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(ADDITIONAL_DATA_LETTER_TYPE_KEY, ADDITIONAL_DATA_LETTER_TYPE_VALUE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, claim.getId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, claim.getReferenceNumber());

        DocumentReadyToPrintEvent event
            = new DocumentReadyToPrintEvent(claim, defendantLetterDocument, sealedClaimDocument);

        //when
        bulkPrintService.print(event);
        //then
        List<Document> documents = Arrays.asList(defendantLetterDocument, sealedClaimDocument);

        verify(sendLetterApi).sendLetter(eq(authValue),
            eq(new Letter(documents, XEROX_TYPE_PARAMETER, additionalData)));
    }
}
