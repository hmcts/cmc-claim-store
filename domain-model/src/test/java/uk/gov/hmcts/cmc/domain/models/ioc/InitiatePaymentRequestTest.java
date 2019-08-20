package uk.gov.hmcts.cmc.domain.models.ioc;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInitiatePaymentRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class InitiatePaymentRequestTest {

    @Test
    public void shouldBeInvalidWhenGivenNullExternalId() {
        InitiatePaymentRequest theirDetails = SampleInitiatePaymentRequest.builder()
            .externalId(null)
            .build();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("externalId : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullAmount() {
        InitiatePaymentRequest theirDetails = SampleInitiatePaymentRequest.builder()
            .amount(null)
            .build();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("amount : may not be null");
    }

    @Test
    public void shouldBeInvalidWhenGivenNullIssuedOnDate() {
        InitiatePaymentRequest theirDetails = SampleInitiatePaymentRequest.builder()
            .issuedOn(null)
            .build();

        Set<String> validationErrors = validate(theirDetails);

        assertThat(validationErrors)
            .hasSize(1)
            .contains("issuedOn : may not be null");
    }
}
