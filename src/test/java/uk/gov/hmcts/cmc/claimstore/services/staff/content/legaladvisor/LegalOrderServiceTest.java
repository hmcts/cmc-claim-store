package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.services.document.SecuredDocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType.PIN_LETTER_TO_DEFENDANT;

@RunWith(MockitoJUnitRunner.class)
public class LegalOrderServiceTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_URL)
        .build();
    private static final List<String> USER_LIST = List.of("John Rambo");

    @Mock
    private SecuredDocumentManagementService securedDocumentManagementService;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private BulkPrintHandler bulkPrintHandler;
    @Mock
    private LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider;

    private LegalOrderService legalOrderService;

    private Claim claim;

    private BulkPrintDetails bulkPrintDetails = BulkPrintDetails.builder()
        .printRequestType(PIN_LETTER_TO_DEFENDANT).printRequestId("requestId").printRequestedAt(LocalDate.now()).id("1").build();

    @Before
    public void setUp() {
        legalOrderService = new LegalOrderService(
            documentTemplates,
            legalOrderCoverSheetContentProvider,
            securedDocumentManagementService,
            bulkPrintHandler
        );
        claim = SampleClaim.builder().build();
//        when(documentTemplates.getLegalOrderCoverSheet()).thenReturn("coverSheet".getBytes());
//        when(legalOrderCoverSheetContentProvider.createContentForClaimant(claim))
//            .thenReturn(ImmutableMap.of("content", "CLAIMANT"));
//        when(legalOrderCoverSheetContentProvider.createContentForDefendant(claim))
//            .thenReturn(ImmutableMap.of("content", "DEFENDANT"));

    }

    @Test
    public void shouldSendPrintEventForOrderAndCoverSheetIfOrderIsInDocStore() {
        when(securedDocumentManagementService.downloadDocument(
            eq(BEARER_TOKEN),
            any(ClaimDocument.class))).thenReturn("legalOrder".getBytes());
        when(documentTemplates.getLegalOrderCoverSheet()).thenReturn("coverSheet".getBytes());

        Document legalOrder = new Document(
            Base64.getEncoder().encodeToString("legalOrder".getBytes()),
            Collections.emptyMap());
        Document coverSheetForClaimant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "CLAIMANT"));

        when(bulkPrintHandler.printDirectionOrder(
            any(Claim.class),
            eq(coverSheetForClaimant),
            any(),
            any(String.class),
            any(List.class)))
            .thenReturn(bulkPrintDetails);

        Document coverSheetForDefendant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "DEFENDANT"));

//        when(bulkPrintHandler.printDirectionOrder(
//            any(Claim.class),
//            any(Document.class),
//            any(Document.class),
//            any(String.class),
//            any(List.class)))
//            .thenReturn(bulkPrintDetails);

        legalOrderService.print(
            BEARER_TOKEN,
            claim,
            DOCUMENT
        );

//        verify(bulkPrintHandler).printDirectionOrder(
//            claim,
//            coverSheetForClaimant,
//            legalOrder,
//            BEARER_TOKEN,
//            USER_LIST);

        verify(bulkPrintHandler).printDirectionOrder(
            claim,
            coverSheetForClaimant,
            legalOrder,
            BEARER_TOKEN,
            USER_LIST);
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionIfDocumentUrlIsWrong() {
        when(securedDocumentManagementService.downloadDocument(
            BEARER_TOKEN,
            null)).thenThrow(new URISyntaxException("nope", "nope"));
        legalOrderService.print(
            BEARER_TOKEN,
            claim,
            DOCUMENT
        );
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionIfOrderIsNotInDocStore() {
        legalOrderService.print(
            BEARER_TOKEN,
            claim,
            DOCUMENT
        );
    }

}
