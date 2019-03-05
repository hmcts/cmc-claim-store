package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.net.URI;

@Component
public class ClaimDocumentMapper {

    public CCDCollectionElement<CCDClaimDocument> to(ClaimDocument claimDocument) {
        CCDClaimDocument.CCDClaimDocumentBuilder builder =  CCDClaimDocument.builder();

        builder.documentName(claimDocument.getDocumentName())
            .documentLink(new CCDDocument(claimDocument.getDocumentManagementUrl().toString()))
            .documentType("CLAIM_ISSUE_RECEIPT") // TODO Need to somehow pass or map the event
            .authoredDatetime(claimDocument.getAuthoredDatetime())
            .createdDatetime(claimDocument.getCreatedDatetime())
            .createdBy(claimDocument.getCreatedBy()) // TODO Should this be "ocmc"?
            .build();

        return CCDCollectionElement.<CCDClaimDocument>builder()
            .value(builder.build())
            .id(claimDocument.getDocumentName())
            .build();
    }

    public ClaimDocument from(CCDCollectionElement<CCDClaimDocument> collectionElement) {

        if (collectionElement == null || collectionElement.getValue() == null) {
            return null;
        }

        CCDClaimDocument ccdClaimDocument = collectionElement.getValue();

        return ClaimDocument.builder()
            .documentName(ccdClaimDocument.getDocumentName())
            .documentManagementUrl(URI.create(ccdClaimDocument.getDocumentLink().getDocumentUrl()))
            .documentType(ClaimDocumentType.CLAIM_ISSUE_RECEIPT)
            .authoredDatetime(ccdClaimDocument.getAuthoredDatetime())
            .createdDatetime(ccdClaimDocument.getCreatedDatetime())
            .createdBy(ccdClaimDocument.getCreatedBy())
            .build();
    }
}
