package uk.gov.hmcts.cmc.claimstore.tests.functional.solicitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BasePdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class SolicitorPdfTest extends BasePdfTest {

    @Before
    public void before() {
        user = idamTestService.createSolicitor();
    }

    @After
    public void after() {
        idamTestService.deleteUser(user.getUserDetails().getEmail());
    }

    @Test
    @Ignore
    public void shouldBeAbleToFindTestClaimDataInSolicitorSealedClaimPdf() throws IOException {
        shouldBeAbleToFindTestClaimDataInPdf("legalSealedClaim", createCase());
    }

    @Override
    protected void assertionsOnPdf(Claim createdCase, String pdfAsText) {
        ClaimData claimData = createdCase.getClaimData();
        Party claimant = claimData.getClaimant();
        assertThat(pdfAsText).contains("Claim number: " + createdCase.getReferenceNumber());
        assertThat(pdfAsText).contains("Fee account: " + claimData.getFeeAccountNumber().get());
        assertThat(pdfAsText).contains("Claim issued: " + Formatting.formatDate(createdCase.getIssuedOn()));
        assertThat(pdfAsText).contains("Claimant " + claimant.getName() + " \n"
            + getFullAddressString(claimant.getAddress()));
        assertThat(pdfAsText).contains("Service address " + claimData.getDefendant().getName() + " \n"
            + getFullAddressString(claimant.getCorrespondenceAddress().get()));
        assertThat(pdfAsText).contains("The claimant expects to recover up to "
            + Formatting.formatMoney(((AmountRange) claimData.getAmount()).getHigherValue()));
    }

    @Override
    protected Supplier<SampleClaimData> getSampleClaimDataBuilder() {
        return testData::submittedBySolicitorBuilder;
    }

    @Override
    protected Claim createCase() {
        ClaimData claimData = getSampleClaimDataBuilder().get().build();
        return commonOperations.saveClaim(claimData, user.getAuthorisation(), user.getUserDetails().getId())
            .then().extract().body().as(Claim.class);
    }

}
