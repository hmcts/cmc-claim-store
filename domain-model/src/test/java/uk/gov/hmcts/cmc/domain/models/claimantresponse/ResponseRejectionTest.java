package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class ResponseRejectionTest {

    @Test
    public void shouldBeSuccessfulValidationForValidResponse() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeValidWhenReasonNotPresent() {
        ClaimantResponse claimantResponse = ResponseRejection.builder().build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeValidWithFreeMediationResponse() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.validRejectionWithFreeMediation();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidWhenMediationPhoneNumberIsTooLong() {
        ClaimantResponse claimantResponse = ResponseRejection
            .builder()
            .mediationPhoneNumber(RandomStringUtils.randomAlphabetic(31)).build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }

    @Test
    public void shouldBeInvalidWhenMediationContactPersonIsTooLong() {
        ClaimantResponse claimantResponse = ResponseRejection
            .builder()
            .mediationContactPerson(RandomStringUtils.randomAlphabetic(31))
            .build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }

    @Test
    public void shouldBeInvalidWhenReasonTooLong() {
        ClaimantResponse claimantResponse = ResponseRejection.builder().reason(randomAlphabetic(99001)).build();

        Set<String> response = validate(claimantResponse);

        assertThat(response).hasSize(1);
    }
}
