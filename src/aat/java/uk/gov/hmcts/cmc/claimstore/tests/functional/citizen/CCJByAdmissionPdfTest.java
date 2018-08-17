package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseClaimPdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class CCJByAdmissionPdfTest extends BaseClaimPdfTest {

    @Before
    public void before() {
        user = idamTestService.createCitizen();
    }

    @Test
    public void shouldBeAbleToFindAppropriateDataInCCJByAdmissionPdf() throws IOException {
        shouldBeAbleToFindTestCCJDataInPdf("ccj");
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Date of order: " + Formatting.formatDate(createdCase
                                        .getCountyCourtJudgmentIssuedAt()
                                        .orElseThrow(IllegalArgumentException::new)));
        assertThat(pdfAsText).contains("Claimant Name: " + createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains("Defendant name: " + createdCase.getClaimData().getDefendant().getName());
    }
}
