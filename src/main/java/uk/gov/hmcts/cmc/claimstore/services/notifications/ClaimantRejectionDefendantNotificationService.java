package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.services.document.SecuredDocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

@Service
public class ClaimantRejectionDefendantNotificationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BulkPrintHandler bulkPrintHandler;
    private final SecuredDocumentManagementService securedDocumentManagementService;
    private static final String UNABLE_TO_DOWNLOAD_DOCUMENT_MESSAGE = "Unable to download document and consequently wont be able to print";

    @Autowired
    public ClaimantRejectionDefendantNotificationService(BulkPrintHandler bulkPrintHandler,
                                                         SecuredDocumentManagementService securedDocumentManagementService
    ) {
        this.bulkPrintHandler = bulkPrintHandler;
        this.securedDocumentManagementService = securedDocumentManagementService;
    }

    public BulkPrintDetails printClaimantMediationRejection(Claim claim, CCDDocument ccdDocument, String authorisation) {
        requireNonNull(claim);
        Document document = downloadClaimantRejectionDocument(authorisation, ccdDocument);
        return bulkPrintHandler
            .printClaimantMediationRefusedLetter(
                claim,
                authorisation,
                document
            );
    }

    private Document downloadClaimantRejectionDocument(String authorisation, CCDDocument ccdDocument) {
        Document document = null;
        try {
            document = new Document(Base64.getEncoder().encodeToString(
                    securedDocumentManagementService.downloadDocument(
                        authorisation,
                        ClaimDocument.builder()
                            .documentName(ccdDocument.getDocumentFileName())
                            .documentType(ClaimDocumentType.CLAIMANT_MEDIATION_REFUSED)
                            .documentManagementUrl(new URI(ccdDocument.getDocumentUrl()))
                            .build())),
                Collections.emptyMap());
        } catch (URISyntaxException uriEx) {
            logger.error(UNABLE_TO_DOWNLOAD_DOCUMENT_MESSAGE);
        }
        return document;
    }
}
