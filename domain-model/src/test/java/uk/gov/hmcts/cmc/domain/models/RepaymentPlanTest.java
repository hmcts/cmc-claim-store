package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule.EVERY_TWO_WEEKS;

public class RepaymentPlanTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectPaymentPlan() {
        //given
        RepaymentPlan repaymentPlan = RepaymentPlan.builder()
            .instalmentAmount(ONE)
            .firstPaymentDate(now().plusDays(5))
            .paymentSchedule(EVERY_TWO_WEEKS).build();
        //when
        Set<String> response = validate(repaymentPlan);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        RepaymentPlan repaymentPlan = RepaymentPlan.builder().build();
        //when
        Set<String> errors = validate(repaymentPlan);
        //then
        assertThat(errors)
            .hasSize(3);
    }

    @Test
    public void shouldBeInvalidForNullInstalmentAmount() {
        //given
        RepaymentPlan repaymentPlan = RepaymentPlan.builder()
            .paymentSchedule(EVERY_TWO_WEEKS)
            .firstPaymentDate(now().plusDays(5))
            .build();
        //when
        Set<String> errors = validate(repaymentPlan);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("instalmentAmount : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullPaymentSchedule() {
        //given
        RepaymentPlan repaymentPlan = RepaymentPlan.builder()
            .instalmentAmount(TEN)
            .firstPaymentDate(now().plusDays(5))
            .build();
        //when
        Set<String> errors = validate(repaymentPlan);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("paymentSchedule : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullFirstPaymentDate() {
        //given
        RepaymentPlan repaymentPlan = RepaymentPlan.builder()
            .instalmentAmount(TEN)
            .paymentSchedule(EVERY_TWO_WEEKS)
            .build();
        //when
        Set<String> errors = validate(repaymentPlan);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("firstPaymentDate : may not be null");
    }

    @Test
    public void shouldBeInvalidForInstalmentAmountLessThanOnePound() {
        //given
        RepaymentPlan repaymentPlan = RepaymentPlan.builder()
            .instalmentAmount(BigDecimal.valueOf(0.99))
            .paymentSchedule(EVERY_TWO_WEEKS)
            .firstPaymentDate(now().plusDays(5))
            .build();
        //when
        Set<String> errors = validate(repaymentPlan);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("instalmentAmount : must be greater than or equal to 1.00");
    }
}
