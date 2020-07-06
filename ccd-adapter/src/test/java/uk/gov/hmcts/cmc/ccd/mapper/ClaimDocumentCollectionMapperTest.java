package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.springframework.util.Assert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ScannedDocument;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype;
import uk.gov.hmcts.cmc.domain.models.ScannedDocumentType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype.OCON9X;

public class ClaimDocumentCollectionMapperTest {

    private final ClaimDocumentCollectionMapper mapper = new ClaimDocumentCollectionMapper(
        new ClaimDocumentMapper(),
        new ScannedDocumentMapper());

    @Test
    public void shouldFilterOutPinDocuments() {

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
            .documentType(ClaimDocumentType.DEFENDANT_PIN_LETTER)
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

        mapper.to(collection, builder);

        CCDCase build = builder.build();
        List<CCDCollectionElement<CCDClaimDocument>> caseDocuments = build.getCaseDocuments();
        assertThat(caseDocuments.size()).isEqualTo(4);
    }

    @Test
    public void shouldMapToStaffUploadedDocuments() {
        CCDCase.CCDCaseBuilder builder = CCDCase.builder();
        ClaimDocumentCollection collection = new ClaimDocumentCollection();

        collection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.CCJ_REQUEST)
            .build());

        collection.addStaffUploadedDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.CCJ_REQUEST)
            .build());
        mapper.to(collection, builder);

        CCDCase build = builder.build();
        List<CCDCollectionElement<CCDClaimDocument>> caseDocuments = build.getStaffUploadedDocuments();
        assertThat(caseDocuments.size()).isEqualTo(1);

    }

    @Test
    public void shouldMapToScannedDocuments() {
        CCDCase.CCDCaseBuilder builder = CCDCase.builder();
        ClaimDocumentCollection collection = new ClaimDocumentCollection();

        collection.addClaimDocument(ClaimDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ClaimDocumentType.CCJ_REQUEST)
            .build());

        collection.addScannedDocument(ScannedDocument.builder()
            .documentManagementUrl(URI.create("someurl"))
            .documentManagementBinaryUrl(URI.create("someBinaryUrl"))
            .documentType(ScannedDocumentType.CHERISHED)
            .subtype("subtype")
            .build());

        mapper.to(collection, builder);

        CCDCase build = builder.build();
        List<CCDCollectionElement<CCDScannedDocument>> caseDocuments = build.getScannedDocuments();
        assertThat(caseDocuments.size()).isEqualTo(1);
    }

    @Test
    public void shouldMapFromScannedDocuments() {
        CCDCase ccdCase = SampleData.withPaperResponseFromScannedDoc();
        Claim.ClaimBuilder builder = Claim.builder();

        mapper.from(ccdCase, builder);

        Claim build = builder.build();
        ClaimDocumentCollection caseDocuments = build.getClaimDocumentCollection()
            .orElseThrow(() -> new IllegalStateException("Missing claim document collection"));
        Assert.notNull(caseDocuments.getScannedDocuments(), "Staff Uploaded document list cant be null");
        assertThat(caseDocuments.getScannedDocuments().size()).isEqualTo(1);

    }

    @Test
    public void shouldMapFromStaffUploadedDocuments() {
        CCDCase ccdCase = SampleData.withStaffUploadedDoc(PAPER_RESPONSE_DISPUTES_ALL);
        Claim.ClaimBuilder builder = Claim.builder();

        mapper.from(ccdCase, builder);

        Claim build = builder.build();
        ClaimDocumentCollection caseDocuments = build.getClaimDocumentCollection()
            .orElseThrow(() -> new IllegalStateException("Missing claim document collection"));
        Assert.notNull(caseDocuments.getStaffUploadedDocuments(), "Scanned document list cant be null");
        assertThat(caseDocuments.getStaffUploadedDocuments().size()).isEqualTo(1);
    }

    @Test
    public void shouldGetLatestScannedDocument() {

        ClaimDocumentCollection claimDocumentCollection = new ClaimDocumentCollection();

        ScannedDocument earliestScannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype(ScannedDocumentSubtype.OCON9X.value)
            .deliveryDate(LocalDateTime.now().minusDays(1))
            .build();

        ScannedDocument latestScannedDocument = ScannedDocument.builder()
            .documentType(ScannedDocumentType.FORM)
            .subtype(ScannedDocumentSubtype.OCON9X.value)
            .deliveryDate(LocalDateTime.now())
            .build();

        claimDocumentCollection.addScannedDocument(earliestScannedDocument);
        claimDocumentCollection.addScannedDocument(latestScannedDocument);

        Claim claim = Claim.builder()
            .claimDocumentCollection(claimDocumentCollection)
            .build();

        ScannedDocument scannedDocument = claim.getScannedDocument(
            ScannedDocumentType.FORM, OCON9X)
            .orElseThrow(() -> new IllegalStateException("Missing document"));

        assertThat(scannedDocument).isEqualTo(latestScannedDocument);
    }
}
