package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

@Component
public class ClaimDocumentMapper {

    public CCDCollectionElement<CCDClaimDocument> to(ClaimDocument claimDocument) {
        CCDClaimDocument.CCDClaimDocumentBuilder builder =  CCDClaimDocument.builder();

        builder.documentName(claimDocument.getDocumentName())
            .documentLink(claimDocument.getDocumentManagementUrl())
            .documentType(CCDClaimDocumentType.CLAIM_ISSUE_RECEIPT) // TODO Need to somehow pass or map the event
            .authoredDate(claimDocument.getAuthoredDatetime())
            .createdDatetime(claimDocument.getCreatedDatetime())
            .createdBy(claimDocument.getCreatedBy()) // TODO Should this be "ocmc"?
            .build();

        return CCDCollectionElement.<CCDClaimDocument>builder().value(builder.build()).id(claimDocument.getDocumentName()).build();

    }

    public ClaimDocument from(CCDCollectionElement<CCDClaimDocument> collectionElement) {

        if (collectionElement == null || collectionElement.getValue() == null) {
            return null;
        }

        CCDClaimDocument ccdClaimDocument = collectionElement.getValue();

        return ClaimDocument.builder()
            .documentName(ccdClaimDocument.getDocumentName())
            .documentManagementUrl(ccdClaimDocument.getDocumentLink())
            .documentType(ClaimDocumentType.valueOf(ccdClaimDocument.getDocumentType().toString()))
            .authoredDatetime(ccdClaimDocument.getAuthoredDate())
            .createdDatetime(ccdClaimDocument.getCreatedDatetime())
            .createdBy(ccdClaimDocument.getCreatedBy())
            .build();
    }
}
