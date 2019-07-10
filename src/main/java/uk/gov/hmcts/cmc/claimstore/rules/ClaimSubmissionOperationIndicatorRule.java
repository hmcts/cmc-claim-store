package uk.gov.hmcts.cmc.claimstore.rules;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.NO;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@Service
public class ClaimSubmissionOperationIndicatorRule {

    public void assertOperationIndicatorUpdateIsValid(
        @NotNull Claim claim,
        ClaimSubmissionOperationIndicators newIndicators
    ) {
        List<String> invalidIndicators = new ArrayList<>();
        ClaimSubmissionOperationIndicators oldIndicators = claim.getClaimSubmissionOperationIndicators();

        for (Field field : ClaimSubmissionOperationIndicators.class.getDeclaredFields()) {
            invalidIndicators.addAll(validateFieldFlagChange(field, claim, newIndicators));
        }

        if (invalidIndicators.size() > 0) {
            throw new BadRequestException("Invalid input. The following indicator(s)"
                + invalidIndicators + " cannot be set to Yes");
        }
    }

    private List<String> validateFieldFlagChange(
        Field field,
        Claim claim,
        ClaimSubmissionOperationIndicators newIndicators
    ) {
        String fieldName = field.getName();
        ClaimSubmissionOperationIndicators oldIndicators = claim.getClaimSubmissionOperationIndicators();
        switch (fieldName) {
            case "claimantNotification":
                return isFlagChangeValid(fieldName,
                    oldIndicators.getClaimantNotification(),
                    newIndicators.getClaimantNotification()
                );
            case "defendantNotification":
                return isFlagChangeValid(fieldName,
                    oldIndicators.getDefendantNotification(),
                    newIndicators.getDefendantNotification()
                );
            case "bulkPrint":
                return isFlagChangeValid(fieldName,
                    oldIndicators.getBulkPrint(),
                    newIndicators.getBulkPrint()
                );
            case "rpa":
                return isFlagChangeValid(fieldName,
                    oldIndicators.getRpa(),
                    newIndicators.getRpa()
                );
            case "staffNotification":
                return isFlagChangeValid(fieldName,
                    oldIndicators.getStaffNotification(),
                    newIndicators.getStaffNotification()
                );
            case "sealedClaimUpload":
                return isFlagChangeValidForDocumentUpload(fieldName,
                    oldIndicators.getSealedClaimUpload(),
                    newIndicators.getSealedClaimUpload(),
                    claim.getClaimDocument(SEALED_CLAIM).isPresent()
                );
            case "claimIssueReceiptUpload":
                return isFlagChangeValidForDocumentUpload(fieldName,
                    oldIndicators.getClaimIssueReceiptUpload(),
                    newIndicators.getClaimIssueReceiptUpload(),
                    claim.getClaimDocument(CLAIM_ISSUE_RECEIPT).isPresent()
                );
            default:
                return Collections.emptyList();
        }
    }

    private List<String> isFlagChangeValid(String fieldName, YesNoOption oldValue, YesNoOption newValue) {
        return oldValue.equals(NO) && newValue.equals(YES) ? ImmutableList.of(fieldName) : Collections.emptyList();
    }

    private List<String> isFlagChangeValidForDocumentUpload(
        String fieldName,
        YesNoOption oldValue,
        YesNoOption newValue,
        boolean isDocumentPresent
    ) {
        return isDocumentPresent || oldValue.equals(NO) && newValue.equals(YES)
            ? ImmutableList.of(fieldName)
            : Collections.emptyList();
    }
}
