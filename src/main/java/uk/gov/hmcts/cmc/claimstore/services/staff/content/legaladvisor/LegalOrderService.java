package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.COVER_SHEET;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;

@Service
@ConditionalOnProperty(prefix = "document_management", name = "url")
public class LegalOrderService {

    private final DocumentTemplates documentTemplates;
    private final LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider;
    private final PrintService legalOrderBulkPrintService;
    private final DocumentManagementService documentManagementService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public LegalOrderService(
        DocumentTemplates documentTemplates,
        PrintService legalOrderBulkPrintService,
        LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider,
        DocumentManagementService documentManagementService,
        CaseDetailsConverter caseDetailsConverter) {
        this.documentTemplates = documentTemplates;
        this.legalOrderBulkPrintService = legalOrderBulkPrintService;
        this.legalOrderCoverSheetContentProvider = legalOrderCoverSheetContentProvider;
        this.documentManagementService = documentManagementService;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    public void print(String authorisation, CaseDetails caseDetails, CCDDocument ccdLegalOrder) throws Exception {
        requireNonNull(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        Document legalOrder = downloadLegalOrder(authorisation, ccdLegalOrder);

        Document coverSheetForClaimant = new Document(
            new String(documentTemplates.getLegalOrderCoverSheet()),
            legalOrderCoverSheetContentProvider.createContentForClaimant(claim));

        legalOrderBulkPrintService.print(
            claim,
            ImmutableMap.of(
                COVER_SHEET, coverSheetForClaimant,
                ORDER_DIRECTIONS, legalOrder
            )
        );

        Document coverSheetForDefendant = new Document(
            new String(documentTemplates.getLegalOrderCoverSheet()),
            legalOrderCoverSheetContentProvider.createContentForDefendant(claim));

        legalOrderBulkPrintService.print(
            claim,
            ImmutableMap.of(
                COVER_SHEET, coverSheetForDefendant,
                ORDER_DIRECTIONS, legalOrder
            )
        );
    }

    private Document downloadLegalOrder(String authorisation, CCDDocument ccdLegalOrder) throws URISyntaxException {
        return new Document(
            new String(documentManagementService.downloadDocument(
                authorisation,
                new URI(ccdLegalOrder.getDocumentUrl()),
                ccdLegalOrder.getDocumentFileName())),
            Collections.emptyMap()
        );
    }
}
