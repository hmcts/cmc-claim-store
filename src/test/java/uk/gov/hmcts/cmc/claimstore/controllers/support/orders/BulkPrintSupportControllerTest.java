package uk.gov.hmcts.cmc.claimstore.controllers.support.orders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.net.URI;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.LEGAL_ADVISOR_ORDER_PDF;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintSupportControllerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CLAIM_REFERENCE = "000CM001";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();
    private static final User USER = new User(AUTHORISATION, USER_DETAILS);
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");

    @Mock
    private ClaimService claimService;
    @Mock
    private UserService userService;
    @Mock
    private LegalOrderService legalOrderService;
    @Mock
    private CaseMapper caseMapper;
    private Claim sampleClaim;

    private BulkPrintSupportController bulkPrintSupportController;

    @Before
    public void before() {
        bulkPrintSupportController
            = new BulkPrintSupportController(legalOrderService, claimService, userService, caseMapper);

        sampleClaim = SampleClaim.builder()
            .withOrderDocument(DOCUMENT_URI)
            .build();

        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
    }

    @Test
    public void shouldSubmitRequestForBulkPrint() {
        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, LEGAL_ADVISOR_ORDER_PDF);
        CCDCase ccdCase = CCDCase.builder()
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(document)
                    .documentType(CCDClaimDocumentType.ORDER_DIRECTIONS)
                    .build())
                .build()))
            .build();

        when(caseMapper.to(eq(sampleClaim))).thenReturn(ccdCase);

        bulkPrintSupportController.resendLegalAdvisorOrderToPrint(CLAIM_REFERENCE);

        verify(legalOrderService).print(eq(AUTHORISATION), eq(sampleClaim), eq(document));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenDocumentIsNotPresentInCase() {
        CCDCase ccdCase = CCDCase.builder().build();
        when(caseMapper.to(eq(sampleClaim))).thenReturn(ccdCase);

        bulkPrintSupportController.resendLegalAdvisorOrderToPrint(CLAIM_REFERENCE);
    }
}
