package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.net.URI;

@Component
public class ClaimDocumentMapper {

    public CCDCollectionElement<CCDClaimDocument> to(ClaimDocument claimDocument) {
        CCDClaimDocument.CCDClaimDocumentBuilder builder = CCDClaimDocument.builder();
        final String documentUrl = claimDocument.getDocumentManagementUrl().toString();
        final String documentBinaryUrl = claimDocument.getDocumentManagementBinaryUrl().toString();

        builder.documentName(claimDocument.getDocumentName())
            .documentLink(new CCDDocument(documentUrl, documentBinaryUrl, claimDocument.getDocumentName()))
            .documentType(CCDClaimDocumentType.valueOf(claimDocument.getDocumentType().name()))
            .authoredDatetime(claimDocument.getAuthoredDatetime())
            .createdDatetime(claimDocument.getCreatedDatetime())
            .createdBy(claimDocument.getCreatedBy())
            .size(claimDocument.getSize());

        return CCDCollectionElement.<CCDClaimDocument>builder()
            .value(builder.build())
            .id(claimDocument.getId())
            .build();
    }

    public ClaimDocument from(CCDCollectionElement<CCDClaimDocument> collectionElement) {

        if (collectionElement == null || collectionElement.getValue() == null) {
            return null;
        }

        CCDClaimDocument ccdClaimDocument = collectionElement.getValue();

        return ClaimDocument.builder()
            .id(collectionElement.getId())
            .documentName(ccdClaimDocument.getDocumentName())
            .documentManagementUrl(URI.create(ccdClaimDocument.getDocumentLink().getDocumentUrl()))
            .documentManagementBinaryUrl(URI.create(ccdClaimDocument.getDocumentLink().getDocumentBinaryUrl()))
            .documentType(ClaimDocumentType.valueOf(ccdClaimDocument.getDocumentType().name()))
            .authoredDatetime(ccdClaimDocument.getAuthoredDatetime())
            .createdDatetime(ccdClaimDocument.getCreatedDatetime())
            .createdBy(ccdClaimDocument.getCreatedBy())
            .size(ccdClaimDocument.getSize())
            .build();
    }
}
