package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

public class ClaimIssuedStaffNotificationServiceTest extends MockSpringTest {

    private static final String DEFENDANT_PIN = "a334frf";
    private static final String CLAIMANT_EMAIL = "claimant@email-domain.com";
    private static final String AUTHORISATION = "Bearer: aaa";

    private Claim claim = SampleClaim.builder().withClaimData(SampleClaimData.submittedByClaimant()).build();

    @Autowired
    private ClaimIssuedStaffNotificationService service;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() {
        service.notifyStaffClaimIssued(null, DEFENDANT_PIN, CLAIMANT_EMAIL, AUTHORISATION);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantPin() {
        service.notifyStaffClaimIssued(claim, null, CLAIMANT_EMAIL, AUTHORISATION);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaimantEmail() {
        service.notifyStaffClaimIssued(claim, DEFENDANT_PIN, null, AUTHORISATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenGivenEmptyClaimantEmail() {
        service.notifyStaffClaimIssued(claim, DEFENDANT_PIN, "", AUTHORISATION);
    }

}
