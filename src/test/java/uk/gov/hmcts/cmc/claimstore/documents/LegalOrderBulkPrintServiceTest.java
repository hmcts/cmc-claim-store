package uk.gov.hmcts.cmc.claimstore.documents;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.BULK_PRINT_FAILED;
import static uk.gov.hmcts.cmc.claimstore.documents.BulkPrintService.XEROX_TYPE_PARAMETER;

@RunWith(MockitoJUnitRunner.class)
public class LegalOrderBulkPrintServiceTest {
    private static final String AUTH_VALUE = "AuthValue";
    private static final Claim CLAIM = SampleClaim.getDefault();
    private static final Document LEGAL_ORDER = new Document("legalOrder", Collections.emptyMap());
    private static final Document COVER_SHEET = new Document("coverSheet", Collections.emptyMap());

    @Mock
    private SendLetterApi sendLetterApi;

    @Mock
    private AppInsights appInsights;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private LegalOrderBulkPrintService legalOrderBulkPrintService;

    @Before
    public void beforeEachTest() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_VALUE);
        legalOrderBulkPrintService = new LegalOrderBulkPrintService(
            sendLetterApi,
            authTokenGenerator,
            appInsights
        );
    }

    @Test
    public void shouldSendLetterWithDocumentsAsInGivenOrder() {
        //when
        legalOrderBulkPrintService.print(CLAIM,
            ImmutableMap.of(
                ClaimDocumentType.COVER_SHEET, COVER_SHEET,
                ClaimDocumentType.ORDER_DIRECTIONS, LEGAL_ORDER
            ));
        List<Document> documents = Arrays.asList(COVER_SHEET, LEGAL_ORDER);

        verify(sendLetterApi).sendLetter(
            eq(AUTH_VALUE),
            eq(new Letter(documents, XEROX_TYPE_PARAMETER, Collections.emptyMap())));
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotifyStaffOnPrintFailure() {
        RuntimeException exception = new RuntimeException("send Letter failed");

        legalOrderBulkPrintService.print(
            CLAIM,
            ImmutableMap.of(
                ClaimDocumentType.COVER_SHEET, COVER_SHEET,
                ClaimDocumentType.ORDER_DIRECTIONS, LEGAL_ORDER
            ));

        verify(legalOrderBulkPrintService).showErrorForBulkPrintFailure(
            eq(exception),
            eq(CLAIM),
            eq(ImmutableMap.of(
                ClaimDocumentType.COVER_SHEET, COVER_SHEET,
                ClaimDocumentType.ORDER_DIRECTIONS, LEGAL_ORDER
            ))
        );
        verify(appInsights).trackEvent(eq(BULK_PRINT_FAILED), eq(REFERENCE_NUMBER), eq(CLAIM.getReferenceNumber()));
    }
}
