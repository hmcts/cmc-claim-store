package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDDefendant;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.MediationOutcome;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleCCDClaimSubmissionOperationIndicators.CCDClaimSubmissionOperationIndicatorsWithPinSuccess;

public class MediationOutcomeMapperTest {

    @Test
    public void shouldMapMediationOutcomeSuccesFromCCDCase() {
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithRespondent(SampleCCDDefendant.withMediationAgreementDate().build());

        assertThat(MediationOutcomeMapper.from(ccdCase), is(MediationOutcome.SUCCEEDED));
    }

    @Test
    public void shouldMapMediationOutcomeFailureFromCCDCase() {
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithRespondent(SampleCCDDefendant.withMediationFailureReason().build());

        assertThat(MediationOutcomeMapper.from(ccdCase), is(MediationOutcome.FAILED));
    }

    @Test
    public void shouldMapMediationOutcomeAsNullFromCCDCase() {
        CCDCase ccdCase =
            SampleData.getCCDCitizenCaseWithOperationIndicators(CCDClaimSubmissionOperationIndicatorsWithPinSuccess);

        assertNull(MediationOutcomeMapper.from(ccdCase));
    }

}
