package uk.gov.hmcts.cmc.domain.models.offers;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDate;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class OfferValidationTest {

    @Test
    public void shouldHaveNoValidationErrorsForValidOffer() {
        Offer offer = SampleOffer.validDefaults();

        Set<String> validationErrors = validate(offer);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidIfTextIsNull() {
        Offer offer = SampleOffer.builder()
            .withContent(null)
            .build();

        Set<String> validationErrors = validate(offer);

        assertThat(validationErrors).containsOnly("content : may not be empty");
    }

    @Test
    public void shouldBeInvalidIfTextExceedsLengthLimit() {
        Offer offer = SampleOffer.builder()
            .withContent(StringUtils.repeat('X', Offer.CONTENT_LENGTH_LIMIT + 1))
            .build();

        Set<String> validationErrors = validate(offer);

        assertThat(validationErrors).containsOnly(
            format("content : may not be longer than %d characters", Offer.CONTENT_LENGTH_LIMIT)
        );
    }

    @Test
    public void shouldBeInvalidIfCompletionDateIsNull() {
        Offer offer = SampleOffer.builder()
            .withCompletionDate(null)
            .build();

        Set<String> validationErrors = validate(offer);

        assertThat(validationErrors).containsOnly("completionDate : may not be null");
    }

    @Test
    public void shouldBeInvalidIfCompletionDateIsToday() {
        Offer offer = SampleOffer.builder()
            .withCompletionDate(LocalDate.now())
            .build();

        Set<String> validationErrors = validate(offer);

        assertThat(validationErrors).containsOnly("completionDate : must be in the future");
    }

    @Test
    public void shouldBeInvalidIfCompletionDateIsYesterday() {
        Offer offer = SampleOffer.builder()
            .withCompletionDate(LocalDate.now().minusDays(1))
            .build();

        Set<String> validationErrors = validate(offer);

        assertThat(validationErrors).containsOnly("completionDate : must be in the future");
    }

}
