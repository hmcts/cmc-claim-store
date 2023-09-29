package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.document.SecuredDocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimantRejectionDefendantNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantRejectionDefendantNotificationServiceTest {

    private static final String DOC_URL = "http://doc.url";
    private static final String DOC_NAME = "defendantLetter";
    private static final String AUTH_TOKEN = "auth-token";
    private static final String BEARER_TOKEN = "Bearer letmein";
    private static final String ID_SAMPLE = "123456789";

    private static final User CASEWORKER = new User(BEARER_TOKEN, SampleUserDetails.builder()
        .withRoles(Role.CASEWORKER.getRole()).build());

    private ClaimantRejectionDefendantNotificationService service;

    @Mock
    private BulkPrintHandler bulkPrintHandler;

    @Mock
    private SecuredDocumentManagementService securedDocumentManagementService;

    @Mock
    private UserService userService;

    @Before
    public void beforeEachTest() {

        BulkPrintDetails detail = BulkPrintDetails.builder().id(ID_SAMPLE).build();

        when(securedDocumentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .thenReturn(new byte[]{1, 2, 3, 4});
        when(bulkPrintHandler.printClaimantMediationRefusedLetter(any(Claim.class), anyString(), any(Document.class))).thenReturn(detail);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(CASEWORKER);

        service = new ClaimantRejectionDefendantNotificationService(bulkPrintHandler, securedDocumentManagementService, userService);
    }

    @Test
    public void shouldInvokePrintClaimantMeditationRefusedLetter() {

        Claim claim = SampleClaim.builder().build();
        CCDDocument doc =  CCDDocument.builder().documentUrl(DOC_URL).documentFileName(DOC_NAME).build();

        service.printClaimantMediationRejection(claim, doc);

        verify(bulkPrintHandler)
            .printClaimantMediationRefusedLetter(
                eq(claim),
                anyString(),
                any(Document.class)
            );
    }
}
