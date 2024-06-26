package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BasePdfTest;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.Retry;
import uk.gov.hmcts.cmc.claimstore.tests.helpers.RetryFailedFunctionalTests;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimPdfTest extends BasePdfTest {

    @BeforeEach
    public void before() {
        user = bootstrap.getClaimant();
    }

    @Rule
    public RetryFailedFunctionalTests retryRule = new RetryFailedFunctionalTests(3);

    @Test
    @Retry
    public void shouldBeAbleToFindTestClaimDataInClaimIssueReceiptPdf() throws IOException {
        shouldBeAbleToFindTestClaimDataInPdf("claimIssueReceipt", createCase());
    }

    @Test
    @Retry
    public void shouldBeAbleToFindTestClaimDataInSealedClaimPdf() throws IOException {
        user = bootstrap.getSolicitor();
        Claim createdCase = createCase();
        String pdfAsText = textContentOf(retrievePdf("sealedClaim", createdCase.getExternalId()));
        assertionsOnPdf(createdCase, pdfAsText);
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedByClaimantBuilder;
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Issued on: "
            + createdCase.getIssuedOn().map(Formatting::formatDate).orElseThrow());
        assertThat(pdfAsText).contains("Name: " + createdCase.getClaimData().getClaimant().getName());
        assertThat(pdfAsText).contains("Address: "
            + getFullAddressString(createdCase.getClaimData().getClaimant().getAddress()));
        assertThat(pdfAsText).contains("Name: " + createdCase.getClaimData().getDefendant().getName());
        assertThat(pdfAsText).contains("Address: "
            + getFullAddressString(createdCase.getClaimData().getDefendant().getAddress()));
        assertThat(pdfAsText).contains("Claim amount: "
            + Formatting.formatMoney(((AmountBreakDown) createdCase.getClaimData().getAmount()).getTotalAmount()));
        assertThat(pdfAsText).contains(Formatting.formatDate(createdCase.getResponseDeadline()));
    }

}
