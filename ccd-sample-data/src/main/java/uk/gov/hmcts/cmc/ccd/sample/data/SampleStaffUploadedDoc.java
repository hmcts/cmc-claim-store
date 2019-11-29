package uk.gov.hmcts.cmc.ccd.sample.data;

import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;

import java.util.Collections;
import java.util.List;

class SampleStaffUploadedDoc {

    private SampleStaffUploadedDoc() {
        // Utility private constructor
    }

    static List<CCDCollectionElement<CCDClaimDocument>> staffUploadedDocs = Collections.singletonList(
        CCDCollectionElement.<CCDClaimDocument>builder()
            .id("2323-2342-34-2-342")
            .value(CCDClaimDocument.builder()
                .documentName("name")
                .documentLink(CCDDocument.builder()
                    .documentUrl("http://www.cnn.com")
                    .documentBinaryUrl("http://www.cnn.com")
                    .documentFileName("documentFileName").build()
                )
                .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT).build())
            .build());

}
