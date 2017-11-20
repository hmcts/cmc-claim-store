package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.Optional;

public class ClaimIssuedStaffNotificationServiceTest extends MockSpringTest {

    private static final String DEFENDANT_PIN = "a334frf";
    private static final String AUTHORISATION = "Bearer: aaa";

    private Claim claim = SampleClaim.builder().withClaimData(SampleClaimData.submittedByClaimant()).build();

    @Autowired
    private ClaimIssuedStaffNotificationService service;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffClaimIssued(null, Optional.of(DEFENDANT_PIN), AUTHORISATION);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantPin() {
        service.notifyStaffClaimIssued(claim, Optional.empty(), AUTHORISATION);
    }
}
