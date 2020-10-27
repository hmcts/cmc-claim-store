package uk.gov.hmcts.cmc.claimstore.rules;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.cmc.claimstore.exceptions.DocumentDownloadForbiddenException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class ClaimDocumentsAccessRule {

    private static final List<ClaimDocumentType> solicitortViewableDocsType = List.of(ClaimDocumentType.SEALED_CLAIM);

    private static final List<ClaimDocumentType> caseworkerViewableDocsType = List.of(ClaimDocumentType.values());

    public static final Supplier<List<ClaimDocumentType>> defendantViewableDocsType = () ->
        Arrays.stream(ClaimDocumentType.values())
            .filter(not(ClaimDocumentType.CLAIM_ISSUE_RECEIPT::equals))
            .collect(Collectors.toList());

    public static final Supplier<List<ClaimDocumentType>> claimantViewableDocsType = () ->
        Arrays.stream(ClaimDocumentType.values())
            .filter(not(ClaimDocumentType.SEALED_CLAIM::equals))
            .collect(Collectors.toList());

    private static final String FORBIDDEN_ACTION_MESSAGE = "The access to the requested document is forbidden";

    private ClaimDocumentsAccessRule() {
        // Do nothing constructor.
    }

    public static void assertDocumentCanBeAccessedByUser(Claim claim, ClaimDocumentType docToDownload, User user) {
        if (!ObjectUtils.allNotNull(claim, user)) {
            throw new DocumentDownloadForbiddenException(FORBIDDEN_ACTION_MESSAGE);
        }

        if (!findViewableDocsList(claim, user)
            .contains(docToDownload)) {
            throw new DocumentDownloadForbiddenException(FORBIDDEN_ACTION_MESSAGE);
        }
    }

    private static List<ClaimDocumentType> findViewableDocsList(Claim claim, User user) {

        UserDetails userDetails = user.getUserDetails();
        if (userDetails.isSolicitor()) {
            return solicitortViewableDocsType;
        }

        if (userDetails.isCaseworker()) {
            return caseworkerViewableDocsType;
        }

        if (userDetails.getId().equals(claim.getDefendantId())
            || userDetails.getId().equals(claim.getLetterHolderId())) {
            return defendantViewableDocsType.get();
        } else if (userDetails.getId().equals(claim.getSubmitterId())) {
            return claimantViewableDocsType.get();
        }
        return Collections.emptyList();
    }
}
