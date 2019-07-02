package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@Service
public class ClaimSubmissionOperationIndicatorRule {

    public void assertOperationIndicatorUpdateIsValid(
        @NotNull Claim claim,
        ClaimSubmissionOperationIndicators newClaimSubmissionOperationIndicators
    ) {
        List<String> invalidIndicators = new ArrayList<>();
        ClaimSubmissionOperationIndicators existingClaimSubmissionOperationIndicators
            = claim.getClaimSubmissionOperationIndicators();

        if (existingClaimSubmissionOperationIndicators.getClaimIssueReceiptUpload().equals(NO)
            && newClaimSubmissionOperationIndicators.getClaimIssueReceiptUpload().equals(YES)) {
            invalidIndicators.add("claimIssueReceiptUpload");
        }

        if (existingClaimSubmissionOperationIndicators.getSealedClaimUpload().equals(NO)
            && newClaimSubmissionOperationIndicators.getSealedClaimUpload().equals(YES)) {
            invalidIndicators.add("sealedClaimUpload");
        }

        if (existingClaimSubmissionOperationIndicators.getBulkPrint().equals(NO)
            && newClaimSubmissionOperationIndicators.getBulkPrint().equals(YES)) {
            invalidIndicators.add("bulkPrint");
        }

        if (existingClaimSubmissionOperationIndicators.getClaimantNotification().equals(NO)
            && newClaimSubmissionOperationIndicators.getClaimantNotification().equals(YES)) {
            invalidIndicators.add("claimantNotification");
        }

        if (existingClaimSubmissionOperationIndicators.getDefendantNotification().equals(NO)
            && newClaimSubmissionOperationIndicators.getDefendantNotification().equals(YES)) {
            invalidIndicators.add("defendantNotification");
        }

        if (existingClaimSubmissionOperationIndicators.getRpa().equals(NO)
            && newClaimSubmissionOperationIndicators.getRpa().equals(YES)) {
            invalidIndicators.add("rpa");
        }

        if (existingClaimSubmissionOperationIndicators.getStaffNotification().equals(NO)
            && newClaimSubmissionOperationIndicators.getStaffNotification().equals(YES)) {
            invalidIndicators.add("staffNotification");
        }

        if (invalidIndicators.size() > 0) {
            throw new BadRequestException("Invalid input. The following indicator(s)"
                + invalidIndicators + " cannot be set to Yes");
        }
    }
}
