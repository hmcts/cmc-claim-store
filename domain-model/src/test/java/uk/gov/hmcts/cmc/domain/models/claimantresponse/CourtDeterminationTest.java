package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Set;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule.EVERY_MONTH;
import static uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention.bySetDate;

public class CourtDeterminationTest {

    @Test
    public void shouldBeValidWhenCourtPaymentIntentionIsBlankForClaimantInFavourOfDefendant() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(TEN)
            .courtPaymentIntention(null)
            .decisionType(DecisionType.CLAIMANT_IN_FAVOUR_OF_DEFENDANT)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenCourtPaymentIntentionIsNotBlankForClaimantInFavourOfDefendant() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(TEN)
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT_IN_FAVOUR_OF_DEFENDANT)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(1)
            .containsOnly("courtPaymentIntention : should be blank for decision CLAIMANT_IN_FAVOUR_OF_DEFENDANT");
    }

    @Test
    public void shouldBeSuccessfulValidationForValidCourtDeterminationBySetDate() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(TEN)
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeSuccessfulValidationForValidCourtDeterminationByInstallment() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(PaymentIntention.builder()
                .paymentOption(INSTALMENTS)
                .repaymentPlan(RepaymentPlan.builder()
                    .firstPaymentDate(now().plusDays(14))
                    .instalmentAmount(TEN)
                    .paymentSchedule(EVERY_MONTH)
                    .build())
                .build())
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .disposableIncome(TEN)
            .decisionType(DecisionType.CLAIMANT)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenMissingCourtCalculatedPaymentIntention() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(null)
            .courtPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .decisionType(DecisionType.CLAIMANT)
            .disposableIncome(TEN)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(1);
    }

    @Test
    public void shouldBeValidWhenMissingCourtPaymentIntentionAndZeroDisposableForDefendantDecision() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(null)
            .decisionType(DecisionType.DEFENDANT)
            .disposableIncome(ZERO)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldNotBeValidWhenMissingCourtPaymentIntentionAndNonZeroDisposableForDefendantDecision() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtDecision(bySetDate())
            .courtPaymentIntention(null)
            .decisionType(DecisionType.DEFENDANT)
            .disposableIncome(TEN)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(1);
    }
}
