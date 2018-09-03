package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class ResponseAcceptationTest {

    @Test
    public void shouldBeSuccessfulValidationForValidResponse() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenAmountNotPresent() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder()
            .withAmountPaid(null)
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }

    @Test
    public void shouldBeInvalidWhenAmountIsNegative() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder()
            .withAmountPaid(BigDecimal.valueOf(-10))
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }

    @Test
    public void shouldBeValidWhenAmountIsZero() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder()
            .withAmountPaid(BigDecimal.ZERO)
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }
}
