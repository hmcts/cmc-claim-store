package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GenerateOrderRule {
    public static final String CLAIMANT_REQUESTED_FOR_EXPORT_REPORT =
        "The claimant has asked for permission to use expert report";

    public static final String DEFENDANT_REQUESTED_FOR_EXPORT_REPORT =
        "The defendant has asked for permission to use expert report";

    public List<String> validateExpectedFieldsAreSelectedByLegalAdvisor(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "ccd case object can not be null");
        CCDOrderGenerationData directionOrderData = ccdCase.getDirectionOrderData();

        List<String> validationErrors = new ArrayList<>();

        if (directionOrderData.getExpertReportPermissionPartyAskedByClaimant().toBoolean()
            && directionOrderData.getExpertReportPermissionPartyGivenToClaimant() == null
        ) {
            validationErrors.add(CLAIMANT_REQUESTED_FOR_EXPORT_REPORT);
        }

        if (directionOrderData.getExpertReportPermissionPartyAskedByDefendant().toBoolean()
            && directionOrderData.getExpertReportPermissionPartyGivenToDefendant() == null
        ) {
            validationErrors.add(DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
        }
        return validationErrors;
    }
}
