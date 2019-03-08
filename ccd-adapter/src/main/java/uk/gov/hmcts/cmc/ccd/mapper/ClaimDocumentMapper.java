package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefenceType;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.net.URI;

@Component
public class ClaimDocumentMapper {

    public CCDCollectionElement<CCDClaimDocument> to(ClaimDocument claimDocument) {
        CCDClaimDocument.CCDClaimDocumentBuilder builder =  CCDClaimDocument.builder();

        builder.documentName(claimDocument.getDocumentName())
            .documentLink(new CCDDocument(claimDocument.getDocumentManagementUrl().toString()))
            .documentType(CCDClaimDocumentType.valueOf(claimDocument.getDocumentType().name())) // TODO Need to somehow pass or map the event
            .authoredDatetime(claimDocument.getAuthoredDatetime())
            .createdDatetime(claimDocument.getCreatedDatetime())
            .createdBy(claimDocument.getCreatedBy())
            .build();

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
            .documentType(ClaimDocumentType.valueOf(ccdClaimDocument.getDocumentType().name()))
            .authoredDatetime(ccdClaimDocument.getAuthoredDatetime())
            .createdDatetime(ccdClaimDocument.getCreatedDatetime())
            .createdBy(ccdClaimDocument.getCreatedBy())
            .build();
    }
}
