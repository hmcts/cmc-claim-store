package uk.gov.hmcts.cmc.domain.models.response;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentIntentionTest {
    private static final PaymentIntention SAMPLE_PAYMENT_INTENTION = PaymentIntention.builder()
        .paymentOption(PaymentOption.INSTALMENTS)
        .repaymentPlan(RepaymentPlan.builder()
            .firstPaymentDate(LocalDate.now().minus(10, ChronoUnit.DAYS))
            .build())
        .build();

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void shouldFailPastDateWhenProposing() {
        Set<ConstraintViolation<PaymentIntention>> violations = validator.validate(
            SAMPLE_PAYMENT_INTENTION,
            PaymentIntention.Proposing.class);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
            .containsExactlyInAnyOrder("is in the past");
    }

    @Test
    public void shouldPassPastDateWhenResponding() {
        Set<ConstraintViolation<PaymentIntention>> violations = validator.validate(
            SAMPLE_PAYMENT_INTENTION,
            PaymentIntention.Responding.class);

        assertThat(violations).isEmpty();
    }
}
