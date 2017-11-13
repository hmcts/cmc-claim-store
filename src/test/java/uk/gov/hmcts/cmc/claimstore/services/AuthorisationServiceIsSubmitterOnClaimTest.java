package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorisationServiceIsSubmitterOnClaimTest {

    private static final String USER_ID = "123456789";

    private AuthorisationService authorisationService = new AuthorisationService();

    @Test
    public void shouldReturnTrueIfUserIsSubmitterOnTheClaim() {
        Claim claim = SampleClaim.builder()
            .withSubmitterId(USER_ID)
            .build();

        assertThat(authorisationService.isSubmitterOnClaim(claim, USER_ID)).isTrue();
    }

    @Test
    public void shouldReturnFalseIfUserIsNotSubmitterOnTheClaim() {
        Claim claim = SampleClaim.builder()
            .withSubmitterId("777")
            .build();

        assertThat(authorisationService.isSubmitterOnClaim(claim, USER_ID)).isFalse();
    }

    @Test(expected = ForbiddenActionException.class)
    public void assertShouldThrowForbiddenActionExceptionIfUserIsNotSumibmitterOnTheClaim() {
        Claim claim = SampleClaim.builder()
            .withSubmitterId("777")
            .build();

        authorisationService.assertIsSubmitterOnClaim(claim, USER_ID);
    }

}
