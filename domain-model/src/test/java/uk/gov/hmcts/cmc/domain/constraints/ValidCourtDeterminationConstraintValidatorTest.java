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
public class ValidCourtDeterminationConstraintValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private ValidCourtDeterminationConstraintValidator validator;

    @Before
    public void beforeEachTest() {
        validator = new ValidCourtDeterminationConstraintValidator();

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
    public void shouldBeValidWhenDisposableIsPositiveForDefendantDecisionType() {
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
    public void shouldBeInvalidWhenDisposableIsNegative() {
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
