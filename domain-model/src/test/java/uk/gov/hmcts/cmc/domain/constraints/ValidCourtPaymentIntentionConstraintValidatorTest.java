package uk.gov.hmcts.cmc.domain.constraints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import javax.validation.ConstraintValidatorContext;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
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
    }

    @Test
    public void shouldNotBeValidWhenCourtPaymentIntentionIsPresent() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.DEFENDANT)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }

    @Test
    public void shouldNotBeValidWhenCourtPaymentIntentionIsPresentWithDecisionTypeClaimant() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isFalse();
    }

    @Test
    public void shouldBeValidWhenCourtPaymentIntentionIsNullWithDecisionTypeDefendant() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(null)
            .decisionType(DecisionType.DEFENDANT)
            .build();
        assertThat(validator.isValid(courtDetermination, context)).isTrue();
    }

}
