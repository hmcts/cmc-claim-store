package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.legaladvisor.CCDOrderGenerationData;

import java.util.List;

public class GenerateOrderRuleTest {

    private GenerateOrderRule generateOrderRule = new GenerateOrderRule();

    @Test
    public void shouldReturnValidationMessageWhenExpertPermissionIsNotProvided() {
        CCDCase ccdCase = CCDCase.builder()
            .directionOrderData(CCDOrderGenerationData.builder()
                .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.YES)
                .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.YES)
                .expertReportPermissionPartyGivenToClaimant(null)
                .expertReportPermissionPartyGivenToDefendant(null)
                .build())
            .build();
        List<String> validations = generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase);

        Assertions.assertThat(validations).isNotEmpty()
            .hasSize(2)
            .contains(GenerateOrderRule.CLAIMANT_REQUESTED_FOR_EXPORT_REPORT)
            .contains(GenerateOrderRule.DEFENDANT_REQUESTED_FOR_EXPORT_REPORT);
    }

    @Test
    public void shouldNotReturnValidationMessageWhenExpertPermissionIsProvided() {
        CCDCase ccdCase = CCDCase.builder()
            .directionOrderData(CCDOrderGenerationData.builder()
                .expertReportPermissionPartyAskedByClaimant(CCDYesNoOption.YES)
                .expertReportPermissionPartyAskedByDefendant(CCDYesNoOption.YES)
                .expertReportPermissionPartyGivenToClaimant(CCDYesNoOption.YES)
                .expertReportPermissionPartyGivenToDefendant(CCDYesNoOption.NO)
                .build())
            .build();
        List<String> validations = generateOrderRule.validateExpectedFieldsAreSelectedByLegalAdvisor(ccdCase);

        Assertions.assertThat(validations).isEmpty();
    }
}
