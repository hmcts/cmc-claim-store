package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimDocumentCollectionMapperTest {

    @Test
    public void to() {

        CCDCase.CCDCaseBuilder builder = CCDCase.builder();
        ClaimDocumentCollection collection = new ClaimDocumentCollection();
        collection.addClaimDocument(ClaimDocument.builder().documentManagementUrl(URI.create("someurl"))
            .documentType(ClaimDocumentType.DEFENDANT_PIN_LETTER).build());
        collection.addClaimDocument(ClaimDocument.builder().documentManagementUrl(URI.create("someurl"))
            .documentType(ClaimDocumentType.CCJ_REQUEST).build());
        collection.addClaimDocument(ClaimDocument.builder().documentManagementUrl(URI.create("someurl"))
            .documentType(ClaimDocumentType.CLAIM_ISSUE_RECEIPT).build());
        collection.addClaimDocument(ClaimDocument.builder().documentManagementUrl(URI.create("someurl"))
            .documentType(ClaimDocumentType.SEALED_CLAIM).build());
        collection.addClaimDocument(ClaimDocument.builder().documentManagementUrl(URI.create("someurl"))
            .documentType(ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT).build());

        ClaimDocumentCollectionMapper mapper = new ClaimDocumentCollectionMapper(new ClaimDocumentMapper());

        mapper.to(collection, builder);

        CCDCase build = builder.build();
        List<CCDCollectionElement<CCDClaimDocument>> caseDocuments = build.getCaseDocuments();
        assertThat(caseDocuments.size()).isEqualTo(3);
    }
}
