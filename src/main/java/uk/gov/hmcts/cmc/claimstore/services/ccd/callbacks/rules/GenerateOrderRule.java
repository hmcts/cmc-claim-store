package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;

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
        CCDOrderGenerationData directionOrderData = ccdCase.getDirectionOrderData();

        List<String> validationErrors = new ArrayList<>();

        if (fromEnum(directionOrderData.getExpertReportPermissionPartyAskedByClaimant())
            && !fromEnum(directionOrderData.getExpertReportPermissionPartyGivenToClaimant())
        ) {
            validationErrors.add(CLAIMANT_REQUESTED_FOR_EXPORT_REPORT);
        }

        if (fromEnum(directionOrderData.getExpertReportPermissionPartyAskedByDefendant())
            && !fromEnum(directionOrderData.getExpertReportPermissionPartyGivenToDefendant())
        ) {
            validationErrors.add(DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
        }
        return validationErrors;
    }

    private boolean fromEnum(CCDYesNoOption input) {
        return Optional.ofNullable(input).map(CCDYesNoOption::toBoolean).orElse(false);
    }
}
