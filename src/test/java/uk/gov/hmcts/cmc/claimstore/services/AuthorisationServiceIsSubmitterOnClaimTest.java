package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthorisationServiceIsSubmitterOnClaimTest {

    private static final String USER_ID = "123456789";

    private final AuthorisationService authorisationService = new AuthorisationService();

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

    @Test
    public void assertShouldThrowForbiddenActionExceptionIfUserIsNotSumibmitterOnTheClaim() {
        assertThrows(ForbiddenActionException.class, () -> {
            Claim claim = SampleClaim.builder()
                .withSubmitterId("777")
                .build();

            authorisationService.assertIsSubmitterOnClaim(claim, USER_ID);
        });
    }

}
