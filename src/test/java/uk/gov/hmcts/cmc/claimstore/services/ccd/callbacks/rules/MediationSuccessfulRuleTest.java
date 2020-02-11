package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;

import java.util.List;

public class MediationSuccessfulRuleTest {

    private final MediationSuccessfulRule mediationSuccessfulRule = new MediationSuccessfulRule();

    @Test
    public void shouldReturnValidationMessageWhenNoDocumentIsUploaded() {
        CCDCase ccdCase = CCDCase.builder().build();

        List<String> validations = mediationSuccessfulRule.validateMediationAgreementUploadedByCaseworker(ccdCase);

        Assertions.assertThat(validations).isNotEmpty()
                .hasSize(1)
                .contains(MediationSuccessfulRule.STAFF_UPLOAD_MEDIATION_AGREEMENT);
    }

    @Test
    public void shouldReturnValidationMessageWhenNoMediationAgreementIsUploaded() {
        CCDCase ccdCase = SampleData.withPaperResponseFromStaffUploadedDoc();

        List<String> validations = mediationSuccessfulRule.validateMediationAgreementUploadedByCaseworker(ccdCase);

        Assertions.assertThat(validations).isNotEmpty()
                .hasSize(1)
                .contains(MediationSuccessfulRule.STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT);
    }

    @Test
    public void shouldReturnValidationMessageWhenMediationAgreementIsNotTypePDF() {
        CCDCase ccdCase = SampleData.withMediationAgreementNotPdf();
        List<String> validations = mediationSuccessfulRule.validateMediationAgreementUploadedByCaseworker(ccdCase);

        Assertions.assertThat(validations).isNotEmpty()
                .hasSize(1)
                .contains(MediationSuccessfulRule.STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT);
    }

    @Test
    public void shouldNotReturnValidationMessageWhenMediationAgreementIsTypePDF() {
        CCDCase ccdCase = SampleData.withMediationAgreementPdf();
        List<String> validations = mediationSuccessfulRule.validateMediationAgreementUploadedByCaseworker(ccdCase);

        Assertions.assertThat(validations).isEmpty();
    }
}
