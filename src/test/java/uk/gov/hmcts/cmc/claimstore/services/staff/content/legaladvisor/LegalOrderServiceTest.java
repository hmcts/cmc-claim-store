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
import uk.gov.hmcts.cmc.claimstore.events.legaladvisor.DirectionsOrderReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegalOrderServiceTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_URL)
        .build();

    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private BulkPrintHandler bulkPrintHandler;
    @Mock
    private LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider;

    private LegalOrderService legalOrderService;

    private Claim claim;

    @Before
    public void setUp() {
        legalOrderService = new LegalOrderService(
            documentTemplates,
            legalOrderCoverSheetContentProvider,
            documentManagementService,
            bulkPrintHandler
        );
        claim = SampleClaim.builder().build();
        when(documentTemplates.getLegalOrderCoverSheet()).thenReturn("coverSheet".getBytes());
        when(legalOrderCoverSheetContentProvider.createContentForClaimant(claim))
            .thenReturn(ImmutableMap.of("content", "CLAIMANT"));
        when(legalOrderCoverSheetContentProvider.createContentForDefendant(claim))
            .thenReturn(ImmutableMap.of("content", "DEFENDANT"));
    }

    @Test
    public void shouldSendPrintEventForOrderAndCoverSheetIfOrderIsInDocStore() {
        when(documentManagementService.downloadDocument(
            eq(BEARER_TOKEN),
            any(ClaimDocument.class))).thenReturn("legalOrder".getBytes());
        legalOrderService.print(
            BEARER_TOKEN,
            claim,
            DOCUMENT
        );

        Document legalOrder = new Document(
            Base64.getEncoder().encodeToString("legalOrder".getBytes()),
            Collections.emptyMap());
        Document coverSheetForClaimant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "CLAIMANT"));

        verify(bulkPrintHandler).print(
            new DirectionsOrderReadyToPrintEvent(
                claim,
                coverSheetForClaimant,
                legalOrder,
                BEARER_TOKEN));

        Document coverSheetForDefendant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "DEFENDANT"));
        verify(bulkPrintHandler).print(
            new DirectionsOrderReadyToPrintEvent(
                claim,
                coverSheetForDefendant,
                legalOrder,
                BEARER_TOKEN));
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionIfDocumentUrlIsWrong() {
        when(documentManagementService.downloadDocument(
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
