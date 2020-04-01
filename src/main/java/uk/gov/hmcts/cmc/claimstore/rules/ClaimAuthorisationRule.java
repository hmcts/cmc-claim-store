package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Component
public class ClaimAuthorisationRule {

    private static final String USER_NOT_LINKED_MESSAGE =
        "User id %s does not match linked ids on claim";

    private static final String USER_ID_MISMATCH =
        "Given user id %s does not match authorised id";

    private final UserService userService;

    @Autowired
    public ClaimAuthorisationRule(UserService userService) {
        this.userService = userService;

    }

    public void assertClaimCanBeAccessed(Claim claim, String authorisation) {
        if (claim != null) {
            UserDetails userDetails = userService.getUserDetails(authorisation);
            assertClaimCanBeAccessed(claim, userDetails);
        }
    }

    public void assertClaimCanBeAccessed(Claim claim, User user) {
        if (claim != null) {
            UserDetails userDetails = user.getUserDetails();
            assertClaimCanBeAccessed(claim, userDetails);
        }
    }

    private void assertClaimCanBeAccessed(Claim claim, UserDetails userDetails) {
        if (!userDetails.isCaseworker()
            && !userDetails.getId().equals(claim.getDefendantId())
            && !userDetails.getId().equals(claim.getSubmitterId())
            && !userDetails.getId().equals(claim.getLetterHolderId())) {
            throw new ForbiddenActionException(String.format(USER_NOT_LINKED_MESSAGE,
                userDetails.getId()));
        }
    }

    public void assertUserIdMatchesAuthorisation(String userId, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);

        if (!userDetails.getId().equals(userId) && !userDetails.isCaseworker()) {
            throw new ForbiddenActionException(String.format(USER_ID_MISMATCH, userId));
        }
    }
}
