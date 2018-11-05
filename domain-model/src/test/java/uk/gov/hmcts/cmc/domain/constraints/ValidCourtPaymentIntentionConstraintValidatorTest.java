package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.math.BigDecimal;
import javax.validation.ConstraintValidatorContext;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

@RunWith(MockitoJUnitRunner.class)
public class ValidCourtPaymentIntentionConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private ValidCourtPaymentIntentionConstraintValidator validator;

    @Before
    public void beforeEachTest() {
        validator = new ValidCourtPaymentIntentionConstraintValidator();

        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(
            ConstraintValidatorContext.ConstraintViolationBuilder.class
        );

        when(builder.addPropertyNode(anyString()))
            .thenReturn(
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class)
            );

        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);

    }

    @Test
    public void shouldBeValidWhenCourtPaymentIntentionIsPresentAndDisposableIsPositive() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(BigDecimal.valueOf(1000))
            .decisionType(DecisionType.DEFENDANT)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isTrue();
    }

    @Test
    public void shouldBeInvalidWhenCourtPaymentIntentionIsPresentAndDisposableIsZero() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(BigDecimal.ZERO)
            .decisionType(DecisionType.DEFENDANT)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }


    @Test
    public void shouldBeInvalidWhenCourtPaymentIntentionIsPresentAndDisposableIsNegative() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(BigDecimal.valueOf(-1))
            .decisionType(DecisionType.DEFENDANT)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }

    @Test
    public void shouldBeValidWhenCourtPaymentIntentionIsNullWithDecisionTypeDefendant() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(null)
            .decisionType(DecisionType.DEFENDANT)
            .disposableIncome(BigDecimal.ZERO)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isTrue();
    }

    @Test
    public void shouldBeValidWhenCourtPaymentIntentionIsNullWithDecisionTypeDefendantAndDisposablePositive() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(null)
            .decisionType(DecisionType.DEFENDANT)
            .disposableIncome(BigDecimal.TEN)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }

    @Test
    public void shouldValidWhenCourtPaymentIntentionIsPresentWithDecisionTypeClaimantAndDisposableIsNegative() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT)
            .disposableIncome(BigDecimal.valueOf(-1))
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }

    @Test
    public void shouldValidWhenCourtPaymentIntentionIsPresentWithDecisionTypeClaimantAndDisposableIsZero() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT)
            .disposableIncome(BigDecimal.ZERO)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }

    @Test
    public void shouldValidWhenCourtPaymentIntentionIsPresentWithDecisionTypeClaimantAndDisposableIsPositive() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT)
            .disposableIncome(BigDecimal.valueOf(1000))
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isTrue();
    }

}
