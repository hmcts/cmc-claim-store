package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimAuthorisationRuleTest {

    private static final String AUTHORISATION_TOKEN = "1";
    private static final String MATCHING_USER_ID = "100";
    private static final String FAILING_USER_ID = "101";
    private static final UserDetails sampleDetails =
        SampleUserDetails.builder().withUserId(MATCHING_USER_ID).build();

    @Mock
    private UserService userService;

    private ClaimAuthorisationRule claimAuthorisationRule;

    @Before
    public void setUp() {
        when(userService.getUserDetails(AUTHORISATION_TOKEN)).thenReturn(sampleDetails);
        claimAuthorisationRule = new ClaimAuthorisationRule(userService);
    }

    @Test
    public void shouldNotThrowExceptionWhenSubmitterIdMatchesAuthorisation() {
        Claim claim = SampleClaim.builder().withSubmitterId(MATCHING_USER_ID).withDefendantId(FAILING_USER_ID).build();
        assertThatCode(() -> claimAuthorisationRule.assertClaimCanBeAccessed(claim, AUTHORISATION_TOKEN))
            .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionWhenDefendantIdMatchesAuthorisation() {
        Claim claim = SampleClaim.builder().withSubmitterId(FAILING_USER_ID).withDefendantId(MATCHING_USER_ID).build();
        assertThatCode(() -> claimAuthorisationRule.assertClaimCanBeAccessed(claim, AUTHORISATION_TOKEN))
            .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionWhenLetterHolderIdMatchesAuthorisation() {
        Claim claim = SampleClaim.builder()
            .withSubmitterId(FAILING_USER_ID)
            .withDefendantId(FAILING_USER_ID)
            .withLetterHolderId(MATCHING_USER_ID).build();
        assertThatCode(() -> claimAuthorisationRule.assertClaimCanBeAccessed(claim, AUTHORISATION_TOKEN))
            .doesNotThrowAnyException();
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowForbiddenActionExceptionWhenAuthorisationMatchesNoId() {
        Claim claim = SampleClaim.builder()
            .withSubmitterId(FAILING_USER_ID)
            .withDefendantId(FAILING_USER_ID)
            .withLetterHolderId(FAILING_USER_ID)
            .build();
        claimAuthorisationRule.assertClaimCanBeAccessed(claim, AUTHORISATION_TOKEN);
    }

    @Test
    public void shouldNotThrowExceptionWhenUserIdMatchesAuthorisation() {
        assertThatCode(() -> claimAuthorisationRule.assertUserIdMatchesAuthorisation(MATCHING_USER_ID,
            AUTHORISATION_TOKEN)).doesNotThrowAnyException();
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowForbiddenActionExceptionWhenUserIdDoesNotMatchAuthorisation() {
        claimAuthorisationRule.assertUserIdMatchesAuthorisation(FAILING_USER_ID, AUTHORISATION_TOKEN);
    }

    @Test
    public void shouldNotThrowExceptionWhenCaseworkerAccessesClaim() {
        UserDetails details = SampleUserDetails.builder().withRoles("caseworker-cmc").build();
        when(userService.getUserDetails("2")).thenReturn(details);
        assertThatCode(() -> claimAuthorisationRule.assertUserIdMatchesAuthorisation(FAILING_USER_ID, "2"))
            .doesNotThrowAnyException();
    }

}
