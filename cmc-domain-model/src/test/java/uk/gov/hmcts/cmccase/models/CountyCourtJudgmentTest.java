package uk.gov.hmcts.cmccase.models;

import org.junit.Test;
import uk.gov.hmcts.cmccase.models.ccj.PaymentOption;
import uk.gov.hmcts.cmccase.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmccase.models.sampledata.SampleRepaymentPlan;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmccase.utils.BeanValidator.validate;

public class CountyCourtJudgmentTest {

    @Test
    public void shouldBeSuccessfulValidationForValidCCJModel() {
        //given
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder().build();
        //when
        Set<String> response = validate(ccj);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForNullLineOne() {
        //given
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .withPaymentOption(PaymentOption.IMMEDIATELY)
            .build();
        //when
        Set<String> errors = validate(ccj);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("Invalid county court judgment request");
    }
}
