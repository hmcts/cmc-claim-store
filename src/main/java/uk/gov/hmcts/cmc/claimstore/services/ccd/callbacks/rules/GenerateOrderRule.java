package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderDirection;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class GenerateOrderRule {

    public static final String CLAIMANT_REQUESTED_FOR_EXPORT_REPORT =
        "Enter if you  grant permission for expert to the claimant";

    public static final String DEFENDANT_REQUESTED_FOR_EXPORT_REPORT =
        "Enter if you  grant permission for expert to the defendant";

    public static final String PAST_DATE_ERROR_MESSAGE =
        "The date entered cannot be in the past";

    public void validateExpectedFieldsAreSelectedByLegalAdvisor(CCDCase ccdCase, boolean expertsAtCaseLevel,
                                                                List<String> validationErrors) {
        Objects.requireNonNull(ccdCase, "ccd case object can not be null");

        if (expertsAtCaseLevel) {
            if (isPresentAndIsYes(ccdCase.getExpertReportPermissionPartyAskedByClaimant())
                && !isPresent(ccdCase.getGrantExpertReportPermission())
            ) {
                validationErrors.add(CLAIMANT_REQUESTED_FOR_EXPORT_REPORT);
            }

            if (isPresentAndIsYes(ccdCase.getExpertReportPermissionPartyAskedByDefendant())
                && !isPresent(ccdCase.getGrantExpertReportPermission())
            ) {
                validationErrors.add(DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
            }
        } else {
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
        }
    }

    private boolean isPresent(CCDYesNoOption input) {
        return Optional.ofNullable(input).isPresent();
    }

    private boolean isPresentAndIsYes(CCDYesNoOption input) {
        return isPresent(input) && input.toBoolean();
    }

    public void validateDate(CCDCase ccdCase, List<String> validationErrors) {
        Objects.requireNonNull(ccdCase, "ccd case object can not be null");

        LocalDate uploadDeadlineDate = ccdCase.getDocUploadDeadline();
        LocalDate eyewitnessUploadDeadlineDate = ccdCase.getEyewitnessUploadDeadline();
        List<CCDCollectionElement<CCDOrderDirection>> otherDirectionOrder =
            ccdCase.getOtherDirections() == null ? List.of() : ccdCase.getOtherDirections();

        if (uploadDeadlineDate.isBefore(LocalDate.now()) || eyewitnessUploadDeadlineDate.isBefore(LocalDate.now())) {
            validationErrors.add(PAST_DATE_ERROR_MESSAGE);
        }
        if (otherDirectionOrder.stream().anyMatch(e -> e.getValue().getSendBy().isBefore(LocalDate.now()))) {
            validationErrors.add(PAST_DATE_ERROR_MESSAGE);
        }
    }
}
