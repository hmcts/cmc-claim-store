package uk.gov.hmcts.cmc.claimstore.rules;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class ClaimDocumentsAccessRule {

    public static List<ClaimDocumentType> defendantViewableDocsType = Arrays.stream(ClaimDocumentType.values())
        .filter(not(ClaimDocumentType.CLAIM_ISSUE_RECEIPT::equals))
        .collect(Collectors.toList());

    public static List<ClaimDocumentType> claimantViewableDocsType = Arrays.stream(ClaimDocumentType.values())
        .filter(not(ClaimDocumentType.SEALED_CLAIM::equals))
        .collect(Collectors.toList());

    private static final String FORBIDDEN_ACTION_MESSAGE = "The user logged in is not allowed to access the document";

    private ClaimDocumentsAccessRule() {
        // Do nothing constructor.
    }

    public static void assertDocumentCanBeAccessedByUser(Claim claim, ClaimDocumentType docToDownload, User user) {
        if (!ObjectUtils.anyNotNull(claim, user)) {
            throw new ForbiddenActionException(FORBIDDEN_ACTION_MESSAGE);
        }

        if (user.getUserDetails().getId().equals(claim.getDefendantId())) {
            if (!defendantViewableDocsType.contains(docToDownload)) {
                throw new ForbiddenActionException(FORBIDDEN_ACTION_MESSAGE);
            }
            return;
        } else if (user.getUserDetails().getId().equals(claim.getSubmitterId())) {
            if (!claimantViewableDocsType.contains(docToDownload)) {
                throw new ForbiddenActionException(FORBIDDEN_ACTION_MESSAGE);
            }
            return;
        }

        throw new ForbiddenActionException(FORBIDDEN_ACTION_MESSAGE);
    }
}
