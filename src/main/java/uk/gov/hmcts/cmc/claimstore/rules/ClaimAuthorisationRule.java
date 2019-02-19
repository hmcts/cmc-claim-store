package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class ClaimAuthorisationRule {

    private static final String USER_NOT_LINKED_MESSAGE =
        "User with id %s1 is not linked to claim with external id %s2";

    private static final String USER_ID_MISMATCH =
        "User with id %s1 does not match given user id %s2";

    private final UserService userService;

    @Autowired
    public ClaimAuthorisationRule(UserService userService) {
        this.userService = userService;

    }

    public void assertClaimCanBeAccessed(Claim claim, String authorisation) {
        if (claim != null) {
            UserDetails userDetails = userService.getUserDetails(authorisation);

            if (!userDetails.getId().equals(claim.getDefendantId())
                && !userDetails.getId().equals(claim.getSubmitterId())) {
                throw new ForbiddenActionException(String.format(USER_NOT_LINKED_MESSAGE,
                    userDetails.getId(),
                    claim.getExternalId()));
            }
        }
    }

    public void assertSubmitterIdMatchesAuthorisation(String userId, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        if (!userDetails.getId().equals(userId)) {
            throw new ForbiddenActionException(String.format(USER_ID_MISMATCH, userDetails.getId(), userId));
        }
    }
}
