package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import java.util.Set;

import static java.math.BigDecimal.TEN;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule.EVERY_MONTH;

public class CourtDeterminationTest {

    @Test
    public void shouldBeSuccessfulValidationForValidCourtDeterminationBySetDate() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtCalculatedPaymentIntention(PaymentIntention.builder()
                .paymentOption(BY_SPECIFIED_DATE)
                .paymentDate(now().plusDays(30))
                .build())
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeSuccessfulValidationForValidCourtDeterminationByInstallment() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtCalculatedPaymentIntention(PaymentIntention.builder()
                .paymentOption(INSTALMENTS)
                .repaymentPlan(RepaymentPlan.builder()
                    .firstPaymentDate(now().plusDays(14))
                    .instalmentAmount(TEN)
                    .paymentSchedule(EVERY_MONTH)
                    .build())
                .build())
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenMissingCourtCalculatedPaymentIntention() {
        CourtDetermination courtDetermination = CourtDetermination.builder()
            .courtCalculatedPaymentIntention(null)
            .build();

        Set<String> response = validate(courtDetermination);

        assertThat(response).hasSize(1);
    }
}
