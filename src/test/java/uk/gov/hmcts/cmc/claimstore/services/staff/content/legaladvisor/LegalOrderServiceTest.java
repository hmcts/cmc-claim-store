package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.LegalOrderBulkPrintService;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegalOrderServiceTest {

    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .build();

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private LegalOrderBulkPrintService legalOrderBulkPrintService;
    @Mock
    private LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider;

    private LegalOrderService legalOrderService;

    private Claim claim;

    @Before
    public void setUp() {
        legalOrderService = new LegalOrderService(
            documentTemplates,
            legalOrderBulkPrintService,
            legalOrderCoverSheetContentProvider,
            documentManagementService,
            caseDetailsConverter
        );
        claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(CaseDetails.builder().data(Collections.emptyMap()).build()))
            .thenReturn(claim);
        when(documentTemplates.getLegalOrderCoverSheet()).thenReturn("coverSheet".getBytes());
        when(legalOrderCoverSheetContentProvider.createContentForClaimant(claim))
            .thenReturn(ImmutableMap.of("content", "CLAIMANT"));
        when(legalOrderCoverSheetContentProvider.createContentForDefendant(claim))
            .thenReturn(ImmutableMap.of("content", "DEFENDANT"));
    }

    @Test
    public void shouldPrintOrderAndCoverSheetIfOrderIsInDocStore() throws Exception {
        when(documentManagementService.downloadDocument(
            BEARER_TOKEN,
            new URI(DOCUMENT_URL),
            null)).thenReturn("legalOrder".getBytes());
        legalOrderService.print(
            BEARER_TOKEN,
            CaseDetails.builder().data(Collections.emptyMap()).build(),
            DOCUMENT
        );

        Document legalOrder = new Document("legalOrder", Collections.emptyMap());
        Document coverSheetForClaimant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "CLAIMANT"));

        verify(legalOrderBulkPrintService).print(
            claim,
            ImmutableMap.of(
                ClaimDocumentType.COVER_SHEET, coverSheetForClaimant,
                ClaimDocumentType.ORDER_DIRECTIONS, legalOrder
            )
        );

        Document coverSheetForDefendant = new Document(
            "coverSheet",
            ImmutableMap.of("content", "DEFENDANT"));
        verify(legalOrderBulkPrintService).print(
            claim,
            ImmutableMap.of(
                ClaimDocumentType.COVER_SHEET, coverSheetForDefendant,
                ClaimDocumentType.ORDER_DIRECTIONS, legalOrder
            )
        );
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionIfDocumentUrlIsWrong() throws Exception {
        when(documentManagementService.downloadDocument(
            BEARER_TOKEN,
            new URI(DOCUMENT_URL),
            null)).thenThrow(new URISyntaxException("nope", "nope"));
        legalOrderService.print(
            BEARER_TOKEN,
            CaseDetails.builder().data(Collections.emptyMap()).build(),
            DOCUMENT
        );
    }

    @Test(expected = Exception.class)
    public void shouldThrowExceptionIfOrderIsNotInDocStore() throws Exception {
        when(documentManagementService.downloadDocument(
            BEARER_TOKEN,
            new URI(DOCUMENT_URL),
            null)).thenThrow(new DocumentManagementException("nope"));
        legalOrderService.print(
            BEARER_TOKEN,
            CaseDetails.builder().data(Collections.emptyMap()).build(),
            DOCUMENT
        );
    }

}
