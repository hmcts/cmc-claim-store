package uk.gov.hmcts.cmc.claimstore.tests.functional.solicitor;

import org.junit.Before;
import uk.gov.hmcts.cmc.claimstore.tests.functional.BaseClaimPdfTest;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.models.Address;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class SolicitorClaimPdfTest extends BaseClaimPdfTest {

    @Before
    public void before() {
        user = idamTestService.createSolicitor();
    }

    @Override
    protected void assertionsOnClaimPdf(Claim createdCase, String pdfAsText) {
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

    private static String getFullAddressString(Address address) {
        return address.getLine1() + " \n"
            + address.getLine2() + " \n"
            // line 3 is not used
            + address.getCity() + " \n"
            + address.getPostcode();
    }

}
