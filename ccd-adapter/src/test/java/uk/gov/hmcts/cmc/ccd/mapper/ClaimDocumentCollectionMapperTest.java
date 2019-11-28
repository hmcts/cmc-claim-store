package uk.gov.hmcts.cmc.ccd.mapper;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimDocumentCollectionMapperTest {

    @Test
    public void shouldFilterOutPinAndCCJDocuments() {

        CCDCase.CCDCaseBuilder builder = CCDCase.builder();
        ClaimDocumentCollection collection = new ClaimDocumentCollection();

        collection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.CCJ_REQUEST)
            .build());

        collection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.CLAIM_ISSUE_RECEIPT)
            .build());

        collection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.SEALED_CLAIM)
            .build());

        collection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT)
            .build());

        ClaimDocumentCollectionMapper mapper = new ClaimDocumentCollectionMapper(new ClaimDocumentMapper());
        mapper.to(collection, builder);

        CCDCase build = builder.build();
        List<CCDCollectionElement<CCDClaimDocument>> caseDocuments = build.getCaseDocuments();
        assertThat(caseDocuments.size()).isEqualTo(3);
    }

    @Test
    public void shouldMapCombinedDocumentsAndStaffUploadedDocumentsFromCCD() {

        final List<CCDCollectionElement<CCDClaimDocument>> documents =
            ImmutableList.of(CCDClaimDocumentType.MEDIATION_AGREEMENT,
                CCDClaimDocumentType.CLAIM_ISSUE_RECEIPT)
                .stream()
                .map(t -> CCDCollectionElement.<CCDClaimDocument>builder()
                    .value(buildCCDClaimDocumentCcdClaimDocument(t)).build())
                .collect(Collectors.toList());

        final List<CCDCollectionElement<CCDClaimDocument>> staffDocuments =
            ImmutableList.of(CCDClaimDocumentType.MEDIATION_AGREEMENT,
                CCDClaimDocumentType.CLAIM_ISSUE_RECEIPT)
                .stream()
                .map(t -> CCDCollectionElement.<CCDClaimDocument>builder()
                    .value(buildCCDClaimDocumentCcdClaimDocument(t)).build())
                .collect(Collectors.toList());


        final CCDCase ccdCase = CCDCase.builder()
            .staffUploadedDocuments(documents)
            .caseDocuments(documents)
            .build();

        ClaimDocumentCollectionMapper mapper = new ClaimDocumentCollectionMapper(new ClaimDocumentMapper());
        final Claim.ClaimBuilder builder = Claim.builder();
        mapper.from(ccdCase, builder);

        final Claim claim = builder.build();

        assertThat(claim.getClaimDocumentCollection().isPresent());
        final List<ClaimDocument> claimDocuments = claim.getClaimDocumentCollection().get().getClaimDocuments();
        assertThat(claimDocuments.size()).isEqualTo(documents.size() + staffDocuments.size());
    }


    private CCDDocument buildCCDDocument() {
        return CCDDocument.builder()
            .documentUrl("someurl")
            .documentBinaryUrl("someBinaryUrl")
            .documentFileName("someFilename")
            .build();
    }

    private CCDClaimDocument buildCCDClaimDocumentCcdClaimDocument(CCDClaimDocumentType documentType) {
        return CCDClaimDocument.builder()
            .documentLink(buildCCDDocument())
            .documentType(documentType)
            .build();
    }
}
