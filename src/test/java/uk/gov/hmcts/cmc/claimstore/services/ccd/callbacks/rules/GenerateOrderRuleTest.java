package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GenerateOrderRuleTest {

    private final GenerateOrderRule generateOrderRule = new GenerateOrderRule();

    @Test
    public void shouldReturnValidationMessageWhenExpertPermissionIsNotProvidedCaseLevelExperts() {
        CCDCase ccdCase = CCDCase.builder()
            .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.YES)
            .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.YES)
            .grantExpertReportPermission(null)
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase, true, validations);

        Assertions.assertThat(validations).isNotEmpty()
            .hasSize(2)
            .contains(GenerateOrderRule.CLAIMANT_REQUESTED_FOR_EXPORT_REPORT)
            .contains(GenerateOrderRule.DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
    }

    @Test
    public void shouldNotReturnValidationMessageWhenExpertPermissionIsProvidedCaseLevelExperts() {
        CCDCase ccdCase = CCDCase.builder()
            .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.YES)
            .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.YES)
            .grantExpertReportPermission(CCDYesNoOption.YES)
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase, true, validations);

        Assertions.assertThat(validations).isEmpty();
    }

    @Test
    public void shouldNotReturnValidationMessageWhenUserHasNotAskedForPermissionCaseLevelExperts() {
        CCDCase ccdCase = CCDCase.builder()
            .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.NO)
            .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.NO)
            .grantExpertReportPermission(null)
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase, true, validations);

        Assertions.assertThat(validations).isEmpty();
    }

    @Test
    public void shouldReturnValidationMessageWhenExpertPermissionIsNotProvided() {
        CCDCase ccdCase = CCDCase.builder()
            .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.YES)
            .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.YES)
            .expertReportPermissionPartyGivenToClaimant(null)
            .expertReportPermissionPartyGivenToDefendant(null)
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase, false, validations);

        Assertions.assertThat(validations).isNotEmpty()
            .hasSize(2)
            .contains(GenerateOrderRule.CLAIMANT_REQUESTED_FOR_EXPORT_REPORT)
            .contains(GenerateOrderRule.DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
    }

    @Test
    public void shouldNotReturnValidationMessageWhenExpertPermissionIsProvided() {
        CCDCase ccdCase = CCDCase.builder()
            .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.YES)
            .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.YES)
            .expertReportPermissionPartyGivenToClaimant(CCDYesNoOption.YES)
            .expertReportPermissionPartyGivenToDefendant(CCDYesNoOption.YES)
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase, false, validations);

        Assertions.assertThat(validations).isEmpty();
    }

    @Test
    public void shouldNotReturnValidationMessageWhenUserHasNotAskedForPermission() {
        CCDCase ccdCase = CCDCase.builder()
            .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.NO)
            .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.NO)
            .expertReportPermissionPartyGivenToClaimant(null)
            .expertReportPermissionPartyGivenToDefendant(null)
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase, false, validations);

        Assertions.assertThat(validations).isEmpty();
    }

    @Test
    public void shouldReturnDateValidationErrorForUploadDeadline() {
        CCDCase ccdCase = CCDCase.builder()
            .docUploadDeadline(LocalDate.parse("2019-03-20"))
            .eyewitnessUploadDeadline(LocalDate.now().plusDays(1))
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateDate(ccdCase, validations);

        Assertions.assertThat(validations).isNotEmpty()
            .hasSize(1)
        .contains(GenerateOrderRule.PAST_DATE_ERROR_MESSAGE);
    }

    @Test
    public void shouldReturnDateValidationErrorForEyewitnessUploadDeadline() {
        CCDCase ccdCase = CCDCase.builder()
            .docUploadDeadline(LocalDate.now().plusDays(1))
            .eyewitnessUploadDeadline(LocalDate.parse("2019-03-20"))
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateDate(ccdCase, validations);

        Assertions.assertThat(validations).isNotEmpty()
            .hasSize(1)
            .contains(GenerateOrderRule.PAST_DATE_ERROR_MESSAGE);
    }

    @Test
    public void shouldNotReturnDateValidation() {
        CCDCase ccdCase = CCDCase.builder()
            .docUploadDeadline(LocalDate.now().plusDays(1))
            .eyewitnessUploadDeadline(LocalDate.now().plusDays(1))
            .build();
        List<String> validations = new ArrayList<>();
        generateOrderRule.validateDate(ccdCase, validations);

        Assertions.assertThat(validations).isEmpty();
    }
}
