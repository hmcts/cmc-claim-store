package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import java.util.ArrayList;
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
        .printRequestType(PIN_LETTER_TO_DEFENDANT).printRequestId("requestId").build();

    @Before
    public void setUp() {
        legalOrderService = new LegalOrderService(
            documentTemplates,
            legalOrderCoverSheetContentProvider,
            securedDocumentManagementService,
            bulkPrintHandler
        );
        claim = SampleClaim.builder().build();
        when(documentTemplates.getLegalOrderCoverSheet()).thenReturn("coverSheet".getBytes());
        when(legalOrderCoverSheetContentProvider.createContentForClaimant(claim))
            .thenReturn(ImmutableMap.of("content", "CLAIMANT"));
        when(legalOrderCoverSheetContentProvider.createContentForDefendant(claim))
            .thenReturn(ImmutableMap.of("content", "DEFENDANT"));
        when(securedDocumentManagementService.downloadDocument(
            eq(BEARER_TOKEN),
            any(ClaimDocument.class))).thenReturn("legalOrder".getBytes());

    }

    @Test
    public void shouldSendPrint() {

        List<String> userList = List.of("John Rambo");


        Document legalOrder = new Document(
            Base64.getEncoder().encodeToString("legalOrder".getBytes()),
            Collections.emptyMap());
        Document coverSheetForClaimant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "CLAIMANT"));


//        Document coverSheetForDefendant = new Document(
//            "coverSheet",
//            ImmutableMap.of("content", "DEFENDANT"));

        given(bulkPrintHandler
            .printDirectionOrder(eq(claim), eq(coverSheetForClaimant), eq(legalOrder), eq(BEARER_TOKEN), eq(userList)))
            .willReturn(bulkPrintDetails);

//        given(bulkPrintHandler
//            .printDirectionOrder(eq(claim), eq(coverSheetForDefendant), eq(legalOrder), eq(BEARER_TOKEN), eq(userList)))
//            .willReturn(bulkPrintDetails);

        legalOrderService.print(
            BEARER_TOKEN,
            claim,
            DOCUMENT
        );

        verify(bulkPrintHandler).printDirectionOrder(
            claim,
            coverSheetForClaimant,
            legalOrder,
            BEARER_TOKEN,
            userList);

//        verify(bulkPrintHandler).printDirectionOrder(
//            claim,
//            coverSheetForDefendant,
//            legalOrder,
//            BEARER_TOKEN,
//            userList);
    }


    @Test
    public void shouldSendPrintEventForOrderAndCoverSheetIfOrderIsInDocStore() {

        List<String> userList = List.of("John Rambo");
        when(securedDocumentManagementService.downloadDocument(
            eq(BEARER_TOKEN),
            any(ClaimDocument.class))).thenReturn("legalOrder".getBytes());

        Document legalOrder = new Document(
            Base64.getEncoder().encodeToString("legalOrder".getBytes()),
            Collections.emptyMap());
        Document coverSheetForClaimant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "CLAIMANT"));

        given(bulkPrintHandler
            .printDirectionOrder(eq(claim), eq(coverSheetForClaimant), eq(legalOrder), eq(BEARER_TOKEN), eq(userList)))
            .willReturn(bulkPrintDetails);

        Document coverSheetForDefendant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "DEFENDANT"));

        given(bulkPrintHandler
            .printDirectionOrder(eq(claim), eq(coverSheetForDefendant), eq(legalOrder), eq(BEARER_TOKEN), eq(userList)))
            .willReturn(bulkPrintDetails);

        legalOrderService.print(
            BEARER_TOKEN,
            claim,
            DOCUMENT
        );

        verify(bulkPrintHandler).printDirectionOrder(
            claim,
            coverSheetForClaimant,
            legalOrder,
            BEARER_TOKEN,
            userList);

        verify(bulkPrintHandler).printDirectionOrder(
            claim,
            coverSheetForDefendant,
            legalOrder,
            BEARER_TOKEN,
            userList);
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
