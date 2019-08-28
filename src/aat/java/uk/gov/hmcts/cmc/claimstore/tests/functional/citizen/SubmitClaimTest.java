package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.After;
import org.junit.Before;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseSubmitClaimTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

public class SubmitClaimTest extends BaseSubmitClaimTest {

    @Before
    public void before() {
        user = bootstrap.getClaimant();
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertDocumentsCreated(Claim claim) {
        claim.getClaimDocumentCollection().ifPresent(claimDocumentCollection -> {
                List<ClaimDocument> claimDocuments = claimDocumentCollection.getClaimDocuments();
                assertThat(claimDocuments).hasSize(3);

                List<ClaimDocumentType> documentTypes = claimDocuments.stream()
                    .map(ClaimDocument::getDocumentType)
                    .collect(Collectors.toList());

                assertThat(documentTypes).contains(SEALED_CLAIM, CLAIM_ISSUE_RECEIPT, DEFENDANT_PIN_LETTER);
            }
        );
    }
}
