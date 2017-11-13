package uk.gov.hmcts.cmc.claimstore.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleRepaymentPlan;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidCountyCourtJudgmentValidatorTest {

    @Mock
    private ValidCountyCourtJudgment annotation;

    @Mock
    private ConstraintValidatorContext context;

    private ValidCountyCourtJudgmentValidator validator = new ValidCountyCourtJudgmentValidator();

    @Before
    public void beforeEachTest() {
        validator.initialize(annotation);
    }

    @Test
    public void shouldReturnTrueForNullValidationInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidModelWithImmediatelyPaymentOption() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder().withPaymentOptionImmediately().build();

        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidModelWithPayByInstalmentsPaymentOption() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(
                SampleRepaymentPlan.builder().build()
            ).build();

        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidModelWithPayBySetDatePaymentOption() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPayBySetDate(
                LocalDate.now().plusDays(100)
            ).build();

        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnFalseForImmediatelyWithPopulatedRepaymentPlan() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .withPaymentOption(PaymentOption.IMMEDIATELY).build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForImmediatelyWithPopulatedPayBySetDate() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPayBySetDate(LocalDate.now().plusDays(100))
            .withPaymentOption(PaymentOption.IMMEDIATELY).build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForByInstalmentsWithPopulatedPayBySetDate() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withPayBySetDate(LocalDate.now().plusDays(100))
            .withPaymentOption(PaymentOption.INSTALMENTS).build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForBySetDateWithPopulatedRepaymentPlan() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .withPaymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE).build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }
}
