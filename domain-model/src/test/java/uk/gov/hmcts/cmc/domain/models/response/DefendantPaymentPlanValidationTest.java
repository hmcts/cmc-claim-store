package uk.gov.hmcts.cmc.domain.models.response;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SampleDefendantPaymentPlan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.BeanValidator.validate;

public class DefendantPaymentPlanValidationTest {
    @Test
    public void shouldPassForValidSample() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder().build();
        //when
        Set<String> response = validate(defendantPaymentPlan);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldFailWhenExplanationTooLong() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder()
            .withExplantion(StringUtils.repeat("a", 99001))
            .build();
        //when
        Set<String> errors = validate(defendantPaymentPlan);
        //then
        assertThat(errors)
            .containsExactly("explanation : size must be between 0 and 99000");
    }

    @Test
    public void shouldFailWhenFirstPaymentIsZero() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder()
            .withFirstPayment(BigDecimal.ZERO)
            .build();
        //when
        Set<String> errors = validate(defendantPaymentPlan);
        //then
        assertThat(errors)
            .containsExactly("firstPayment : must be greater than or equal to 0.01");
    }

    @Test
    public void shouldFailWhenInstalmentAmountIsZero() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder()
            .withInstalmentAmount(BigDecimal.ZERO)
            .build();
        //when
        Set<String> errors = validate(defendantPaymentPlan);
        //then
        assertThat(errors)
            .containsExactly("instalmentAmount : must be greater than or equal to 0.01");
    }

    @Test
    public void shouldFailWhenFirstPaymentDateIsInThePast() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder()
            .withFirstPaymentDate(LocalDate.now().minusDays(2))
            .build();
        //when
        Set<String> errors = validate(defendantPaymentPlan);
        //then
        assertThat(errors)
            .containsExactly("firstPaymentDate : is in the past");
    }

    @Test
    public void shouldFailWhenPaymentScheduleIsNull() {
        //given
        DefendantPaymentPlan defendantPaymentPlan = SampleDefendantPaymentPlan.builder()
            .withPaymentSchedule(null)
            .build();
        //when
        Set<String> errors = validate(defendantPaymentPlan);
        //then
        assertThat(errors)
            .containsExactly("paymentSchedule : may not be null");
    }
}
