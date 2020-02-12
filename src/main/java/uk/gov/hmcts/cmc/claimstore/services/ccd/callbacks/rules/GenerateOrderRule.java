package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class GenerateOrderRule {
    public static final String CLAIMANT_REQUESTED_FOR_EXPORT_REPORT =
        "Enter if you  grant permission for expert to the claimant";

    public static final String DEFENDANT_REQUESTED_FOR_EXPORT_REPORT =
        "Enter if you  grant permission for expert to the defendant";

    public List<String> validateExpectedFieldsAreSelectedByLegalAdvisor(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "ccd case object can not be null");

        List<String> validationErrors = new ArrayList<>();

        if (isPresentAndIsYes(ccdCase.getExpertReportPermissionPartyAskedByClaimant())
            && !isPresent(ccdCase.getExpertReportPermissionPartyGivenToClaimant())
        ) {
            validationErrors.add(CLAIMANT_REQUESTED_FOR_EXPORT_REPORT);
        }

        if (isPresentAndIsYes(ccdCase.getExpertReportPermissionPartyAskedByDefendant())
            && !isPresent(ccdCase.getExpertReportPermissionPartyGivenToDefendant())
        ) {
            validationErrors.add(DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
        }
        return validationErrors;
    }

    private boolean isPresent(CCDYesNoOption input) {
        return Optional.ofNullable(input).isPresent();
    }

    private boolean isPresentAndIsYes(CCDYesNoOption input) {
        return isPresent(input) && input.toBoolean();
    }
}
