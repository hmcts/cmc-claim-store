package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.tests.AATConfiguration;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepresentative;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static java.lang.String.format;

@Service
public class TestData {

    private final AATConfiguration aatConfiguration;

    @Autowired
    public TestData(AATConfiguration aatConfiguration) {
        this.aatConfiguration = aatConfiguration;
    }

    public SampleClaimData submittedByClaimantBuilder() {
        return SampleClaimData.submittedByClaimantBuilder()
            .withDefendant(
                SampleTheirDetails.builder()
                    .withEmail(nextUserEmail())
                    .withRepresentative(null)
                    .individualDetails()
            );
    }

    public SampleClaimData submittedBySolicitorBuilder() {
        return SampleClaimData.submittedByLegalRepresentativeBuilder()
            .withClaimant(SampleParty.builder()
                .withRepresentative(SampleRepresentative.builder().build())
                .party());
    }

    public String nextUserEmail() {
        return format(aatConfiguration.getGeneratedUserEmailPattern(), RandomStringUtils.randomAlphanumeric(10));
    }

}
