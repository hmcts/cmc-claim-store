package uk.gov.hmcts.cmc.claimstore.services.staff;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentManagementException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import java.util.Optional;

public class ClaimIssuedStaffNotificationServiceTest extends MockSpringTest {

    private static final String DEFENDANT_PIN = "a334frf";
    private static final String CLAIMANT_EMAIL = "claimant@email-domain.com";
    private static final String AUTHORISATION = "Bearer: aaa";

    private Claim claim = SampleClaim.builder().withClaimData(SampleClaimData.submittedByClaimant()).build();

    @Autowired
    private ClaimIssuedStaffNotificationService service;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaim() throws DocumentManagementException {
        service.notifyStaffClaimIssued(null, Optional.of(DEFENDANT_PIN), CLAIMANT_EMAIL, AUTHORISATION);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullDefendantPin() throws DocumentManagementException {
        service.notifyStaffClaimIssued(claim, Optional.empty(), CLAIMANT_EMAIL, AUTHORISATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenGivenEmptyDefendantPin() throws DocumentManagementException {
        service.notifyStaffClaimIssued(claim, Optional.of(""), CLAIMANT_EMAIL, AUTHORISATION);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullClaimantEmail() throws DocumentManagementException {
        service.notifyStaffClaimIssued(claim, Optional.of(DEFENDANT_PIN), null, AUTHORISATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenGivenEmptyClaimantEmail() throws DocumentManagementException {
        service.notifyStaffClaimIssued(claim, Optional.of(DEFENDANT_PIN), "", AUTHORISATION);
    }

}
