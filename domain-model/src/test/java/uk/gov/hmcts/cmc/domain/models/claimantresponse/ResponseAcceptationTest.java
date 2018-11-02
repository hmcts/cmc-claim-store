package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.math.BigDecimal;
import java.util.Set;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.REFER_TO_JUDGE;

public class ResponseAcceptationTest {

    @Test
    public void shouldBeSuccessfulValidationForValidResponse() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeValidWhenAmountIsZero() {
        ClaimantResponse claimantResponse = ResponseAcceptation.builder()
            .amountPaid(ZERO)
            .formaliseOption(REFER_TO_JUDGE)
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenAmountIsNegative() {
        ClaimantResponse claimantResponse = ResponseAcceptation.builder()
            .amountPaid(BigDecimal.valueOf(-10))
            .formaliseOption(REFER_TO_JUDGE)
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }

    @Test
    public void shouldBeInvalidWhenMissingPaymentIntentionInCourtDetermination() {
        ClaimantResponse claimantResponse = ResponseAcceptation.builder()
            .amountPaid(TEN)
            .courtDetermination(CourtDetermination.builder()
                .courtDecision(null)
                .courtPaymentIntention(SamplePaymentIntention.bySetDate())
                .disposableIncome(TEN)
                .decisionType(CLAIMANT)
                .build())
            .formaliseOption(CCJ)
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }
}
