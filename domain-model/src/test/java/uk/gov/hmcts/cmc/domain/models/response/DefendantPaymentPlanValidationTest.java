package uk.gov.hmcts.cmc.domain.models.response;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleDefendantPaymentPlan;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class DefendantPaymentPlanValidationTest {
    @Test
    public void passesForValidSample() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder().build();
        //when
        Set<String> response = validate(defendantPaymentPlan);
        //then
        assertThat(response).isEmpty();
    }
}
