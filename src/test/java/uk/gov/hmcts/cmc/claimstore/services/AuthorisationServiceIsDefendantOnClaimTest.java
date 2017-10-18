package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthorisationServiceIsDefendantOnClaimTest {

    private static final Long USER_ID = 123456789L;

    private AuthorisationService authorisationService = new AuthorisationService();

    @Test
    public void shouldReturnTrueIfUserIsDefendantOnClaim() {
        Claim claim = SampleClaim.builder()
            .withDefendantId(USER_ID)
            .build();

        assertThat(authorisationService.isDefendantOnClaim(claim, USER_ID)).isTrue();
    }

    @Test
    public void shouldReturnFalseIfUserIsNotDefendantOnClaim() {
        Claim claim = SampleClaim.builder()
            .withDefendantId(777L)
            .build();

        assertThat(authorisationService.isDefendantOnClaim(claim, USER_ID)).isFalse();
    }

    @Test(expected = ForbiddenActionException.class)
    public void assertShouldThrowForbiddenActionExceptionIfUserIsNotDefendantOnClaim() {
        Claim claim = SampleClaim.builder()
            .withDefendantId(777L)
            .build();

        authorisationService.assertIsDefendantOnClaim(claim, USER_ID);
    }

}
