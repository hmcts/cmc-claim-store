package uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.events.legaladvisor.DirectionsOrderReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

@Service
public class LegalOrderService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DocumentTemplates documentTemplates;
    private final LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider;
    private final ApplicationEventPublisher publisher;
    private final DocumentManagementService documentManagementService;

    @Autowired
    public LegalOrderService(
        DocumentTemplates documentTemplates,
        LegalOrderCoverSheetContentProvider legalOrderCoverSheetContentProvider,
        DocumentManagementService documentManagementService,
        ApplicationEventPublisher publisher) {
        this.documentTemplates = documentTemplates;
        this.publisher = publisher;
        this.legalOrderCoverSheetContentProvider = legalOrderCoverSheetContentProvider;
        this.documentManagementService = documentManagementService;
    }

    public void print(String authorisation, Claim claim, CCDDocument ccdLegalOrder) {
        requireNonNull(claim);
        try {
            Document legalOrder = downloadLegalOrder(authorisation, ccdLegalOrder);
            Document coverSheetForClaimant = new Document(
                new String(documentTemplates.getLegalOrderCoverSheet()),
                legalOrderCoverSheetContentProvider.createContentForClaimant(claim));

            publisher.publishEvent(new DirectionsOrderReadyToPrintEvent(
                claim,
                coverSheetForClaimant,
                legalOrder,
                authorisation
            ));

            Document coverSheetForDefendant = new Document(
                new String(documentTemplates.getLegalOrderCoverSheet()),
                legalOrderCoverSheetContentProvider.createContentForDefendant(claim));

            publisher.publishEvent(new DirectionsOrderReadyToPrintEvent(
                claim,
                coverSheetForDefendant,
                legalOrder,
                authorisation
            ));
        } catch (URISyntaxException e) {
            logger.warn("Problem download legal advisor document from doc store, won't print");
        }
    }

    private Document downloadLegalOrder(String authorisation, CCDDocument ccdLegalOrder) throws URISyntaxException {
        return new Document(Base64.getEncoder().encodeToString(
            documentManagementService.downloadDocument(
                authorisation,
                ClaimDocument.builder()
                    .documentName(ccdLegalOrder.getDocumentFileName())
                    .documentType(ClaimDocumentType.ORDER_DIRECTIONS)
                    .documentManagementUrl(new URI(ccdLegalOrder.getDocumentUrl()))
                    .build())),
            Collections.emptyMap());
    }
}
