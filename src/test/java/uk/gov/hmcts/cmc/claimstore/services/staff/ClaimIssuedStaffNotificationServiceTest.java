package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmccase.models.Claim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmccase.models.sampledata.SampleClaimData;

import java.util.Optional;

public class ClaimIssuedStaffNotificationServiceTest extends MockSpringTest {

    private static final String DEFENDANT_PIN = "a334frf";
    private static final String CLAIMANT_EMAIL = "claimant@email-domain.com";

    private Claim claim = SampleClaim.builder().withClaimData(SampleClaimData.submittedByClaimant()).build();

    @Autowired
    private ClaimIssuedStaffNotificationService service;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffClaimIssued(null, Optional.of(DEFENDANT_PIN), CLAIMANT_EMAIL);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantPin() {
        service.notifyStaffClaimIssued(claim, Optional.empty(), CLAIMANT_EMAIL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenGivenEmptyDefendantPin() {
        service.notifyStaffClaimIssued(claim, Optional.of(""), CLAIMANT_EMAIL);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaimantEmail() {
        service.notifyStaffClaimIssued(claim, Optional.of(DEFENDANT_PIN), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenGivenEmptyClaimantEmail() {
        service.notifyStaffClaimIssued(claim, Optional.of(DEFENDANT_PIN), "");
    }

}
