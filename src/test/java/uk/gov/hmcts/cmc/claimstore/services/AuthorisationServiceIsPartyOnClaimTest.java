package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorisationServiceIsPartyOnClaimTest {

    private static final Long USER_ID = 123456789L;

    private AuthorisationService authorisationService = new AuthorisationService();

    @Test
    public void shouldReturnTrueIfUserIsSubmitter() {
        Claim claim = SampleClaim.builder()
            .withSubmitterId(USER_ID)
            .build();

        assertThat(authorisationService.isPartyOnClaim(claim, USER_ID)).isTrue();
    }

    @Test
    public void shouldReturnTrueIfUserIsDefendant() {
        Claim claim = SampleClaim.builder()
            .withDefendantId(USER_ID)
            .build();

        assertThat(authorisationService.isPartyOnClaim(claim, USER_ID)).isTrue();
    }

    @Test
    public void shouldReturnFalseIfUserIsNeitherSubmitterNorDefendant() {
        assertThat(authorisationService.isPartyOnClaim(SampleClaim.getDefault(), USER_ID)).isFalse();
    }

    @Test(expected = ForbiddenActionException.class)
    public void assertIsPartyOnClaimShouldThrowForbiddenIfUserIsNotPartyOnClaim() {
        authorisationService.assertIsPartyOnClaim(SampleClaim.getDefault(), USER_ID);
    }

}
