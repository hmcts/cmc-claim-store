package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ValidCountyCourtJudgmentValidatorTest {

    @Mock
    private ValidCountyCourtJudgment annotation;

    @Mock
    private ConstraintValidatorContext context;

    private final ValidCountyCourtJudgmentValidator validator = new ValidCountyCourtJudgmentValidator();

    @Before
    public void beforeEachTest() {
        validator.initialize(annotation);
    }

    @Test
    public void shouldReturnTrueForNullValidationInput() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForNullPaymentOptionInput() {
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(null)
            .build();
        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidModelWithImmediatelyPaymentOption() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .build();

        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidModelWithPayByInstalmentsPaymentOption() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(
                SampleRepaymentPlan.builder().build()
            ).build();

        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnTrueForValidModelWithPayBySetDatePaymentOption() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(
                LocalDate.now().plusDays(100)
            ).build();

        assertThat(validator.isValid(ccj, context)).isTrue();
    }

    @Test
    public void shouldReturnFalseForImmediatelyWithPopulatedRepaymentPlan() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForImmediatelyWithPopulatedPayBySetDate() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.IMMEDIATELY)
            .payBySetDate(LocalDate.now().plusDays(100))
            .build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForByInstalmentsWithPopulatedPayBySetDate() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .payBySetDate(LocalDate.now().plusDays(100))
            .build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }

    @Test
    public void shouldReturnFalseForBySetDateWithPopulatedRepaymentPlan() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .build();

        assertThat(validator.isValid(ccj, context)).isFalse();
    }
}
