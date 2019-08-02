package uk.gov.hmcts.cmc.claimstore.tests.functional.solicitor;

import org.junit.After;
import org.junit.Before;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseSubmitClaimTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class SolicitorSubmitClaimTest extends BaseSubmitClaimTest {

    @Before
    public void before() {
        user = idamTestService.createSolicitor();
    }

    @After
    public void after() {
        idamTestService.deleteUser(user.getUserDetails().getEmail());
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedBySolicitorBuilder;
    }

    @Override
    protected void assertDocumentsCreated(Claim claim) {
        claim.getClaimDocumentCollection().ifPresent(claimDocumentCollection -> {
                List<ClaimDocument> claimDocuments = claimDocumentCollection.getClaimDocuments();

                assertThat(claimDocuments).hasSize(1);

                assertThat(claimDocuments.get(0).getDocumentType()).isEqualTo(ClaimDocumentType.SEALED_CLAIM);
            }
        );
    }
}
